package cn.ingenic.weather;

import cn.ingenic.remotemanager.Commands;
import cn.ingenic.weather.engine.City;
import weathersource.webxml.com.cn.InternetAccess;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class CommandReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(Commands.ACTION_BIND.equals(intent.getAction())){
			EngineManager em=EngineManager.getInstance(context);
			City city = em.getDefaultMarkCity();
			if(city!=null)
				em.getWeatherByIndex(city.index);
			else
				em.getWeatherByIndex("792");// 792 ,also beijing, is default city
		    Log.i(Commands.TAG,"[first bind]receive: "+Commands.ACTION_BIND+";"+city);
			return;
		}
		
		Bundle bundle = intent.getExtras();
		InternetAccess.onInternetResponse(bundle.getInt("cmd"), bundle.getString("data"));
		Long.valueOf("1111");
		SystemClock.currentThreadTimeMillis();
	}

}
