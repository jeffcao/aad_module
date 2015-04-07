package com.g.sc;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PermissionChecker {

	private static final String TAG = PermissionChecker.class.toString();

	/**
	 * NameNotFoundException: 当getPackageInfo函数中传入的包名没有在系统中安装时会抛出此异常
	 * 
	 * @param context
	 * @param neededPermissions
	 * @return
	 * @throws NameNotFoundException
	 */
	public static boolean isAllPermissionObtained(Context context,
			String[] neededPermissions) throws NameNotFoundException {
		boolean result = true;
		PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
				context.getPackageName(), PackageManager.GET_PERMISSIONS);
		String permissions[] = packageInfo.requestedPermissions;
		if (permissions != null) {
			List<String> permissionList = new ArrayList<String>();
			for (String permission : permissions) {
				permissionList.add(permission);
			}
			for (String str : neededPermissions) {
				if (!permissionList.contains(str)) {
					AdLogger.w(TAG, "权限:" + str + "尚未获得，程序无法正常运行");
					result = false;
					break;
				}
			}
		} else {
			AdLogger.w(TAG, "所需权限：");
			for (String str : neededPermissions) {
				AdLogger.w(TAG, "       " + str);
			}
			AdLogger.w(TAG, "尚未获得，程序无法正常运行");
			result = false;
		}
		return result;
	}
}
