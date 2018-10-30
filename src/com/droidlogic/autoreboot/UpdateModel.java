package com.droidlogic.autoreboot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.os.Build.VERSION;
import android.util.Log;
import android.content.Context;
import android.os.PowerManager;

public class UpdateModel {
	private String updateFile="";
	private Context mContext;
	private SharePrefUtils mpref;
	private boolean updateCheck=false;
	public UpdateModel(Context cxt){
		mContext=cxt;
		mpref=new SharePrefUtils(cxt);
		readParam();
	}
	public String getPath(){
		return updateFile;
	}
	public void setFilePath(String file){
		FileUtils util=new FileUtils(mContext);
		if(VERSION.SDK_INT>=23){
			/*android-n*/
			updateFile = util.getRecoveryDir(file);
		}else{
			FileUtils_JB util_jb=new FileUtils_JB(mContext);
			updateFile=util_jb.getRecoveryDir(file);
		}
		//swith(System)--->getPath
		//updateFile = file;
		
	}
	public String createPlan(int times){
		if(updateCheck)
		return "after "+times+" will update "+updateFile+" \n";
		return "";
	}
	public void saveParam(String file,boolean isCheck){
		updateCheck=isCheck;
		updateFile=file;
		mpref.setUpdatePath(file);
		mpref.setUpdateCheck(isCheck);
		updateFile=file;
	}
	public void readParam(){
		updateFile=mpref.getUpdatePath();
		updateCheck=mpref.isUpdateCheck();
	}

	private void save(FileOutputStream fos){
		try {
			Class<?> fileUtil=Class.forName("android.os.FileUtils");
			Method syn=fileUtil.getMethod("sync", FileOutputStream.class);
			syn.invoke(fileUtil, fos);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getUpdateFile() {
		return updateFile;
	}
	public boolean isUpdateCheck() {
		return updateCheck;
	}
	public boolean execPlan(){
		File cacheDir=new File("/cache/recovery");
		File cacheCommand=new File("/cache/recovery/command");
		String arg="--update_package="+updateFile;
		Log.d("reboot", "setup Update:"+arg);
		try {
			cacheDir.mkdirs();
			cacheCommand.createNewFile();
			FileWriter command = new FileWriter(cacheCommand);
			FileOutputStream fos = new FileOutputStream(cacheCommand);
	        try {
	            command.write(arg);
	            command.write("\n");
	        } finally {
	            command.close();
	            save(fos);
	        }
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		PowerManager mPm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mPm.reboot("update");
		return true;
	}
}
