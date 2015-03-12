package com.afib.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Base activity that fragments are started from.  This activity
 * is the main activity for the application.
 * 
 * @author Logan
 */
public class MainActivity extends Activity {
		
	/**
	 * Called when the activity is launched or the app process has been killed.  
	 * More information about the activity life cycle can be found here, 
	 * <a href="http://developer.android.com/reference/android/app/Activity.html">.
	 * 
	 * @param savedInstanceState used to recreate an existing activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
      
		//If there is no existing activity, we load the default fragment
		if(savedInstanceState == null) 
		{
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			//Load the initial home fragment
			HomeFragment homeFragment = new HomeFragment();
			fragmentTransaction.replace(android.R.id.content, homeFragment);
			
			fragmentTransaction.commit();
		}
	}
    
}
