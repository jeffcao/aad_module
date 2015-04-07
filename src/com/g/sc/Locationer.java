package com.g.sc;

import android.content.Context;

public abstract class Locationer {
	
	protected Context mContext;
	protected OnLocationOkListener mLocationListener = new OnLocationOkListener() {
		
		@Override
		public void onLocationOk(GeographyLocation location) {
			
		}
	};
	
	public Locationer(Context context) {
		mContext = context;
	}
	
	interface OnLocationOkListener {
		void onLocationOk(GeographyLocation location);
	}
	
	public void setOnLocationOkListener(OnLocationOkListener listener) {
		mLocationListener = listener;
	}
	
	public abstract void locate();
}
