package com.droidlogic.autoreboot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader; 
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog; 
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener; 

public class AutoRebootActivity extends Activity implements View.OnClickListener {
	public static final String TAG = "reboot";
	private Button selectFile;
	private Button startTest;
	private Button stopTest;

	private TextView mResult;
	private TextView mFilePath;

	private EditText mDelayTime;
	private EditText mPingTime;
	private EditText mPingIP;
	private EditText mRebootTimes;
	private CheckBox mReboot;
	private CheckBox mUpdate;
	private CheckBox mPing;
       private RadioButton mSingle;
       private RadioButton mLoop;
       private RadioGroup radioGroup;

	private SharePrefUtils mUtils;
	public static final int DELAY_TIME = 30;// 30s
	public static final int REBOOT_AFTER = 3;
	public static final int PING_TIMES = 3;
	public static final String NET_URL = "www.baidu.com";
	private RebootModel rebootModel;
	private PingModel pingModel;
	private UpdateModel updateModel;
	private HandlerThread mRThread;
	private Handler mRHandler;
	private static final int queryUpdateFile = 1;
	private static final int queryReturnOk = 0;
	private static final int PING = 1;
	private static final int UPDATE = 2;
	private static final int REBOOT = 3;
	private int tryTimes = 0;
	private Handler mUIHandler;
	private LoggerWriter Logger;
	private View root;
	private PowerManager.WakeLock wl;
       private BufferedReader mReader = null;
       private Process logcatProc;
       private Process logcatProc2;
       private Process logcatProc3;
       private String picName = "failImg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
              Log.i(TAG, "[onCreate]");
		Logger = new LoggerWriter(this);
		setContentView(R.layout.activity_auto_reboot);
		selectFile = (Button) findViewById(R.id.select_updateFile);
		startTest = (Button) findViewById(R.id.start_plan);
		stopTest = (Button) findViewById(R.id.stop_plan);
             radioGroup = (RadioGroup) findViewById(R.id.radioGroupID);
		selectFile.setOnClickListener(this);
		startTest.setOnClickListener(this);
		stopTest.setOnClickListener(this);
             radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                 public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    switch (checkedId) {
                        case R.id.checkSingle:
                            mRebootTimes.setEnabled(true);
                            break;
                        case R.id.checkLoop:
                            mRebootTimes.setEnabled(false);
                            break;
                        default:
                            break;
                    }
                 }
              });
		TextView version=(TextView)findViewById(R.id.version_info);
		version.setText(getString(R.string.version)+getString(R.string.version_info));
		mResult = (TextView) findViewById(R.id.test_result);
		mFilePath = (TextView) findViewById(R.id.file_path);
		mDelayTime = (EditText) findViewById(R.id.editText1);
		mRebootTimes = (EditText) findViewById(R.id.edit_times);
		mPingTime = (EditText) findViewById(R.id.editText3);
		mPingIP = (EditText) findViewById(R.id.editText4);
		
		mReboot = (CheckBox) findViewById(R.id.checkBox1);
		mUpdate = (CheckBox) findViewById(R.id.checkBox2);
		mPing = (CheckBox) findViewById(R.id.check_box3);
             mSingle = (RadioButton) findViewById(R.id.checkSingle);
             mLoop = (RadioButton) findViewById(R.id.checkLoop);
		mUtils = new SharePrefUtils(this); 
		if (!mUtils.isSetup()) {
			Logger.Log("apk have not been setup????");
			this.finish();
		}
        
            rebootModel = new RebootModel(this);
            pingModel = new PingModel(this);
            updateModel = new UpdateModel(this);
            Logger.Log("apk load test model ok");
            mRThread = new MyHandlerThread("reboot-t");
            mRThread.start();
            mRHandler = new MyHandler(mRThread.getLooper());
            //initView();
            mUIHandler = new Handler();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "autoreboot");
        wl.acquire();
		Logger.Log("apk load ui");
		if(!mUtils.isRunningPlan()){
			root = findViewById(R.id.tableLayout1);
			root.setBackgroundColor(android.graphics.Color.RED);
			Logger.Log("test not running use delay ui");
			mUIHandler.postDelayed(new Runnable(){
				public void run(){
					initView();
				}
			}, 5*1000);
		}else{
			initView();
		}
	}
	
	private void initView() {
		Logger.Log("test show ui");
		if (!mUtils.isRunningPlan()) {
			Logger.Log("test not running,only show ui!");
			stopTest.setEnabled(false);
			loadUI();
		} else {
			Logger.Log("test is running,show ui!");
			loadUI();
			selectFile.setEnabled(false);
			stopTest.setEnabled(true);
			startTest.setEnabled(false);
			mResult.setText(computeResult());
			execPlan();
		}
	}
	
	private String computeResult() {
		String ret = "";
             String res = this.getResources().getString(R.string.current_items); 
		if (mUtils.isRunningPlan()) {
			ret = this.getResources().getString(R.string.running) + "\n";
		} else {
			ret = this.getString(R.string.stoping) + " " + this.getString(R.string.stop_time) + mUtils.getStopTime() + " " + "It's 8h before normal time" + "\n";
		}
		ret += this.getString(R.string.start_time) + mUtils.getStartTime() + " " + "It's 8h before normal time" + "\n";
		if (mReboot.isChecked()) {
			ret += this.getResources().getString(R.string.reboot_times) + mUtils.getRunningTimes() + "\n";
                    res += this.getResources().getString(R.string.reboot);
		}
		if (mUpdate.isChecked()) {
			ret += this.getResources().getString(R.string.update_times) + mUtils.getUpdateTimes() + "\n";
                    res += this.getResources().getString(R.string.update);
		}
             if (mPing.isChecked()) {
			ret += this.getResources().getString(R.string.ping_IP) + mUtils.getPingIps() + "\n";
                    res += this.getResources().getString(R.string.ping);
		}
             if (mSingle.isChecked()) {
			ret += this.getResources().getString(R.string.current_mode) + "Single";
		}
             if (mLoop.isChecked()) {
			ret += this.getResources().getString(R.string.current_mode) + "Loop";
		}
             ret += "\n" + res;
		return ret;
	}

	@Override
	protected void onPause() {
	       Log.i(TAG, "[onPause]");
        Logger.Log("[onPause]");
		super.onPause();
		wl.release();
	}

    @Override
	protected void onStop() {
	       Log.i(TAG, "[onStop]");
		   Logger.Log("[onStop]");
		super.onStop();
	}

    @Override
	protected void onDestroy() {
	       Log.i(TAG, "[onDestroy]");
		   Logger.Log("[onDestroy]");
		super.onDestroy();
	}

	private void execPlan() {
		Logger.Log( "exec plan update times:" + mUtils.getUpdateTimes() + " reboot times:" + mUtils.getRunningTimes());
		Log.d(TAG, "exec plan update times:" + mUtils.getUpdateTimes() + " reboot times:" + mUtils.getRunningTimes());
		if (mPing.isChecked()) {
			tryTimes = 0;
			Log.d(TAG, "plan to Ping");
			mRHandler.sendEmptyMessageDelayed(PING,DELAY_TIME*1000);
		} else if (mReboot.isChecked() && !mUpdate.isChecked() || mUpdate.isChecked() && mReboot.isChecked()
		        && (1 + mUtils.getRunningTimes() + mUtils.getUpdateTimes())
		                % (1 + rebootModel.getmRebootTimes()) != 0) {
			Log.d(TAG, "exec reboot");
			mRHandler.sendEmptyMessageDelayed(REBOOT, 1000 * rebootModel.getmDelayTime());
		} else if (mUpdate.isChecked()/*
		                               * &&(1+mUtils.getRunningTimes()+mUtils.
		                               * getUpdateTimes())%rebootModel.
		                               * getmRebootTimes()==0
		                               */) {
			mRHandler.sendEmptyMessageDelayed(UPDATE, DELAY_TIME * 1000);
			Log.d(TAG, "exec update");
		} 
        }

	private void loadUI() {
		root = findViewById(R.id.tableLayout1);
		root.setBackgroundColor(android.graphics.Color.WHITE);
		if (mUtils.isPingCheck())
			mPing.setChecked(true);
		else
			mPing.setChecked(false);
		if (mUtils.isRebootCheck())
			mReboot.setChecked(true);
		else
			mReboot.setChecked(false); 
		if (mUtils.isUpdateCheck())
			mUpdate.setChecked(true);
		else
			mUpdate.setChecked(false);
             if (mUtils.isSingleChecked()) {
			mSingle.setChecked(true); 
                } else {
			mLoop.setChecked(true); 
            }
                if (radioGroup != null && mRebootTimes != null) {
                     int btId = radioGroup.getCheckedRadioButtonId();
                     switch (btId) {
                        case R.id.checkSingle:
                            mRebootTimes.setEnabled(true);
                            break;
                        case R.id.checkLoop:
                            mRebootTimes.setEnabled(false);
                            break;
                        default:
                            break;
                    }
                }
		mDelayTime.setText("" + rebootModel.getmDelayTime());
		mPingTime.setText("" + pingModel.getPingTimes());
		mPingIP.setText(pingModel.getmTargetIps());
		mRebootTimes.setText("" + rebootModel.getmRebootTimes());
		mFilePath.setText(mUtils.getUpdatePath());
		mResult.setText(computeResult());
		mUtils.saveStopTime();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.select_updateFile:
                            savePlan();
				Intent intent0 = new Intent(this, FileSelector.class);
				Activity activity = (Activity) this;
				startActivityForResult(intent0, queryUpdateFile);
				break;
			case R.id.start_plan:
				//mUtils.setRunningTimes(0);
				//mUtils.setUpdateTimes(0);
				if (!mPing.isChecked() && !mReboot.isChecked() && !mUpdate.isChecked()){
                                    AlertDialog dialog = new AlertDialog.Builder(this)
                                       .setMessage("No test item is choosed, you need to choose at least one item.")
                                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                           @Override
                                           public void onClick(DialogInterface dialog, int which) {
                                               mUtils.stopRunning();
                                               mUIHandler.post(new Runnable() {
                				         @Override
                					   public void run() {
                					       stopPlan();
                					   }
                				    });
                                           }
                                       })
                                       .create();
                                  dialog.show();
                           }
				mUtils.clear();
				mUtils.Setup();
				mUtils.startRunning();
                           savePlan();
				startTest.setEnabled(false);
				stopTest.setEnabled(true);
                           mResult.setBackgroundColor(Color.parseColor("#6D0ACE0C"));
				execPlan();
				mResult.setText(mUtils.getTestPlan() + computeResult());
				break;
			case R.id.stop_plan:
				mUtils.stopRunning();
				stopPlan();
				break;
		}
	}
	
	private void stopPlan() {
		mRHandler.removeMessages(REBOOT);
		mRHandler.removeMessages(UPDATE);
		while(mRHandler.hasMessages(PING))
			mRHandler.removeMessages(PING);
		startTest.setEnabled(true);
		stopTest.setEnabled(false);
		selectFile.setEnabled(true);
		mResult.setText(computeResult());
             //mResult.setBackgroundColor(Color.parseColor("#6D0ACE0C"));
	}
	
	private void savePlan() {
		String plan = "";
		String delayStr = mDelayTime.getText().toString();
		String times = mRebootTimes.getText().toString();
		//rebootModel.saveRebootParam(delayStr, times, mReboot.isChecked(), mLoop.isChecked());
             rebootModel.saveRebootParam(delayStr, times, mReboot.isChecked(), mSingle.isChecked());
		plan = rebootModel.createPlan();
		String updateFile = mFilePath.getText().toString();
		if (mUpdate.isChecked() && (updateFile == null || updateFile.length() == 0)) {
			mUpdate.setChecked(false);// cancel
			AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("No file is selected, please select a file.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUtils.stopRunning();
                                stopPlan();
                            }
                        })
                        .create();
                   dialog.show();
		} else {
			updateModel.saveParam(mFilePath.getText().toString(), mUpdate.isChecked());
			plan += updateModel.createPlan(rebootModel.getmRebootTimes());
		}
		if(mPing.isChecked()&&(!mUpdate.isChecked()&&!mReboot.isChecked())) {
			mPing.setChecked(false);
                    AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("Can not only do ping, please press 'Stop Test' button.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUtils.stopRunning();
                                stopPlan();
                            }
                        })
                        .create();
                   dialog.show();
		}
		String pingTimes = mPingTime.getText().toString();
		String pingIps = mPingIP.getText().toString();
		int t = PING_TIMES;
		try {
			t = Integer.parseInt(pingTimes);
		} catch (NumberFormatException ex) {
		    Log.e(TAG, "[savePlan]ex :" + ex);
		}
		pingModel.saveParams(pingIps, t, mPing.isChecked());
		plan += pingModel.createPlan();
		mUtils.savePlan(plan);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
             Log.i(TAG, "[onActivityResult] requestCode :" + requestCode + ", resultCode :" + resultCode);
			if ((requestCode == queryUpdateFile) && (resultCode == queryReturnOk)) {
				Bundle bundle = data.getExtras();
				String file = bundle.getString(FileSelector.FILE);
                           Log.i(TAG, "[onActivityResult] file :" + file);
				if (file != null) {
					String path = (new File(file)).getAbsolutePath();
					updateModel.setFilePath(path);
					mFilePath.setText(updateModel.getPath());
                                 updateModel.saveParam(mFilePath.getText().toString(), mUpdate.isChecked());
                                Log.i(TAG, "[onActivityResult] path :" + path + ", updateModel.getPath() :" + updateModel.getPath());
				}
			}
		}
            else {
                Log.e(TAG, "[onActivityResult] data == null");
            }
	}
	
	class MyHandler extends Handler {
		MyHandler(Looper loop) {
			super(loop);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case PING:
					Logger.Log("exec ping "+pingModel.getInet()+"  current try ping:"+tryTimes);
					if (mPing.isChecked()) {
						mUIHandler.post(new Runnable() {
							@Override
							public void run() {
							      if(pingModel.hasIps()) {
                                                        mResult.setBackgroundColor(android.graphics.Color.GREEN);
                                                        mResult.setText(mResult.getText() + "\n"+ AutoRebootActivity.this.getString(R.string.ping_ip) + pingModel.getInet());
                                                    } else {
                                                        mResult.setBackgroundColor(android.graphics.Color.RED);
                                                        mResult.setText(mResult.getText() + "\n" + AutoRebootActivity.this.getString(R.string.ping_ip) + "not get ipaddress"
                                                            + "\n" + "detail bugInfo this time can be checked in /data/data/com.droidlogic.autoreboot/file/cache-log.txt file"
                                                            + "\n" + "detail bugInfo everytime before reboot can be checked in /data/data/com.droidlogic.autoreboot/file/cache-log-backup.txt file"
                                                            + "\n" + "screencap can be checked under /data/data/com.droidlogic.autoreboot/file/ directory");
                                                    }
							}
							
						});
					}
					if ((tryTimes+1) < pingModel.getPingTimes() && !pingModel.execPlan()) {
						Log.d(TAG, "ping failed");
						Logger.Log("exec ping failed");
						mRHandler.sendEmptyMessageDelayed(PING, DELAY_TIME*1000);
						tryTimes++;
						return;
					} else if ((tryTimes+1) >= pingModel.getPingTimes()) {
						Logger.Log("exec ping timeout");
                                        mUtils.stopRunning();
						mUIHandler.post(new Runnable() {
							@Override
							public void run() {
							      //stopPlan();
								mRHandler.removeMessages(REBOOT);
                                        		mRHandler.removeMessages(UPDATE);
                                        		while(mRHandler.hasMessages(PING))
                                        			mRHandler.removeMessages(PING);
                                        		startTest.setEnabled(true);
                                        		stopTest.setEnabled(false);
                                        		selectFile.setEnabled(true); 
							}
						});
                                        //captureScreen
                                        try {
                                            logcatProc2 = Runtime.getRuntime().exec("screencap /data/data/com.droidlogic.autoreboot/files/" + picName + ".png");
                                        } catch(IOException e1) {
                                            e1.printStackTrace();
                                        }
                                        //ifconfig
                                        try {
                                            if(Build.VERSION.SDK_INT>=24) {
                                                logcatProc = Runtime.getRuntime().exec("ifconfig");
                                            } else {
                                                logcatProc = Runtime.getRuntime().exec("netconfig");
                                            }
                                        } catch(IOException e2) {
                                            e2.printStackTrace();
                                        }
                                        mReader = new BufferedReader(new InputStreamReader(
                                                logcatProc.getInputStream()), 1024);
                                        String line = null;
                                        int linenum = 0;
                                        try {
                                            while((line = mReader.readLine()) != null) {
                                                Logger.Log(line);
                                            }
                                        } catch(IOException e3) {
                                            e3.printStackTrace();
                                        }
                                        //copyFile
                                        try {
                                            logcatProc3 = Runtime.getRuntime().exec("cp /data/data/com.droidlogic.autoreboot/files/cache-log /data/data/com.droidlogic.autoreboot/files/cache-log-backup");
                                        } catch(IOException e3) {
                                            e3.printStackTrace();
                                        }
                                        return;
					}
					Logger.Log("ping success" + mUtils.getRunningTimes() + " - " + rebootModel.getmRebootTimes());
					Log.d(TAG, "ping success" + mUtils.getRunningTimes() + " - " + rebootModel.getmRebootTimes());
					if (mReboot.isChecked() && !mUpdate.isChecked()){
                                          //|| mReboot.isChecked() && (mUtils.getRunningTimes() <= rebootModel.getmRebootTimes()) {
					        //|| mReboot.isChecked() && ((1 + mUtils.getRunningTimes() + mUtils.getUpdateTimes())
					               // % (rebootModel.getmRebootTimes()+1)) != 0) {
						Logger.Log("wait for reboot test");
						mRHandler.sendEmptyMessageDelayed(REBOOT, rebootModel.getmDelayTime() * 1000);
					} else if (mUpdate.isChecked()) {
					//&& ((mUtils.getRunningTimes() + mUtils.getUpdateTimes() + 1)
					   //     % (rebootModel.getmRebootTimes()+1)) == 0) {
						Logger.Log("wait for update test");
						mRHandler.sendEmptyMessageDelayed(UPDATE,10000);
					} else {
						Logger.Log("end test successly");
						mUIHandler.post(new Runnable() {
							@Override
							public void run() {
								stopPlan();
							}
						});
						Log.d(TAG, "end" + tryTimes);
					}
					break;
				case UPDATE:
					Logger.Log("exec update test");
					mUtils.setUpdateTimes(mUtils.getUpdateTimes() + 1);
					updateModel.execPlan();
					break;
				case REBOOT:
					Logger.Log("exec reboot test");
					mUtils.setRunningTimes(mUtils.getRunningTimes() + 1);
                                 if(mSingle.isChecked() && mUtils.getRunningTimes() <= rebootModel.getmRebootTimes()) {
                                     rebootModel.rebootNow();
									 Logger.Log("reboot case 1111");
                                 } else if(mLoop.isChecked()) {
                                     rebootModel.rebootNow();
									 Logger.Log("reboot case 2222");
                                 } else {
                                     mUtils.stopRunning();
                                     mUIHandler.post(new Runnable() {
                                        public void run() {
                                            mUtils.setRunningTimes(mUtils.getRunningTimes() - 1);
                                            stopPlan();
											Logger.Log("stopplan case 3333");
                                        }
                                    });
                                 }
					break;
			}
		}
	}
	
	class MyHandlerThread extends HandlerThread{

		public MyHandlerThread(String name) {
			super(name);
		}

		@Override
		protected void onLooperPrepared() {
			super.onLooperPrepared();
		}

		@Override
		public void run() {
			Logger.Log("HandlerThread start run");
			super.run();
		}
		
	}
}
