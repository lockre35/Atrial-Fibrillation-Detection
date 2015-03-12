package com.afib.data;

public class Constants {
	public interface ACTION {
		public static String STARTFOREGROUND_ACTION = "com.afib.data.action.startforeground";
		public static String STOPFOREGROUND_ACTION = "com.afib.data.action.stopforeground";
		public static String MAIN_ACTION = "com.afib.ui.action.GraphActivity";
		public String DEVICE_NAME = "name";
		public String DEVICE_ADDRESS = "address";
	}
 
	public interface NOTIFICATION_ID {
		public static int FOREGROUND_SERVICE = 99999;
	}
}
