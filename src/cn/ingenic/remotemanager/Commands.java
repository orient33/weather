package cn.ingenic.remotemanager;

public class Commands {
	public static final String TAG="sw2df";
	//sync
	public final static String ACTION_SYNC = "cn.ingenic.action.sync";
	
	//weather
	public final static String ACTION_WEATHER = "cn.ingenic.action.weather";
	public final static int CMD_INTERNET_REQUEST = 1;
	/* first bind between watch and phone*/
	public final static String ACTION_BIND = "cn.ingenic.action.weather.first.bind";

	//device manager
	public final static String ACTION_DEVICE_MANAGER = "cn.ingenic.action.devicemanager";
	public final static int CMD_LOCK_SCREEN = 2;
	
	//call log
	public final static String ACTION_CALL_LOG= "cn.ingenic.action.calllog";
	public final static int CMD_SYNC_CALL_LOG = 5;
	
	public final static int CMD_BUILD_CONNECTION = 3;
	public final static int CMD_UNSOL_RESPONSE = 4;
}
