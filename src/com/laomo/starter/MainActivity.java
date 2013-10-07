package com.laomo.starter;

import java.util.List;

import roboguice.util.Ln;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.laomo.starter.adapter.AppsAdapter;
import com.laomo.starter.db.DatabaseHelper;
import com.laomo.starter.db.DatabaseManager;
import com.laomo.starter.db.DatabaseManagerImpl;
import com.laomo.starter.model.AppInfo;

public class MainActivity extends Activity implements OnItemClickListener {

    private ProgressBar mProgressBar;
    private GridView mGridView;
    private TextView mEmptyView;
    private AppsAdapter mAppsAdapter;
    private PackageManager mPackageManager;
    private DatabaseManager<AppInfo> mDatabaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
	mProgressBar.setVisibility(View.VISIBLE);
	mEmptyView = (TextView) findViewById(R.id.empty_view);
	mGridView = (GridView) findViewById(R.id.gridview);
	mGridView.setOnItemClickListener(this);
	mPackageManager = getPackageManager();
	mDatabaseManager = new DatabaseManagerImpl<AppInfo>(DatabaseHelper.getInstance(this), AppInfo.class);
	new MyAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Intent intent = new Intent(this, AllAppActivity.class);
	switch (item.getItemId()) {
	    case R.id.action_add:
		intent.putExtra(AllAppActivity.TYPE, AllAppActivity.TYPE_ADD);
		startActivityForResult(intent, 0);
		return true;
	    case R.id.action_delete:
		intent.putExtra(AllAppActivity.TYPE, AllAppActivity.TYPE_DELETE);
		startActivityForResult(intent, 0);
		return true;
	    case R.id.action_about:
		startActivity(new Intent(this, AboutActivity.class));
	    default:
		return super.onOptionsItemSelected(item);
	}

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	AppInfo appInfo = (AppInfo) mAppsAdapter.getItem(position);
	Intent intent = mPackageManager.getLaunchIntentForPackage(appInfo.packageName);
	if (intent == null) {
	    Toast.makeText(this, "无法打开该应用！", Toast.LENGTH_SHORT).show();
	} else {
	    //这个flag就是启动应用不出现在最近应用列表的关键
	    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	    startActivity(intent);
	    finish();
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == 0) {
	    if (Activity.RESULT_OK == resultCode) {
		new MyAsyncTask().execute();
	    } else if (Activity.RESULT_CANCELED == requestCode) {
		Ln.d("laomo", "no select apps！");
	    }
	}
    }

    class MyAsyncTask extends AsyncTask<Void, Void, List<AppInfo>> {

	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    mProgressBar.setVisibility(View.VISIBLE);
	    mEmptyView.setVisibility(View.GONE);
	}

	@Override
	protected List<AppInfo> doInBackground(Void... params) {
	    return mDatabaseManager.find();
	}

	@Override
	protected void onPostExecute(List<AppInfo> result) {
	    mProgressBar.setVisibility(View.GONE);
	    if (result.isEmpty()) {
		mEmptyView.setVisibility(View.VISIBLE);
	    }
	    mAppsAdapter = new AppsAdapter(MainActivity.this, result);
	    mGridView.setAdapter(mAppsAdapter);
	}
    }
}
