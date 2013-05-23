package cn.ingenic.weather;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.Button;
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
		WidgetViewBuilder builder = getDefaultBuilder(context);
		builder.setWeather(bundle);
		notifyWidget(context, builder.build());
	}
	
	private void updateWidgetTime(Context context){
		klilog.i("updateWidgetWeather");
		WidgetViewBuilder builder = getDefaultBuilder(context);
		builder.setUpdateTime(true);
		notifyWidget(context, builder.build());
		setWidgetUpdateTime(context);
	}
	
	private void notifyWidget(Context context, RemoteViews rv){
		if(rv == null){
			return;
		}
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
	
	private PendingIntent getUpdateTimePendingIntent(Context context){
		Intent intent = new Intent(ACTION_UPDATE_TIME);
		return  PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	private WidgetViewBuilder getDefaultBuilder(Context context){
		WidgetViewBuilder builder = new WidgetViewBuilder(context);
		builder.setLayout(R.layout.widget_layout);
		int[] numRes = {R.drawable.n0, R.drawable.n1, R.drawable.n2, 
				R.drawable.n3, R.drawable.n4, R.drawable.n5, R.drawable.n6, 
				R.drawable.n7, R.drawable.n8, R.drawable.n9, 
				R.drawable.am, R.drawable.pm};
		builder.setNumRes(numRes);
		return builder;
	}
	
	
	private WidgetViewBuilder getRemoteSkin(Context context) {
		try {
			Context remoteContext = context.createPackageContext("cn.ingenic.weather.skin.styleyu",
					Context.CONTEXT_IGNORE_SECURITY|Context.CONTEXT_INCLUDE_CODE);
			
			WidgetViewBuilder builder = new WidgetViewBuilder(context);
			builder.setRemoteContext(remoteContext);
			builder.setLayout(R.layout.widget_layout);
			
			int[] numRes = { R.drawable.n0, R.drawable.n1, R.drawable.n2,
					R.drawable.n3, R.drawable.n4, R.drawable.n5, R.drawable.n6,
					R.drawable.n7, R.drawable.n8, R.drawable.n9, R.drawable.am,
					R.drawable.pm };
			builder.setNumRes(numRes);
			return builder;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
