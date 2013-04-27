package cn.ingenic.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WeatherReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		klilog.i("Weather Receiver received action:"+action);
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
			
		}else if(EngineManager.ACTION_UPDATE_WEATHER.equals(action)){
			EngineManager.getInstance(context).refreshWeather();
		}else if(EngineManager.ACTION_NOTIFY_WEATHER.equals(action)){
			EngineManager.getInstance(context).checkNotifyWeather();
		}
	}

}
