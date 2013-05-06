package cn.ingenic.weather;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Message;
import cn.ingenic.weather.engine.City;
import cn.ingenic.weather.engine.Weather;
import cn.ingenic.weather.engine.WeatherEngine;

public class EngineManager {
	//actions
	public final static String ACTION_UPDATE_WEATHER = "cn.indroid.action.updateweather";
	public final static String ACTION_NOTIFY_WEATHER = "cn.indroid.action.notifyweather";
	
	//shared prefs
	private final static String ENGINE_MANAGER = "engine_manager";
	private final static String KEY_ENGINE_SOURCE = "engine_source";
	private final static String KEY_UPDATE_TIME = "update_time";
	private final static String KEY_UPDATE_DURING = "update_during";
	
	private final static int NOTIFY_HOUR = 6;
	
	private static EngineManager sInstance;
	private Context mContext;
	private WeatherEngine mEngine;
	private CacheManager mCache;
	private List<DataChangedListener> mListeners = new ArrayList<DataChangedListener>();
	
	private boolean isRequesting = false;
	
	private EngineManager(Context context){
		mContext = context;
		mCache = new CacheManager(mContext);
		mEngine = WeatherEngine.getInstance(mContext);
		initNotifyAlarm();
	}
	
	public static EngineManager getInstance(Context context){
		if(sInstance == null){
			sInstance = new EngineManager(context);
		}
		return sInstance;
	}
	
	public interface DataChangedListener{
		void onWeatherChanged(City city);
		void onStateChanged(boolean isRequesting);
	}
	
	public void register(DataChangedListener listener){
		mListeners.add(listener);
		listener.onStateChanged(isRequesting);
	}
	
	public void unRegister(DataChangedListener listener){
		mListeners.remove(listener);
	}
	
	public void init(final Message callback){
		new Thread(){
			@Override
			public void run() {
				super.run();
				mEngine.init(getEngineSource());
				callback.sendToTarget();
			}
		}.start();
	}
	
	public void changeEngineSource(final String source, final Message callback){
		new Thread(){
			@Override
			public void run() {
				super.run();
				callback.arg1 = mEngine.init(source);
				setEngineSource(source);
				callback.sendToTarget();
			}
		}.start();
		
	}
	
	public String[] getSourceList(){
		return mEngine.getSourceList();
	}
	
	public boolean isRequesting(){
		return isRequesting;
	}
	
	/**
	 * Must be called in thread.
	 * Get city weather from internet.
	 */
	synchronized public void getWeatherByIndex(final String index){
		isRequesting = true;
		for(DataChangedListener listener : mListeners){
			listener.onStateChanged(isRequesting);
		}
		new Thread(){
			@Override
			public void run() {
				super.run();
				Looper.prepare();
				City city = mEngine.getWeatherByIndex(index);

				changeUpdateTime(System.currentTimeMillis());
				
				if(city != null && city.weather != null){
					//cache weather
					mCache.cacheWeather(city);
				}
				isRequesting = false;
				for(DataChangedListener listener : mListeners){
					listener.onWeatherChanged(city);
					listener.onStateChanged(isRequesting);
				}
				Looper.loop();
			}
		}.start();
	}
	
	public List<City> getCityList(City city){
		return mEngine.getCityList(city);
	}

	public void setDefaultMarkCity(City city){
		mCache.markDefaultCity(city, getEngineSource());
	}
	
	public boolean hasDefaultCity(){
		return mCache.hasDefaultCity();
	}
	
	public City getDefaultMarkCity(){
		
		try {
			return mCache.getDefaultCity();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
		
	}
	
	public void checkNotifyWeather(){
		City city = getDefaultMarkCity();
        if(city == null){
            return;
        }else if(city.weather == null){
			this.register(new DataChangedListener(){

				@Override
				public void onWeatherChanged(City city) {
					if(city.weather != null){
						checkNotifyWeather();
					}
					EngineManager.this.unRegister(this);
				}

				@Override
				public void onStateChanged(boolean isRequesting) {
					
				}
			});
            return;
		}
		Weather weather = city.weather.get(0);
		if(WeatherUtils.isBadWeather(weather)){
			//TODO:notify lock screen
		}
	}
	
	public void refreshWeather(){
		City city = getDefaultMarkCity();
		if(city != null){
			getWeatherByIndex(city.index);
		}
	}
	
	private void changeUpdateTime(long time){
		setUpdateTime(time);
		freshAlarmTime();
	}
	
	private void freshAlarmTime() {
		AlarmManager am = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_UPDATE_WEATHER);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		int interval = getUpdateDuring();
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, sender);
	}
	
	private void initNotifyAlarm(){
		AlarmManager am = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_NOTIFY_WEATHER);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar cal = Calendar.getInstance();
		Calendar notifyToday = Calendar.getInstance();
		notifyToday.set(Calendar.HOUR_OF_DAY, NOTIFY_HOUR);
		notifyToday.set(Calendar.MINUTE, 0);
		notifyToday.set(Calendar.SECOND, 0);
		notifyToday.set(Calendar.MILLISECOND, 0);
		if(cal.after(notifyToday)){
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		int interval = 1000*60*60*24;
		am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, sender);
	}

	private int getUpdateDuring(){
		SharedPreferences prefs = mContext.getSharedPreferences(ENGINE_MANAGER, Context.MODE_PRIVATE);
		return prefs.getInt(KEY_UPDATE_DURING, 4 * 60 * 60 * 1000);
	}
	
	public long getUpdateDuringHour(){
		return getUpdateDuring()/1000/60/60;
	}
	
	public void setUpdateDuring(int hour){
		hour = hour * 60 * 60 * 1000;
		SharedPreferences prefs = mContext.getSharedPreferences(ENGINE_MANAGER, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(KEY_UPDATE_DURING, hour);
		editor.commit();
	}
	
	private long getUpdateTime(){
		SharedPreferences prefs = mContext.getSharedPreferences(ENGINE_MANAGER, Context.MODE_PRIVATE);
		return prefs.getLong(KEY_UPDATE_TIME, 0);
	}
	
	private void setUpdateTime(long time){
		SharedPreferences prefs = mContext.getSharedPreferences(ENGINE_MANAGER, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(KEY_UPDATE_TIME, time);
		editor.commit();
	}
	
	private void setEngineSource(String source){
		SharedPreferences prefs = mContext.getSharedPreferences(ENGINE_MANAGER, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(KEY_ENGINE_SOURCE, source);
		editor.commit();
	}
	
	private String getEngineSource(){
		SharedPreferences prefs = mContext.getSharedPreferences(ENGINE_MANAGER, Context.MODE_PRIVATE);
		return prefs.getString(KEY_ENGINE_SOURCE, null);
	}
}
