
package com.droidlogic.autoreboot;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

public class FileUtils {
	private static final String TAG="reboot";
	private Context mContext;
	private StorageManager mStorageManager;
	public FileUtils(Context cxt){
		mContext=cxt;
		mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
	}
	public String getFileName(String fullpath){
		
		String[] dirs=fullpath.split("/");
		String dir="/";
		for(int i=0;i<dirs.length;i++){
			if(dirs[i]!=null&&dirs[i].length()>1){
				dir+=dirs[i];
				dir+="/";
			}
			if(isStorage(dir))
				break;
		}
		return fullpath.replace(dir, "");
		
	}
	
	public boolean isStorage(String path){
		Class<?> deskInfoClass = null;
		Method isSd=null;
		Method isUsb=null;
		Object info=getDiskInfo(path);
		try {
            deskInfoClass = Class.forName("android.os.storage.DiskInfo");
            isSd = deskInfoClass.getMethod("isSd");
            isUsb = deskInfoClass.getMethod("isUsb");
            Log.d(TAG,"info:"+info);
            if ( info != null ) {
                if ( (Boolean)isSd.invoke(info) ) {
                    return true;
                }else if ( (Boolean)isUsb.invoke(info) ) {
                    return true;
                }else {
                    return false;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
		return false;
	}
	public  String getRecoveryDir(String fullpath){
		Class<?> deskInfoClass = null;
		Method isSd=null;
		Method isUsb=null;
		Object info=getDiskInfo(fullpath);
		String res="";
		if(info==null){
			res+="/cache/";
		}else{
			try {
                deskInfoClass = Class.forName("android.os.storage.DiskInfo");
                isSd = deskInfoClass.getMethod("isSd");
                isUsb = deskInfoClass.getMethod("isUsb");
                if ( info != null ) {
                    if ( (Boolean)isSd.invoke(info) ) {
                        res += "/sdcard/";
                    }else if ( (Boolean)isUsb.invoke(info) ) {
                        res += "/udisk/";
                        Log.d(TAG,"info isUsb:");
                    }else {
                        res += "/cache/";
                    }
                } else {
                    res += "/cache/";
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                res += "/cache/";
            }
		}
		res += getFileName(fullpath);//new File(fullpath).getName();
		return res;
	}
	public String getTransPath(String inPath) {
		String outPath = inPath;
		String pathLast;
		String pathVol;
		int idx = -1;
		int len;
		Class<?> volumeInfoC = null;
		Method getBestVolumeDescription = null;
		Method getVolumes = null;
		Method getType = null;
		Method isMount = null;
		Method getPath = null;
		List<?> volumes = null;
		try {
			volumeInfoC = Class.forName("android.os.storage.VolumeInfo");
			getVolumes = StorageManager.class.getMethod("getVolumes", StorageManager.class);
			volumes = (List) getVolumes.invoke(mStorageManager);
			isMount = volumeInfoC.getMethod("isMountedReadable");
			getType = volumeInfoC.getMethod("getType");
			getPath = volumeInfoC.getMethod("getPath");
			for (Object vol : volumes) {
				if (vol != null && (Boolean) isMount.invoke(vol) && (Integer) getType.invoke(vol) == 0) {
					pathVol = ((File) getPath.invoke(vol)).getAbsolutePath();
					idx = inPath.indexOf(pathVol);
					if (idx != -1) {
						len = pathVol.length();
						pathLast = inPath.substring(idx + len);
						getBestVolumeDescription = StorageManager.class.getMethod("getBestVolumeDescription",
						        volumeInfoC);
						
						outPath = ((String) getBestVolumeDescription.invoke(mStorageManager, vol)) + pathLast;
					}
				}
			}
		} catch (Exception ex) {
		} finally {
			return outPath;
		}
		
	}
	public ArrayList<File> getMainStorageList() {
		Class<?> volumeInfoC = null;
		Method getvolume = null;
		Method isMount = null;
		Method getType = null;
		Method getPath = null;
		List<?> mVolumes = null;
		ArrayList<File> devList = new ArrayList<File>();
		try {
			volumeInfoC = Class.forName("android.os.storage.VolumeInfo");
			getvolume = StorageManager.class.getMethod("getVolumes");
			isMount = volumeInfoC.getMethod("isMountedReadable");
			getType = volumeInfoC.getMethod("getType");
			getPath = volumeInfoC.getMethod("getPathForUser", int.class);
			mVolumes = (List<?>) getvolume.invoke(mStorageManager);
			
			for (Object vol : mVolumes) {
				if (vol != null && (Boolean) isMount.invoke(vol)
				        && ((Integer) getType.invoke(vol) == 0 || (Integer) getType.invoke(vol) == 2)) {
					devList.add((File) getPath.invoke(vol, 0));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return devList;
		}
	}
	public Object getDiskInfo(String filePath) {
		Class<?> volumeInfoC = null;
		Class<?> deskInfoC = null;
		Method getvolume = null;
		Method getDisk = null;
		Method isMount = null;
		Method getPath = null;
		Method getType = null;
		List<?> mVolumes = null;
		try {
			volumeInfoC = Class.forName("android.os.storage.VolumeInfo");
			deskInfoC = Class.forName("android.os.storage.DiskInfo");
			getvolume = StorageManager.class.getMethod("getVolumes");
			mVolumes = (List<?>) getvolume.invoke(mStorageManager);// mStorageManager.getVolumes();
			isMount = volumeInfoC.getMethod("isMountedReadable");
			getDisk = volumeInfoC.getMethod("getDisk");
			getPath = volumeInfoC.getMethod("getPath");
			getType = volumeInfoC.getMethod("getType");
			for (Object vol : mVolumes) {
				if (vol != null && (Boolean) isMount.invoke(vol) && ((Integer) getType.invoke(vol) == 0)) {
					Object info = getDisk.invoke(vol);
					if (info != null && filePath.contains(((File) getPath.invoke(vol)).getAbsolutePath())) {
						return info;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
		
	}
}
