package com.afib.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends Activity {

	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);

	      Configuration config = getResources().getConfiguration();

	      FragmentManager fragmentManager = getFragmentManager();
	      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

         //Load the initial home fragment
         HomeFragment homeFragment = new HomeFragment();
         fragmentTransaction.replace(android.R.id.content, homeFragment);

	      
	      fragmentTransaction.commit();
	   }
	    
	}
