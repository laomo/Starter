package com.laomo.starter.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.laomo.starter.R;
import com.laomo.starter.model.AppInfo;
import com.laomo.starter.util.AppUtil;

/**
 * @author laomo
 */
public class AllAppsAdapter extends BaseAdapter {

    private List<AppInfo> applist;
    private Context mContext;
    private LayoutInflater mInflater;
    private int iconSize;
    private SparseBooleanArray isSelecteds = new SparseBooleanArray();

    public AllAppsAdapter(Context context, List<AppInfo> applist) {
	mContext = context;
	iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
	this.applist = applist;
	mInflater = LayoutInflater.from(mContext);
	init();
    }

    private void init() {
	int count = applist.size();
	for (int i = 0; i < count; i++) {
	    isSelecteds.put(i, false);
	}
    }

    public void toggleSelected(int position) {
	isSelecteds.put(position, !isSelecteds.get(position));
	notifyDataSetChanged();
    }

    public ArrayList<AppInfo> getSelecteds() {
	ArrayList<AppInfo> list = new ArrayList<AppInfo>();
	int count = isSelecteds.size();
	for (int i = 0; i < count; i++) {
	    if (isSelecteds.valueAt(i)) {
		list.add(applist.get(isSelecteds.keyAt(i)));
	    }
	}
	return list;
    }
    
    public ArrayList<String> getSelectedIds() {
	ArrayList<String> list = new ArrayList<String>();
	int count = isSelecteds.size();
	for (int i = 0; i < count; i++) {
	    if (isSelecteds.valueAt(i)) {
		list.add(applist.get(isSelecteds.keyAt(i)).packageName);
	    }
	}
	return list;
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
	ViewHolder holder;
	if (convertView == null) {
	    holder = new ViewHolder();
	    convertView = mInflater.inflate(R.layout.item_allapp, null);
	    holder.appView = (TextView) convertView.findViewById(R.id.app_view);
	    holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
	    convertView.setTag(holder);
	} else {
	    holder = (ViewHolder) convertView.getTag();
	}
	AppInfo appInfo = applist.get(position);
	//自己控制图标大小，否则获取到的应用图标有大有小，还不知道原因
	Drawable drawable = AppUtil.loadIcon(mContext, appInfo.appIcon, appInfo.packageName);
	drawable.setBounds(0, 0, iconSize, iconSize);
	holder.appView.setCompoundDrawables(drawable, null, null, null);
	holder.appView.setText(appInfo.appName);
	holder.checkBox.setChecked(isSelecteds.get(position));
	return convertView;
    }

    class ViewHolder {
	TextView appView;
	CheckBox checkBox;
    }
}