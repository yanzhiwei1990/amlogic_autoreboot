package com.droidlogic.autoreboot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public class PingModel {
	private Context mContext;
	private String inet;

	private SharePrefUtils mpref;
	private String mTargetIps;
	private int pingTimes;
	private boolean pingChecked;
	public PingModel(Context cxt) {
		mContext=cxt;
		mpref=new SharePrefUtils(cxt);
		readParam();
	}
	public void readParam() {
		mTargetIps=mpref.getPingIps();
		pingTimes=mpref.getPingTimes();
		pingChecked=mpref.isPingCheck();
	}
	public void saveParams(String ips,int times,boolean check){
		String ip="www.baidu.com";
		if(ips!=null&&ips.length()>0){
			ip=ips;
		}
		mpref.setPingIps(ip);
		mpref.setPingCheck(check);
		mpref.setPingTimes(times);
		mTargetIps=ip;
		pingTimes=times;
		pingChecked=check;
	}
	public String getInet() {
		//if(hasIps())
			return inet;
		//return "not get ipaddress";
	}
	public String createPlan(){
		if(pingChecked)
			return "ping "+mTargetIps+" "+pingTimes+"s after reboot\n";
		return "";
	}
	public String getmTargetIps() {
		return mTargetIps;
	}
	public int getPingTimes() {
		return pingTimes;
	}
	public boolean isPingChecked() {
		return pingChecked;
	}
	public void setmTargetIps(String mTargetIps) {
		this.mTargetIps = mTargetIps;
	}
	public void setPingTimes(int pingTimes) {
		this.pingTimes = pingTimes;
	}
	public void setPingChecked(boolean pingChecked) {
		this.pingChecked = pingChecked;
	}
	public boolean execPlan(){
		if(!hasIps()){
			return false;
		}
		try {
			if(ping(mTargetIps)||pingo2(mTargetIps)){
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean hasIps(){
		 try {
			Enumeration<NetworkInterface> intefs=NetworkInterface.getNetworkInterfaces();
			while(intefs.hasMoreElements()){
				NetworkInterface ni=(NetworkInterface)intefs.nextElement();
				Enumeration<InetAddress> inets=ni.getInetAddresses();
				while(inets.hasMoreElements()){
					InetAddress addrs=(InetAddress)inets.nextElement();
					if(!addrs.isLoopbackAddress()&&!addrs.isLoopbackAddress()&&!(addrs.getHostAddress().indexOf(".")==-1))
					{
						System.out.println("ip:"+addrs.getHostAddress());
						inet=addrs.getHostAddress();
						//inet=addrs;
						return true;
					}
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return false;
	}
	private static String getIp(String name) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(name);
			return address.getHostAddress().toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static boolean getCheckResult(String line) {
		Pattern pattern = Pattern.compile("(\\s+)(TTL=\\d+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		while (matcher.find())
			return true;
		return false;
	}
	public static boolean ping(String ipAddress) throws Exception {
        int  timeOut = 3000 ;
        boolean status = InetAddress.getByName(ipAddress).isReachable(timeOut);
        return status;
    }
	public static boolean pingo2(String ipAddress) throws IOException {
		String line = null;
		String ip=getIp(ipAddress);
		if(ip==null) return false;
		Process pro = Runtime.getRuntime().exec("ping " + ip);
		BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
		int times=0;
		boolean pingOk=false;
		while ((line = buf.readLine()) != null&&times<10) {
			System.out.println(line);
			System.out.println(getCheckResult(line));
			times++;
			if(getCheckResult(line)){
				pingOk = true;
				break;
			}
		}
		return pingOk;
	}
}
