package com.mobilesafe.app;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import domain.AppInfo;
import engine.AppInfoProvider;

/**
 * 软件管理界面
 * 
 * @author Administrator
 * 
 */
public class AppManagerActivity extends BaseAcitivity {

	private TextView tv_avail_rom;
	private TextView tv_avail_sd;
	private TextView tv_show_app_count;
	private ListView lv_app_manager;
	
	private List<AppInfo> appInfos; // 保存所有的应用程序信息
	private List<AppInfo> userInfos; // 保存所有的用户应用程序信息
	private List<AppInfo> sysInfos; // 保存所有的系统应用程序信息
	
	private LinearLayout ll_loading;
	private MyAapater adapter;
	private PopupWindow popupWindow; //弹出的悬浮窗体

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appmanager);
		initView();
	}

	private void initView() {
		userInfos = new ArrayList<AppInfo>();
		sysInfos = new ArrayList<AppInfo>();
		
		tv_show_app_count = (TextView) findViewById(R.id.tv_show_app_count);
		tv_avail_rom = (TextView) findViewById(R.id.tv_rom);
		tv_avail_sd = (TextView) findViewById(R.id.tv_sdcard);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading_flag);

		long sdSize = getAvailMemory(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		long romSize = getAvailMemory(Environment.getDataDirectory()
				.getAbsolutePath());
		tv_avail_sd.setText("SD卡可用:" + Formatter.formatFileSize(this, sdSize));
		tv_avail_rom.setText("内存可用:" + Formatter.formatFileSize(this, romSize));

		lv_app_manager = (ListView) findViewById(R.id.lv_my_apps);
		adapter = new MyAapater();

		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
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
							tv_show_app_count.setText("用户程序 ( "+ userInfos.size() + " )");
						}
						lv_app_manager.setAdapter(adapter);
						ll_loading.setVisibility(View.GONE);
					}
				});
			}
		}.start();

		lv_app_manager.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				dismissPopWindow();
				
				if (userInfos != null && sysInfos != null) {
					if (firstVisibleItem > userInfos.size()) {
						tv_show_app_count.setText("系统程序 ( " + sysInfos.size()
								+ " )");
					} else {
						tv_show_app_count.setText("用户程序 ( " + userInfos.size()
								+ " )");
					}
				}
			}
		});
		
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				AppInfo appInfo;
				if (position == 0 || position == (userInfos.size() + 1)) {
					return;
				} else if (position <= userInfos.size()) { //用户程序
					int newPos = position - 1;
					appInfo = userInfos.get(newPos);
				} else { //系统程序
					int newPos = position - 2 - userInfos.size();
					appInfo = sysInfos.get(newPos);
				}
				showPopupWindow(appInfo, parent, view);
			}

		});
	}

	//显示悬浮窗体
	protected void showPopupWindow(AppInfo appInfo, AdapterView<?> parent, View view) {
		dismissPopWindow();
		View contentView = View.inflate(AppManagerActivity.this, R.layout.popup_app_item,
				null);
		popupWindow = new PopupWindow(contentView, -2, -2);
		
		int[] location = new int[2];
		view.getLocationInWindow(location);
		popupWindow.showAtLocation(parent, Gravity.RIGHT | Gravity.TOP,
				location[0], location[1]);
	}

	private void dismissPopWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	@SuppressWarnings("deprecation")
	private long getAvailMemory(String path) {
		StatFs statf = new StatFs(path);
		long count = statf.getAvailableBlocks();
		long size = statf.getBlockSize();
		return size * count;
	}

	private class MyAapater extends BaseAdapter {

		@Override
		public int getCount() {
			return appInfos.size() + 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppInfo appInfo;
			if (position == 0) {
				TextView tv = new TextView(AppManagerActivity.this);
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.parseColor("#212121"));
				tv.setText("用户程序 ( " + userInfos.size() + " )");
				return tv;
			} else if (position == (userInfos.size() + 1)) {
				TextView tv = new TextView(AppManagerActivity.this);
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.parseColor("#212121"));
				tv.setText("系统程序 ( " + sysInfos.size() + " )");
				return tv;
			} else if (position <= userInfos.size()) { //用户程序
				int newPositon = position - 1;
				appInfo = userInfos.get(newPositon);
			} else { //系统程序
				int newPosition = position - userInfos.size() - 2;
				appInfo = sysInfos.get(newPosition);
			}
			
			View view;
			ViewHolder holder;
			
            if ( convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(AppManagerActivity.this,
						R.layout.list_item_appinfo, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.tv_location = (TextView) view
						.findViewById(R.id.tv_app_location);
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.iv_app_icon);
				view.setTag(holder);
			}
			
			holder.iv_icon.setImageDrawable(appInfo.getAppIcon());
			holder.tv_name.setText(appInfo.getAppName());
			if (appInfo.isInstallRom()) {
				holder.tv_location.setText("手机内存");
			} else {
				holder.tv_location.setText("外部设备");
			}
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

	static class ViewHolder {
		TextView tv_name;
		TextView tv_location;
		ImageView iv_icon;
	}
	
	@Override
	protected void onDestroy() {
		dismissPopWindow();
		super.onDestroy();
	}
}
