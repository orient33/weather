package cn.ingenic.weather;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	private final static String ACTION_UPDATE_TIME = "cn.kli.weatherwidget.update_time";
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		klilog.i("onEnabled");
		updateWidgetTime(context);
//		setWidgetUpdateTime(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		klilog.i("onReceive");
		String action = intent.getAction();
		klilog.i("WidgetProvider received:"+action);
		if(ACTION_UPDATE_TIME.equals(action)){
			updateWidgetTime(context);
		}else if(EngineManager.ACTION_FRESH_WIDGET.equals(action)){
			updateWidgetWeather(context, intent.getExtras());
		}
	}
	

	@Override
	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);
		klilog.i("onAppWidgetOptionsChanged");
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		klilog.i("onDeleted");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		klilog.i("onUpdate");
		EngineManager.getInstance(context).updateWidget();
		updateWidgetTime(context);
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		klilog.i("onDisabled");
//		removeWidgetUpdateTime(context);
	}
	
	private void updateWidgetWeather(Context context, Bundle bundle){
		klilog.i("updateWidgetWeather");
		//prepare data to display
		RemoteViews rv = getRemoteView(context);
		if(bundle != null){
			rv.setTextViewText(R.id.tv_city, bundle.getString("city"));
			rv.setTextViewText(R.id.tv_weather, bundle.getString("weather"));
			rv.setTextViewText(R.id.tv_current, bundle.getString("current_temp"));
			rv.setTextViewText(R.id.tv_maxmin, bundle.getString("min_temp")+"|"+
					bundle.getString("max_temp"));
			int icon = bundle.getInt("icon", 0);
			if(icon != 0){
				rv.setImageViewResource(R.id.iv_icon, icon);
			}
		}

		notifyWidget(context, rv);
	}

	private void updateWidgetTime(Context context){
		klilog.i("updateWidgetWeather");
		//prepare data to display
		RemoteViews rv = getRemoteView(context);
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minite = c.get(Calendar.MINUTE);
		rv.setImageViewResource(R.id.iv_time_1, getNumPic(hour/10));
		rv.setImageViewResource(R.id.iv_time_2, getNumPic(hour%10));
		rv.setImageViewResource(R.id.iv_time_3, getNumPic(minite/10));
		rv.setImageViewResource(R.id.iv_time_4, getNumPic(minite%10));
		notifyWidget(context, rv);
		setWidgetUpdateTime(context);
	}
	
	private int getNumPic(int num){
		int res = 0;
		switch(num){
		case 0:
			res = R.drawable.widget_num_0;
			break;
		case 1:
			res = R.drawable.widget_num_1;
			break;
		case 2:
			res = R.drawable.widget_num_2;
			break;
		case 3:
			res = R.drawable.widget_num_3;
			break;
		case 4:
			res = R.drawable.widget_num_4;
			break;
		case 5:
			res = R.drawable.widget_num_5;
			break;
		case 6:
			res = R.drawable.widget_num_6;
			break;
		case 7:
			res = R.drawable.widget_num_7;
			break;
		case 8:
			res = R.drawable.widget_num_8;
			break;
		case 9:
			res = R.drawable.widget_num_9;
			break;
		}
		return res;
	}
	
	private void notifyWidget(Context context, RemoteViews rv){
		//update widget
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
		int[] appIds = awm.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
		awm.updateAppWidget(appIds, rv);
	}
	
	private void setWidgetUpdateTime(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = getUpdateTimePendingIntent(context);
		int interval = 1000 * 60;

		Calendar notify = Calendar.getInstance();
//		notify.add(Calendar.HOUR_OF_DAY, 1);
		notify.add(Calendar.MINUTE, 1);
		notify.set(Calendar.SECOND, 0);
		notify.set(Calendar.MILLISECOND, 0);
		
		am.setRepeating(AlarmManager.RTC_WAKEUP, notify.getTimeInMillis(), interval, sender);
	}
	
	private void removeWidgetUpdateTime(Context context){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = getUpdateTimePendingIntent(context);
		am.cancel(pi);
	}
	
	private PendingIntent getUpdateTimePendingIntent(Context context){
		Intent intent = new Intent(ACTION_UPDATE_TIME);
		return  PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	private RemoteViews getRemoteView(Context context){
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_time_weather);
		return rv;
	}
}
