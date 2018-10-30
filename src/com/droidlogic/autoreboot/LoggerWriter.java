package com.droidlogic.autoreboot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;

public class LoggerWriter {
	private static final String FileName="cache-log";
	private FileOutputStream fos;
	private OutputStreamWriter writer;
	public LoggerWriter(Context cxt){
		File file = new File(FileName);
		if(file.exists())
			//file.delete();
             //execute after reboot rather than back
		try{
		fos = cxt.openFileOutput(FileName,Context.MODE_PRIVATE);
	      writer = new OutputStreamWriter(fos);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void Log(String log){
		try {
			WriteLog(log);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void WriteLog(String log) throws IOException{
        if (writer != null) {
            writer.write(getTime() + "-->" + System.currentTimeMillis()+"::"+log+"\n");
		    writer.flush();  
        }
		if (fos != null) {
           fos.flush(); 
        }
	}
	public void closeLog(){
		try{
        	    if(writer!=null)
        		writer.close();
		    if(fos!=null)
			fos.close();
		}catch(Exception ex){ex.printStackTrace();}
		
	}

    private String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        formatter.setTimeZone(TimeZone.getDefault());
        Date curDate = new Date();
        String currentTime = formatter.format(curDate);
        return currentTime;
    }
}
