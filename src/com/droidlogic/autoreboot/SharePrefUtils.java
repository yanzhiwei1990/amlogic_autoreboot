package com.droidlogic.autoreboot;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharePrefUtils {
	private Context mContext;
	public static final String SHARE_FILE_NAME="reboot_params";
	
	public static final String Reboot_delay="reboot_delay";
	public static final String Reboot_times="reboot_times";
	public static final String Reboot_select="reboot_select";
	public static final String Update_filepath="update_path";
	public static final String Update_select="update_select";
	public static final String Ping_addr="ping_ipaddr";
	public static final String Ping_times="ping_times";
	
	public static final String Setup="setup";
	public static final String Start="start";
	public static final String Plan_Str="test_plan";
	public static final String StartTime="startTime";
	public static final String StopTime="endTime";
	public static final String RunningTimes="running_times";
	public static final String UpdateTimes="update_times";
	public static final String Check_Reboot="reboot_check";
	public static final String Check_Update="update_check";
	public static final String Check_Ping="ping_check";
       public static final String Check_Single="single_check"; 
       public static final String Check_Loop="loop_check";
	private SharedPreferences mPrefUtil;
	private SharedPreferences.Editor edit;
	public SharePrefUtils(Context cxt){
		mContext=cxt;
		mPrefUtil=mContext.getSharedPreferences(SHARE_FILE_NAME, Context.MODE_PRIVATE);
		edit=mPrefUtil.edit();
	}
	public boolean isSetup(){
		return mPrefUtil.getBoolean(Setup, false);
	}
	public void Setup(){
		edit.putBoolean(Setup, true);
		edit.commit();
	}
	@SuppressLint("NewApi")
	private static String getTime(){
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	public String getStartTime(){
		return mPrefUtil.getString(StartTime, "");
	}
	public String getTestPlan(){
		return mPrefUtil.getString(Plan_Str,"");
	}
	public boolean isRunningPlan(){
		return mPrefUtil.getBoolean(Start, false);
	}
	public void stopRunning(){
             edit.putBoolean(Start, false);
		edit.putString(StopTime, getTime());
		edit.commit();
	}
	public void startRunning(){
		edit.putBoolean(Start, true);
		edit.putString(StartTime, getTime());
		edit.commit();
	}
	public int getDelayTime(){
		return mPrefUtil.getInt(Reboot_delay, AutoRebootActivity.DELAY_TIME);
	}
	public void setDelayTime(int delay){
		edit.putInt(Reboot_delay, delay);
		edit.commit();
	}
	public void setRebootTimes(String times){
		int t=AutoRebootActivity.REBOOT_AFTER;
		try{
			t=Integer.parseInt(times);
		}catch(NumberFormatException ex){
			
		}
		edit.putInt(Reboot_times,t );
		edit.commit();
	}
	public void setUpdateTimes(int value){
		edit.putInt(UpdateTimes, value);
		edit.commit();
	}
	public int getUpdateTimes(){
		return mPrefUtil.getInt(UpdateTimes, 0);
	}
	public int getRebootTimes(){
		return mPrefUtil.getInt(Reboot_times, AutoRebootActivity.REBOOT_AFTER);
	}
	public String getUpdatePath(){
		return mPrefUtil.getString(Update_filepath, "");
	}
	public void setUpdatePath(String path){
		edit.putString(Update_filepath, path);
		edit.commit();
	}
	public int getPingTimes(){
		return mPrefUtil.getInt(Ping_times, 3);
	}
	public String getPingIps(){
		return mPrefUtil.getString(Ping_addr, "www.baidu.com");
	}
	public void setPingIps(String ips){
		edit.putString(Ping_addr, ips);
		edit.commit();
	}
	public void setPingTimes(int tims){
		edit.putInt(Ping_times, tims);
		edit.commit();
	}
	public void setRunningTimes(int times){
		edit.putInt(RunningTimes, times);
		edit.commit();
	}
	public int getRunningTimes(){
		return mPrefUtil.getInt(RunningTimes, 0);
	}
	public boolean isUpdateCheck(){
		return mPrefUtil.getBoolean(Check_Update, false);
	}
	public boolean isPingCheck(){
		return mPrefUtil.getBoolean(Check_Ping, false);
	}
	public boolean isRebootCheck(){
		return mPrefUtil.getBoolean(Check_Reboot, false);
	}

     public boolean isSingleChecked() {
		return mPrefUtil.getBoolean(Check_Single,false);
	}

     public boolean isLoopChecked() {
		return mPrefUtil.getBoolean(Check_Loop,false);
	}
 
	public void setUpdateCheck(boolean check){
		edit.putBoolean(Check_Update, check);
		edit.commit();
	}
	public void setPingCheck(boolean check){
		edit.putBoolean(Check_Ping, check);
		edit.commit();
	}
	public void setRebootCheck(boolean check){
		edit.putBoolean(Check_Reboot, check);
		edit.commit();
	}
       public void setSingleCheck(boolean check){
		edit.putBoolean(Check_Single, check);
		edit.commit();
	} 

        public void setLoopCheck(boolean check){
		edit.putBoolean(Check_Loop, check);
		edit.commit();
	}
        
	public void saveStopTime(){
		edit.putString(StopTime, getTime());
		edit.commit();
	}
	public void clear(){
		edit.clear();
		edit.commit();
	}
	public void savePlan(String plan){
		edit.putString(Plan_Str, plan);
		edit.commit();
	}
	public String ReadPlan(){
		return mPrefUtil.getString(Plan_Str, "");
	}
	public String getStopTime() {
		
		return mPrefUtil.getString(StopTime, "");
	}
	
}
