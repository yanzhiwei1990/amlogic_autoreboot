package com.droidlogic.autoreboot;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class FileUtils_JB {
	private static final String UDISK_JB2 = "/storage/external_storage/sd";
	private static final String SDCARD_JB1 = "/storage/external_storage/sdcard";
    private static final String UDISK_L = "/storage/udisk";
    private static final String SDCARD_L = "/storage/sdcard";
	private static final String TAG = "FileUtils";
	private Context cxt;
	public FileUtils_JB(Context cxt){
		this.cxt = cxt;
	}
	public ArrayList<File> getMainStorageList(){
		ArrayList<File> files=new ArrayList<File>();
		files.add(new File("/sdcard"));
		files.add(new File("/storage"));
		return files;
	}
	/*fullpath transform to /sdcard/ ---/udisk/ */
	public  String getRecoveryDir(String fullpath){
		return getTransPath(fullpath);
	}
	public String getTransPath(String inPath) {
		String ret="";
		String[] strs= inPath.split("/");
		String str="";
		for(int i=0;i<strs.length;i++){
			str+=strs[i];
			str+="/";
			if(isSdcard(str))
				break;
			if(isUdisk(str))
				break;
		}
		Log.d(TAG, "getTrans:"+str);
		if(inPath.contains("sdcard")){
			ret = inPath.replace(str, "/sdcard/");
		}else{
			ret+=inPath.replace(str, "/udisk/"); 
		}
		return ret;
	}
	public static class ZipFileFilter implements FilenameFilter {
        public boolean accept ( File directory, String file ) {
            String dir = directory.getPath();
            if ( new File ( directory, file ).isDirectory() ) {
                return true;
            } else if ( file.toLowerCase().endsWith ( ".zip" ) && isSdcard ( dir ) &&
                        !isUdisk ( dir ) ) {
                return true;
            } else if ( file.toLowerCase().endsWith ( ".zip" ) && isUdisk ( dir ) &&
                        !isSdcard ( dir ) ) {
                return true;
            } else {
                return false;
            }
        }
	}

    public static boolean isSdcard ( String dir ) {
        if ( ( Build.VERSION.SDK_INT > 20 ) && dir.startsWith ( SDCARD_L ) ) {
            return true;
        } else if ( ( Build.VERSION.SDK_INT <= 20 ) &&
                    dir.startsWith ( SDCARD_JB1 ) ) {
            return true;
        }
        return false;
    }

    public static boolean isUdisk ( String dir ) {
//        Log.d ( TAG, "dir:" + dir + " Build.VERSION.SDK_INT == 20" + Build.VERSION.SDK_INT
//                             + " " + dir.startsWith ( UDISK_L ) + " " + dir.contains ( "sdcard" ) );
        if ( ( Build.VERSION.SDK_INT > 20 ) && dir.startsWith ( UDISK_L ) &&
                !dir.contains ( "sdcard" ) ) {
            return true;
        } else if ( ( Build.VERSION.SDK_INT <= 20 ) && dir.startsWith ( UDISK_JB2 ) &&
                    !dir.contains ( "sdcard" ) ) {
            return true;
        }
        return false;
    }
    
}
