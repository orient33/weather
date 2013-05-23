package cn.ingenic.weather;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetViewBuilder {
	private final static boolean HOUR_24 = false;
	
	private int mLayout;
	private Context mContext;
	private Context mRemoteContext;
	private RemoteViews mRv;
	private int[] mNumRes;
	private Bundle mWeatherData;
	private boolean mUpdateTime;
	
	WidgetViewBuilder(Context context){
		mContext = context;
	}
	
	public void setRemoteContext(Context context){
		mRemoteContext = context;
	}
	
	public void setLayout(int layout){
		mLayout = layout;
	}
	
	public void setNumRes(int[] res){
		mNumRes = res;
	}
	
	public void setUpdateTime(boolean update){
		mUpdateTime = update;
	}
	
	public void setWeather(Bundle data){
		mWeatherData = data;
	}
	
	public RemoteViews build(){
		if(mLayout == 0 || mNumRes == null){
			return null;
		}
		
		if(mRemoteContext == null){
			buildFromDefault();
		}else{
			buildFromRemote();
		}
		
		return mRv;
	}
	
	private void buildFromRemote(){

		String pkg = mRemoteContext.getPackageName();

		RemoteResUtils utils = new RemoteResUtils(mContext, mRemoteContext);
		mRv = new RemoteViews(pkg, mLayout);
		
		if(mWeatherData != null){
			mRv.setTextViewText(R.id.tv_city, mWeatherData.getString("city"));
			mRv.setTextViewText(R.id.tv_weather, mWeatherData.getString("weather"));
			mRv.setTextViewText(R.id.tv_current, mWeatherData.getString("current_temp"));
			mRv.setTextViewText(R.id.tv_min, mWeatherData.getString("min_temp"));
			mRv.setTextViewText(R.id.tv_max, mWeatherData.getString("max_temp"));
			int icon = mWeatherData.getInt("icon", 0);
			if(icon != 0){
				mRv.setImageViewResource(R.id.iv_icon, icon);
			}
		}
		
		if(mUpdateTime){
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minite = c.get(Calendar.MINUTE);
			if(HOUR_24){
				mRv.setImageViewResource(R.id.iv_time_1, mNumRes[hour/10]);
				mRv.setImageViewResource(R.id.iv_time_2, mNumRes[hour%10]);
				mRv.setViewVisibility(R.id.iv_time_apm, View.GONE);
			}else{
				mRv.setImageViewResource(R.id.iv_time_1, hour > 12 ? mNumRes[(hour - 12)/10] : mNumRes[hour/10]);
				mRv.setImageViewResource(R.id.iv_time_2, hour > 12 ? mNumRes[(hour - 12)%10] : mNumRes[hour%10]);
				mRv.setViewVisibility(R.id.iv_time_apm, View.VISIBLE);
				mRv.setImageViewResource(R.id.iv_time_apm, hour > 12 ? mNumRes[11] : mNumRes[10]);
			}
			mRv.setImageViewResource(R.id.iv_time_3, mNumRes[minite/10]);
			mRv.setImageViewResource(R.id.iv_time_4, mNumRes[minite%10]);
			
			int i = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			String[] days;
			try {
				days = mRemoteContext.getResources().getStringArray(R.array.weekday);
			} catch (NotFoundException e) {
				days = mContext.getResources().getStringArray(R.array.weekday);
			}
			mRv.setTextViewText(R.id.tv_week, days[i-1]);

			String dateFormat;
			try {
				dateFormat = mRemoteContext.getString(R.string.date_format);
			} catch (Exception e) {
				dateFormat = mContext.getString(R.string.date_format);
			}
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			mRv.setTextViewText(R.id.tv_date, sdf.format(new Date()));
			
		}
		
		/*
		if(mWeatherData != null){
			mRv.setTextViewText(utils.getResId("tv_city"), mWeatherData.getString("city"));
			mRv.setTextViewText(utils.getResId("tv_weather"), mWeatherData.getString("weather"));
			mRv.setTextViewText(utils.getResId("tv_current"), mWeatherData.getString("current_temp"));
			mRv.setTextViewText(utils.getResId("tv_min"), mWeatherData.getString("min_temp"));
			mRv.setTextViewText(utils.getResId("tv_max"), mWeatherData.getString("max_temp"));
			int icon = mWeatherData.getInt("icon", 0);
			if(icon != 0){
				mRv.setImageViewResource(utils.getResId("iv_icon"), icon);
			}
		}
		
		if(mUpdateTime){
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minite = c.get(Calendar.MINUTE);
			if(HOUR_24){
				mRv.setImageViewResource(utils.getResId("iv_time_1"), mNumRes[hour/10]);
				mRv.setImageViewResource(utils.getResId("iv_time_2"), mNumRes[hour%10]);
				mRv.setViewVisibility(utils.getResId("iv_time_apm"), View.GONE);
			}else{
				mRv.setImageViewResource(utils.getResId("iv_time_1"), hour > 12 ? mNumRes[(hour - 12)/10] : mNumRes[hour/10]);
				mRv.setImageViewResource(utils.getResId("iv_time_2"), hour > 12 ? mNumRes[(hour - 12)%10] : mNumRes[hour%10]);
				mRv.setViewVisibility(utils.getResId("iv_time_apm"), View.VISIBLE);
				mRv.setImageViewResource(utils.getResId("iv_time_apm"), hour > 12 ? mNumRes[11] : mNumRes[10]);
			}
			mRv.setImageViewResource(utils.getResId("iv_time_3"), mNumRes[minite/10]);
			mRv.setImageViewResource(utils.getResId("iv_time_4"), mNumRes[minite%10]);
			
			int i = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			String[] days;
			try {
				days = mRemoteContext.getResources().getStringArray(utils.getResId("weekday"));
			} catch (NotFoundException e) {
				days = mContext.getResources().getStringArray(R.array.weekday);
			}
			mRv.setTextViewText(R.id.tv_week, days[i-1]);

			String dateFormat;
			try {
				dateFormat = mRemoteContext.getString(utils.getResId("date_format"));
			} catch (Exception e) {
				dateFormat = mContext.getString(R.string.date_format);
			}
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			mRv.setTextViewText(utils.getResId("tv_date"), sdf.format(new Date()));
			
		}*/
	}
	
	private void buildFromDefault(){
		
		mRv = new RemoteViews(mContext.getPackageName(), mLayout);
		
		if(mWeatherData != null){
			mRv.setTextViewText(R.id.tv_city, mWeatherData.getString("city"));
			mRv.setTextViewText(R.id.tv_weather, mWeatherData.getString("weather"));
			mRv.setTextViewText(R.id.tv_current, mWeatherData.getString("current_temp"));
			mRv.setTextViewText(R.id.tv_min, mWeatherData.getString("min_temp"));
			mRv.setTextViewText(R.id.tv_max, mWeatherData.getString("max_temp"));
			int icon = mWeatherData.getInt("icon", 0);
			if(icon != 0){
				mRv.setImageViewResource(R.id.iv_icon, icon);
			}
		}
		
		if(mUpdateTime){
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minite = c.get(Calendar.MINUTE);
			if(HOUR_24){
				mRv.setImageViewResource(R.id.iv_time_1, mNumRes[hour/10]);
				mRv.setImageViewResource(R.id.iv_time_2, mNumRes[hour%10]);
				mRv.setViewVisibility(R.id.iv_time_apm, View.GONE);
			}else{
				mRv.setImageViewResource(R.id.iv_time_1, hour > 12 ? mNumRes[(hour - 12)/10] : mNumRes[hour/10]);
				mRv.setImageViewResource(R.id.iv_time_2, hour > 12 ? mNumRes[(hour - 12)%10] : mNumRes[hour%10]);
				mRv.setViewVisibility(R.id.iv_time_apm, View.VISIBLE);
				mRv.setImageViewResource(R.id.iv_time_apm, hour > 12 ? mNumRes[11] : mNumRes[10]);
			}
			mRv.setImageViewResource(R.id.iv_time_3, mNumRes[minite/10]);
			mRv.setImageViewResource(R.id.iv_time_4, mNumRes[minite%10]);
			
			int i = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			String[] days = mContext.getResources().getStringArray(R.array.weekday);
			mRv.setTextViewText(R.id.tv_week, days[i-1]);

			String dateFormat = mContext.getString(R.string.date_format);
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			mRv.setTextViewText(R.id.tv_date, sdf.format(new Date()));
			
		}
	}
	
	class RemoteResUtils{
		private Context mContext;
		private Context mRemoteContext;
		RemoteResUtils(Context context, Context remote){
			mContext = context;
			mRemoteContext = remote;
		}
		
		int getResId(String resName){
			return getResIdByName(mContext, mRemoteContext, resName);
		}
		
		private int getResIdByName(Context context, Context remoteContext, String resName){
			try {
		        Class<?> c = remoteContext.getClassLoader().loadClass(remoteContext.getPackageName()+".R");
		        Class<?>[] cl = c.getClasses();
		        int b =0;
		        for (int i = 0; i < cl.length; i++) {
		                Log.d("TAG", cl[i].getSimpleName());
		                Field field[] = cl[i].getFields();
		                for (int j = 0; j < field.length; j++) {
		                        Log.d("TAG", "NAME:"+field[j].getName()+"--VALUE:"+field[j].getInt(field[j].getName()));
		                        if(field[j].getName().equals(resName)) {
		                                b = field[j].getInt(field[j].getName());
		                                Log.d("TAG", "--------id");
		                        }
		                };
		        }
				return b;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
	}
	
	
}
