package com.afib.tests;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;

import com.afib.ui.FindDeviceFragment;
import com.afib.ui.GraphFragment;
import com.afib.ui.MainActivity;
import com.afib.ui.R;

/**
 * Created by Logan on 4/28/2015.
 */
public class UI_MainActivity_Tests extends ActivityInstrumentationTestCase2<MainActivity> {

    Intent mLaunchIntent;
    Activity mActivity;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    public UI_MainActivity_Tests() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        button1 = (Button)mActivity.findViewById(R.id.GraphActivityButton);
        button2 = (Button)mActivity.findViewById(R.id.Button02);
        button3 = (Button)mActivity.findViewById(R.id.Button03);
        button4 = (Button)mActivity.findViewById(R.id.Button04);
    }

    @SmallTest
    public void testPreconditions() {
        assertNotNull("Activity is null", mActivity);
        assertNotNull("Graph button is null", button1);
        assertNotNull("FindDevice button is null", button2);
        assertNotNull("Instructions button is null", button3);
        assertNotNull("Analyze button is null", button4);
    }

    @MediumTest
    public void testGraphStart() {
        TouchUtils.clickView(this, button1);
        GraphFragment dialog = (GraphFragment) mActivity.getFragmentManager().findFragmentByTag("GraphFragment");
        assertNotNull("GraphFragment is null", dialog);
    }

//    @MediumTest
//    public void testFindDeviceStart() {
//        TouchUtils.clickView(this, button2);
//        FindDeviceFragment dialog = (FindDeviceFragment) mActivity.getFragmentManager().findFragmentByTag("test");
//        assertNotNull("FindDeviceFragment is null", dialog);
//    }
}
