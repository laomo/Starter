package com.laomo.starter;

import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.laomo.starter.adapter.AllAppsAdapter;
import com.laomo.starter.db.DatabaseHelper;
import com.laomo.starter.db.DatabaseManager;
import com.laomo.starter.db.DatabaseManagerImpl;
import com.laomo.starter.model.AppInfo;
import com.laomo.starter.util.AppUtil;

public class AllAppActivity extends ListActivity implements OnClickListener {

    public static final String TYPE = "type";
    public static final int TYPE_ADD = 1;
    public static final int TYPE_DELETE = 2;

    private ProgressBar mProgressBar;
    private TextView mEmptyView;
    private AllAppsAdapter mAllAppsAdapter;
    private Button mSubmitBtn;
    private Button mCancelBtn;
    private DatabaseManager<AppInfo> mDatabaseManager;
    private int mType;
    private boolean withSystemApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_allapp);
	mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
	mSubmitBtn = (Button) findViewById(R.id.submmit_btn);
	mSubmitBtn.setOnClickListener(this);
	mCancelBtn = (Button) findViewById(R.id.cancel_btn);
	mCancelBtn.setOnClickListener(this);
	mEmptyView = (TextView) findViewById(R.id.empty_view);
	mDatabaseManager = new DatabaseManagerImpl<AppInfo>(DatabaseHelper.getInstance(this), AppInfo.class);
	mType = getIntent().getIntExtra(TYPE, 1);
	if (TYPE_ADD == mType) {
	    setTitle(R.string.action_add);
	} else if (TYPE_DELETE == mType) {
	    setTitle(R.string.action_delete);
	}
	new MyAsyncTask().execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	super.onListItemClick(l, v, position, id);
	mAllAppsAdapter.toggleSelected(position);
    }

    @Override
    public void onClick(View v) {
	switch (v.getId()) {
	    case R.id.submmit_btn:
		new Thread() {
		    public void run() {
			if (TYPE_ADD == mType) {
			    mDatabaseManager.insertOrUpdate(mAllAppsAdapter.getSelecteds());
			} else if (TYPE_DELETE == mType) {
			    mDatabaseManager.deleteByIdList(mAllAppsAdapter.getSelectedIds());
			}
			runOnUiThread(new Runnable() {

			    @Override
			    public void run() {
				setResult(Activity.RESULT_OK);
				finish();
			    }
			});
		    }
		}.start();
		break;
	    case R.id.cancel_btn:
		setResult(Activity.RESULT_CANCELED);
		finish();
		break;

	    default:
		break;
	}
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setTitle(withSystemApp?R.string.action_no_sys_app:R.string.action_with_sys_app);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if(TYPE_ADD == mType){
    		withSystemApp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("withSystemApp", false);
        	menu.add(0, 1, 0, getString(withSystemApp?R.string.action_no_sys_app:R.string.action_with_sys_app));
        	return true;
    	}else{
    		return false;
    	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	withSystemApp = !withSystemApp;
    	new MyAsyncTask().execute();
    	PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("withSystemApp", withSystemApp).commit();
		return true;
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
	    if (TYPE_DELETE == mType) {
		return mDatabaseManager.find();
	    }
	    return AppUtil.getAllApps(AllAppActivity.this, withSystemApp);
	}

	@Override
	protected void onPostExecute(List<AppInfo> result) {
	    mProgressBar.setVisibility(View.GONE);
	    if (result.isEmpty()) {
		mEmptyView.setVisibility(View.VISIBLE);
	    }
	    mAllAppsAdapter = new AllAppsAdapter(AllAppActivity.this, result);
	    setListAdapter(mAllAppsAdapter);
	}
    }
}
