package com.droidlogic.autoreboot;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
public class WelcomeActivity extends Activity implements ViewPager.OnPageChangeListener{
	private ViewPager vp;
	private int[] imageIdArray;
	private List<View> viewList;
	private ViewGroup vg;
	
	private ImageView iv_point;
	private ImageView []ivPointArray;
	private ImageButton ib_start;
	private SharePrefUtils mPrefUtil;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefUtil = new SharePrefUtils(this);
		if(mPrefUtil.isSetup()){
			startActivity(new Intent(WelcomeActivity.this,AutoRebootActivity.class));
			this.finish();
		}
		setContentView(R.layout.welcome);
		
		ib_start=(ImageButton)findViewById(R.id.guide_ib_start);
		ib_start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPrefUtil.Setup();
				startActivity(new Intent(WelcomeActivity.this,AutoRebootActivity.class));
				WelcomeActivity.this.finish();
			}
		});
		initViewPaper();
		initPoint();
	}

	private void initPoint() {
		vg=(ViewGroup)findViewById(R.id.guid_ll_point);
		ivPointArray=new ImageView[viewList.size()];
		int size=viewList.size();
		for(int i=0;i<size;i++){
			iv_point=new ImageView(this);
			iv_point.setLayoutParams(new ViewGroup.LayoutParams(20,20));
			ivPointArray[i]=iv_point;
			if(i==0){
				iv_point.setBackgroundResource(R.drawable.point);
			}else{
				iv_point.setBackgroundResource(R.drawable.poing_null);
			}
			vg.addView(ivPointArray[i]);
		}
	}

	private void initViewPaper() {
		vp=(ViewPager)findViewById(R.id.guide_vp);
		imageIdArray=new int[]{R.drawable.guide_1,R.drawable.guide_2,R.drawable.guide_3,R.drawable.guide_4};
		viewList=new ArrayList<View>();
		LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		int len=imageIdArray.length;
		for(int i=0;i<len;i++){
			ImageView imageView=new ImageView(this);
			imageView.setLayoutParams(params);
			imageView.setBackgroundResource(imageIdArray[i]);
			
			viewList.add(imageView);
		}
		vp.setAdapter(new GuidPageAdapter(viewList));
		vp.setOnPageChangeListener(this);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		int length=imageIdArray.length;
		for(int i=0;i<length;i++){
			ivPointArray[position].setBackgroundResource(R.drawable.point);
			if(position!=i){
				ivPointArray[i].setBackgroundResource(R.drawable.poing_null);
			}
		}
		
		if(position==imageIdArray.length-1){
			ib_start.setVisibility(View.VISIBLE);
			ib_start.requestFocus();
		}else{
			ib_start.setVisibility(View.GONE);
		}
	}
	
}
