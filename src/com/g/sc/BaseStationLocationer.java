package com.g.sc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class BaseStationLocationer extends Locationer {

	private HttpClient httpclient;
	private HttpParams httpParameters;
	private int timeoutConnection = 3000;
	private int timeoutSocket = 5000;
	private static final String TAG = BaseStationLocationer.class.toString();

	public BaseStationLocationer(Context context) {
		super(context);
	}

	/**
	 * permission need: android.permission.ACCESS_COARSE_LOCATION
	 * android.permission.INTERNET
	 * 
	 * @return
	 */
	private void startToLocate() {
		GeographyLocation geoLocation = new GeographyLocation();
		TelephonyManager mTelNet = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String operator = mTelNet.getNetworkOperator();
		if (operator == null || operator.length() == 0) {
			AdLogger.e(TAG, "SIM卡停机，无法进行基站定位。");
			return;
		}
		String mcc = operator.substring(0, 3);
		String mnc = operator.substring(3);
		CellLocation location = mTelNet.getCellLocation();
		if (location instanceof GsmCellLocation) {
			gsmLocate(geoLocation, mcc, mnc, (GsmCellLocation) location);
		} else if (location instanceof CdmaCellLocation) {
			cdmaLocate(geoLocation, (CdmaCellLocation) location);
		} else {
			AdLogger.e(TAG, "无法定位的SIM卡制式");
		}
	}

	private void cdmaLocate(GeographyLocation geoLocation,
			CdmaCellLocation location) {
		if (location != null) {
			AdLogger.i(TAG, "开始进行CDMA基站位置解析");
			double lat = (double) location.getBaseStationLatitude() / 14400;
			double lon = (double) location.getBaseStationLongitude() / 14400;
			String latitude = Double.toString(lat);
			String longitude = Double.toString(lon);
			geoLocation.setLatitude(latitude);
			geoLocation.setLongitude(longitude);
			geoLocation.setLocationGoogleName(getLocalByItude(latitude,
					longitude));
		} else {
			AdLogger.e(TAG, "获取不到基站，无法定位");
			mLocationListener.onLocationOk(geoLocation);
		}
	}

	private void gsmLocate(GeographyLocation geoLocation, String mcc,
			String mnc, GsmCellLocation location) {
		if (location != null) {
			AdLogger.i(TAG, "开始进行GSM基站位置解析");
			int cid = location.getCid();
			int lac = location.getLac();
			httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					timeoutConnection);
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			httpclient = new DefaultHttpClient(httpParameters);
			HttpPost post = new HttpPost("http://www.google.com/loc/json");
			try {
				JSONObject holder = new JSONObject();
				holder.put("version", "1.1.0");
				holder.put("host", "maps.google.com");
				holder.put("address_language", "zh_CN");
				holder.put("request_address", true);

				JSONObject tower = new JSONObject();
				tower.put("mobile_country_code", mcc);
				tower.put("mobile_network_code", mnc);
				tower.put("cell_id", cid);
				tower.put("location_area_code", lac);

				JSONArray towerarray = new JSONArray();
				towerarray.put(tower);

				holder.put("cell_towers", towerarray);

				StringEntity query = new StringEntity(holder.toString());
				post.setEntity(query);
				HttpResponse response = httpclient.execute(post);
				HttpEntity entity = response.getEntity();
				BufferedReader buffReader = new BufferedReader(
						new InputStreamReader(entity.getContent()));
				StringBuffer strBuff = new StringBuffer();
				String result = null;
				while ((result = buffReader.readLine()) != null) {
					strBuff.append(result);
				}
				JSONObject json = new JSONObject(strBuff.toString());
				JSONObject subjson = new JSONObject(json.getString("location"));
				String latitude = subjson.getString("latitude");
				String longitude = subjson.getString("longitude");
				geoLocation.setLatitude(latitude);
				geoLocation.setLongitude(longitude);
				geoLocation.setLocationGoogleName(getLocalByItude(latitude,
						longitude));
			} catch (Exception e) {
				AdLogger.e(TAG, "定位时出现异常，无法定位");
			} finally {
				mLocationListener.onLocationOk(geoLocation);
				post.abort();
				httpclient = null;
			}
		} else {
			AdLogger.e(TAG, "获取不到基站，无法定位");
			mLocationListener.onLocationOk(geoLocation);
		}
	}

	private String getLocalByItude(String latitude, String longitude) {
		HttpURLConnection conn = null;
		String line = null;
		try {

			URL url = new URL("http://maps.google.cn/maps/geo?key=abcdefg&q="
					+ latitude + "," + longitude);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(timeoutConnection);
			conn.setReadTimeout(timeoutSocket);
			conn.setRequestMethod("GET");
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufReader = new BufferedReader(inStreamReader);
			StringBuffer bufStr = new StringBuffer();

			while ((line = bufReader.readLine()) != null) {
				bufStr.append(line);
			}
			line = bufStr.toString();
			if (line != null && line.length() > 0) {
				JSONObject jsonobject = new JSONObject(line);
				JSONArray jsonArray = new JSONArray(jsonobject.get("Placemark")
						.toString());
				line = "";
				for (int i = 0; i < jsonArray.length(); i++) {
					line = jsonArray.getJSONObject(i).getString("address");
				}
			}
		} catch (Exception e) {
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return line;
	}

	@Override
	public void locate() {
		new Thread() {

			@Override
			public void run() {
				startToLocate();
				super.run();
			}

		}.start();
	}
}