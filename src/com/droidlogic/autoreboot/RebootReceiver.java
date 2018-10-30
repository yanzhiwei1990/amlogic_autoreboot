package com.droidlogic.autoreboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {
	private static final String TAG="RebootReceiver";
	@Override
	public void onReceive(Context cxt, Intent intent) {
		Log.d(TAG, "boot complete");
		if(intent!=null&&intent.getAction()==Intent.ACTION_BOOT_COMPLETED){
		    LoggerWriter mLoggerWriter = new LoggerWriter(cxt);
			mLoggerWriter.Log("boot complete, then start AutoRebootActivity");
            mLoggerWriter.closeLog();
			Intent act=new Intent();
			act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			act.setClass(cxt, AutoRebootActivity.class);
			cxt.startActivity(act);
		}
	}
}
