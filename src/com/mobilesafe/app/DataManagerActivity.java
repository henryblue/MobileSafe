package com.mobilesafe.app;

import java.util.ArrayList;
import java.util.List;


import domain.AppInfo;
import engine.AppInfoProvider;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class DataManagerActivity extends BaseAcitivity {

	private LinearLayout ll_loading;
	private TextView tv_satus;
	private ListView lv_app_manager;
	private MyAapater adapter;
	protected List<AppInfo> appInfos;
	protected List<AppInfo> userInfos;
	protected List<AppInfo> sysInfos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datamanager);
		initView();
	}
	
	private void initView() {
        userInfos = new ArrayList<AppInfo>();
        sysInfos = new ArrayList<AppInfo>();
		
		ll_loading = (LinearLayout) findViewById(R.id.ll_data_loading);
		tv_satus = (TextView) findViewById(R.id.tv_show_data_count);
		lv_app_manager = (ListView) findViewById(R.id.lv_data_apps);
		adapter = new MyAapater();
		fillData();
		
		lv_app_manager.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                if (userInfos != null && sysInfos != null) {
                    if (firstVisibleItem > userInfos.size()) {
                    	tv_satus.setText("系统软件 ( " + sysInfos.size()
                                + " )");
                    } else {
                    	tv_satus.setText("个人软件 ( " + userInfos.size()
                                + " )");
                    }
                }
            }
        });

	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				appInfos = AppInfoProvider
						.getAppInfos(DataManagerActivity.this);
				for (AppInfo info : appInfos) {
					if (info.isUserApp()) {
						userInfos.add(info);
					} else {
						sysInfos.add(info);
					}
				}
				
				runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (userInfos != null) {
                        	tv_satus.setText("个人软件 ( "
                                    + userInfos.size() + " )");
                        }
                        lv_app_manager.setAdapter(adapter);
                        ll_loading.setVisibility(View.GONE);
                    }
                });
			};
		}.start();
	}
	
	private class MyAapater extends BaseAdapter {

        @Override
        public int getCount() {
            return appInfos.size() + 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AppInfo appInfo;
            if (position == 0) {
                TextView tv = new TextView(DataManagerActivity.this);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#77212121"));
                tv.setText("用户程序 ( " + userInfos.size() + " )");
                return tv;
            } else if (position == (userInfos.size() + 1)) {
                TextView tv = new TextView(DataManagerActivity.this);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#77212121"));
                tv.setText("系统程序 ( " + sysInfos.size() + " )");
                return tv;
            } else if (position <= userInfos.size()) { // 用户程序
                int newPositon = position - 1;
                appInfo = userInfos.get(newPositon);
            } else { // 系统程序
                int newPosition = position - userInfos.size() - 2;
                appInfo = sysInfos.get(newPosition);
            }

            View view;
            final ViewHolder holder;

            if (convertView != null && convertView instanceof RelativeLayout) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(DataManagerActivity.this,
                        R.layout.list_item_datamanager, null);
                holder = new ViewHolder();
                holder.tv_name = (TextView) view.findViewById(R.id.tv_data_name);
                holder.iv_icon = (ImageView) view
                        .findViewById(R.id.iv_data_icon);
                view.setTag(holder);
            }
            holder.tv_name.setText(appInfo.getAppName());
            holder.iv_icon.setImageDrawable(appInfo.getAppIcon());
         
            return view;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

    }
	
    class ViewHolder {
    	ImageView iv_icon;
        TextView tv_name;
    }
	
}