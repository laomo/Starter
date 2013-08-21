package com.laomo.starter.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.laomo.starter.R;
import com.laomo.starter.model.AppInfo;
import com.laomo.starter.util.AppUtil;

/**
 * @author laomo
 */
public class AppsAdapter extends BaseAdapter {

    private List<AppInfo> applist;
    private Context mContext;
    private LayoutInflater inflater;
    private int iconSize;

    public AppsAdapter(Context context, List<AppInfo> applist) {
	mContext = context;
	iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
	this.applist = applist;
	inflater = LayoutInflater.from(mContext);
    }

    public int getCount() {
	return applist.size();
    }

    public Object getItem(int position) {
	return applist.get(position);
    }

    public long getItemId(int position) {
	return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
	TextView textView;
	if (convertView == null) {
	    textView = (TextView) inflater.inflate(R.layout.item_app, null);
	} else {
	    textView = (TextView) convertView;
	}
	AppInfo appInfo = applist.get(position);
	//自己控制图标大小，否则获取到的应用图标有大有小，还不知道原因
	Drawable drawable = AppUtil.loadIcon(mContext, appInfo.appIcon, appInfo.packageName);
	drawable.setBounds(0, 0, iconSize, iconSize);
	textView.setCompoundDrawables(null, drawable, null, null);
	textView.setGravity(Gravity.CENTER);//文字居中
	textView.setText(appInfo.appName);
	return textView;
    }
}