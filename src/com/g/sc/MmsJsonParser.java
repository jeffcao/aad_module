package com.g.sc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Telephony.Mms.Inbox;

import com.g.sc.MediaTypeRecogniser.MediaType;
import com.g.sc.PhoneNumberChecker.PhoneNumberError;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;

public class MmsJsonParser {

	private Context mContext;
	public static final String SMIL_NAME = "smil";
	private static final String TAG = MmsJsonParser.class.toString();
	private static final String[] needPermission = {
			"android.permission.READ_SMS", "android.permission.WRITE_SMS" };
	// 在json解析的过程当中，如果有任何一环出现问题，置这个变量为空，整个解析失败
	private boolean hasErrorHappened = false;

	public MmsJsonParser(Context context) {
		mContext = context;
	}

	/**
	 * @param jsonString
	 *            JSON格式的字符串
	 * @param fromMobile
	 *            发来短信的电话号码
	 * @return 彩信写入成功，返回true,失败返回false
	 * @throws PhoneNumberError
	 *             当传入的号码不正确时抛出此异常
	 * @throws JSONException
	 *             当传入的字符串不是JSON格式或者JSONkey-value解析不正常时抛出
	 * @throws NoSmilException
	 *             当没有SMIL部件或者SMIL部件不是放在第一的位置时抛出此异常
	 * @throws PermissionNotObtainedException
	 *             当导致MmsException发生的权限没有获取时，会抛出此异常
	 */
	public boolean parseMmsJson(String jsonString, String fromMobile)
			throws PhoneNumberError, JSONException, NoSmilException,
			PermissionNotObtainedException {
		checkPhoneNumber(fromMobile);
		checkPermission();
		if (null == jsonString) {
			return false;
		}
		String mms = jsonString;
		AdLogger.i(TAG, "init mms root json: " + mms);
		JSONObject root = new JSONObject(mms);
		AdLogger.i(TAG, "mms root json inited");
		MultimediaMessagePdu pdu = initPdu(root, fromMobile);
		if (pdu != null && !hasErrorHappened) {
			return sendMms(pdu);
		} else {
			return false;
		}
	}

	private void checkPermission() throws PermissionNotObtainedException {
		try {
			boolean isAllPermissionObtained = PermissionChecker
					.isAllPermissionObtained(mContext, needPermission);
			if (!isAllPermissionObtained) {
				throw new PermissionNotObtainedException(
						needPermission.toString());
			}
		} catch (NameNotFoundException e) {
			AdLogger.e(TAG, "permission input error, can't find in system");
		}
	}

	private void checkPhoneNumber(String fromMobile) throws PhoneNumberError {
		if (!PhoneNumberChecker.isNumberCorrect(fromMobile)) {
			PhoneNumberChecker.PhoneNumberError exception = new PhoneNumberChecker.PhoneNumberError(
					"phone number is not correct");
			throw exception;
		}
	}

	/**
	 * @throws MmsException
	 *             当访问的URL不正确（此异常不会出现）或者访问不具有权限的数据库时,会抛出此异常,需要以下两种权限
	 *             <uses-permission android:name="android.permission.READ_SMS"/>
	 *             <uses-permission
	 *             android:name="android.permission.WRITE_SMS"/>
	 *             无权限访问数据库异常，无法捕获，会直接导致进程死掉
	 */
	private boolean sendMms(MultimediaMessagePdu pdu) {
		AdLogger.i(TAG, "将彩信写入");
		PduBody pd = pdu.getBody();
		AdLogger.i(TAG, "彩信总共有" + pd.getPartsNum() + "个部件");
		PduPersister p = PduPersister.getPduPersister(mContext);
		try {
			p.persist(pdu, Inbox.CONTENT_URI);
			return true;
		} catch (MmsException e) {
			return false;
		}
	}

	private MultimediaMessagePdu initPdu(JSONObject root, String fromMobile)
			throws JSONException, NoSmilException {
		PduBody body = initPdubody(root);
		PduHeaders headers = new PduHeaders();
		MultimediaMessagePdu pdu = new MultimediaMessagePdu(headers, body);
		Field field = null;
		Object obj = null;
		try {
			field = headers.getClass().getDeclaredField("mHeaderMap");
			field.setAccessible(true);
			obj = field.get(headers);
		} catch (SecurityException e) {
			AdLogger.e(TAG, e.getMessage());
			return null;
		} catch (NoSuchFieldException e) {
			AdLogger.e(TAG, e.getMessage());
			return null;
		} catch (IllegalArgumentException e) {
			AdLogger.e(TAG, e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			AdLogger.e(TAG, e.getMessage());
			return null;
		}
		// 这里的两个unckecked代码是从源码里面复制，不会出现问题
		@SuppressWarnings("unchecked")
		HashMap<Integer, Object> mHeaderMap = (HashMap<Integer, Object>) obj;
		EncodedStringValue[] mobiles = EncodedStringValue.extract(fromMobile);
		@SuppressWarnings("unchecked")
		ArrayList<EncodedStringValue> list = (ArrayList<EncodedStringValue>) mHeaderMap
				.get(PduHeaders.TO);
		if (null == list) {
			list = new ArrayList<EncodedStringValue>();
		}
		list.add(mobiles[0]);
		mHeaderMap.put(PduHeaders.TO, list);
		mHeaderMap.put(PduHeaders.MESSAGE_TYPE,
				PduHeaders.MESSAGE_TYPE_SEND_REQ);
		mHeaderMap.put(PduHeaders.CONTENT_TYPE,
				ContentType.MULTIPART_RELATED.getBytes());
		pdu.setBody(body);
		return pdu;
	}

	private PduBody initPdubody(JSONObject root) throws JSONException,
			NoSmilException {
		PduBody body = new PduBody();
		List<MmsPduPart> pduParts = initPduparts(root);
		for (MmsPduPart part : pduParts) {
			body.addPart(part);
			AdLogger.i(TAG, "part type is: " + part.getType());
		}
		boolean hasSmil = false;
		MmsPduPart part = pduParts.get(0);
		if (part != null && (part.getType() == MediaType.SMIL)) {
			hasSmil = true;
		}
		if (!hasSmil) {
			throw new NoSmilException(
					"every body must has a smil part and must be placed in first location");
		}
		return body;
	}

	/**
	 * 对于出现错误的part，不加进pduParts链表中
	 */
	private List<MmsPduPart> initPduparts(JSONObject root) throws JSONException {
		// root 可保证为正确的JSONObject
		List<MmsPduPart> pduParts = new ArrayList<MmsPduPart>();
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = root.keys();
		String key = null;
		while (iterator.hasNext()) {
			key = iterator.next();
			MediaTypeRecogniser.MediaType type = MediaTypeRecogniser
					.recogniseMediaType(key);
			if (MediaTypeRecogniser.MediaType.SMIL == type) {
				String data = root.getString(key);
				MmsPduPart pdu = MmsPduPartFactory
						.create(type, SMIL_NAME, data);
				AdLogger.i(TAG, "smil pdu is :" + pdu);
				if (pdu != null) {
					pduParts.add(0, pdu);
				} else {
					hasErrorHappened = true;
				}
			} else {
				initTypePduparts(root, pduParts, key, type);
			}
		}
		return pduParts;
	}

	private void initTypePduparts(JSONObject root, List<MmsPduPart> pduParts,
			String key, MediaTypeRecogniser.MediaType type)
			throws JSONException {
		// root , pduParts, key, type可保证不为空
		if (MediaTypeRecogniser.MediaType.UNKWON == type) {
			hasErrorHappened = true;
			return;
		}
		JSONObject typePart = root.getJSONObject(key);
		@SuppressWarnings("unchecked")
		Iterator<String> typeIterator = typePart.keys();
		String typeKey = null;
		String typeData = null;
		MmsPduPart typePdu = null;
		while (typeIterator.hasNext()) {
			typeKey = typeIterator.next();
			typeData = typePart.getString(typeKey);
			typePdu = MmsPduPartFactory.create(type, typeKey, typeData);
			AdLogger.i(TAG, "type pdu is: " + typePdu);
			if (pduParts != null) {
				pduParts.add(typePdu);
			} else {
				hasErrorHappened = true;
			}
		}
	}

}
