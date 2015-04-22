package com.afib.communication;

public class Constants {
	public interface ACTION {
		public static String STARTFOREGROUND_ACTION = "com.afib.data.action.startforeground";
		public static String STOPFOREGROUND_ACTION = "com.afib.data.action.stopforeground";
		public static String MAIN_ACTION = "com.afib.ui.action.GraphActivity";
		public String DEVICE_NAME = "name";
		public String DEVICE_ADDRESS = "address";
        public static String STREAM_DATA = "STREAM_DATA";
        public static String OUTPUT_FILENAME = "Output_File_Name";
        public static String INPUT_FILENAME = "Input_File_Name";
        public static int INPUT_BLOCK_SIZE = 100;
	}
 
	public interface NOTIFICATION_ID {
		public static int FOREGROUND_SERVICE = 99999;
	}
}
