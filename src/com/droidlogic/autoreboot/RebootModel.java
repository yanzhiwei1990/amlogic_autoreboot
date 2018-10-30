package com.droidlogic.autoreboot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;

public class RebootModel {
	private Context mContext;
	private int mDelayTime;
	private boolean addToPlan;
       private boolean isLoop;
       private boolean isSingle;
	private int mRebootTimes;
	private SharePrefUtils mpref;
	public RebootModel(Context cxt){
		mContext=cxt;
		mpref=new SharePrefUtils(cxt);
		readRebootParam();
	}
	
	public void saveRebootParam(String delay,String times,boolean check,boolean single){
		setDelayTime(delay);
		mpref.setDelayTime(mDelayTime);
		setTimes(times);
		mpref.setRebootTimes(times);
		addToPlan=check;
             //isLoop=loop; 
             isSingle=single;
		mpref.setRebootCheck(addToPlan);
             //mpref.setLoopCheck(isLoop); 
             mpref.setSingleCheck(isSingle);
	}
	
	private void setTimes(String times){
		int t=AutoRebootActivity.REBOOT_AFTER;
		try{
			t=Integer.parseInt(times);
			mRebootTimes=t;
		}catch(NumberFormatException ex){
			//ex.printStackTrace();
		}
	}
	public void readRebootParam(){
		addToPlan=mpref.isPingCheck();
		mDelayTime=mpref.getDelayTime();
		mRebootTimes=mpref.getRebootTimes();
	}
	public String createPlan(){
		if(addToPlan){
			return "Reboot after:"+mDelayTime+" seconds\n";
		}else{
			return "";
		}
	}
	public void setDelayTime(String times){
		int t=AutoRebootActivity.DELAY_TIME;
		try{
			t=Integer.parseInt(times);
			mDelayTime=t;
		}catch(NumberFormatException ex){
			//ex.printStackTrace();
		}
	}
	public void rebootNow(){
		PowerManager mPm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mPm.reboot("normal_reboot");
	}

	public int getmDelayTime() {
		return mDelayTime;
	}
	public boolean isAddToPlan() {
		return addToPlan;
	}

	public int getmRebootTimes() {
		return mRebootTimes;
	}
}
