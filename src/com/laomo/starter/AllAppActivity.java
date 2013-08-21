package com.laomo.starter;

import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

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
    private AllAppsAdapter mAllAppsAdapter;
    private Button mSubmitBtn;
    private Button mCancelBtn;
    private DatabaseManager<AppInfo> mDatabaseManager;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_allapp);
	mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
	mSubmitBtn = (Button) findViewById(R.id.submmit_btn);
	mSubmitBtn.setOnClickListener(this);
	mCancelBtn = (Button) findViewById(R.id.cancel_btn);
	mCancelBtn.setOnClickListener(this);

	mDatabaseManager = new DatabaseManagerImpl<AppInfo>(DatabaseHelper.getInstance(this), AppInfo.class);
	mType = getIntent().getIntExtra(TYPE, 1);
	if (TYPE_ADD == mType) {
	    setTitle(R.string.action_add);
	} else if (TYPE_DELETE == mType) {
	    setTitle(R.string.action_add);
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

    class MyAsyncTask extends AsyncTask<Void, Void, List<AppInfo>> {

	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected List<AppInfo> doInBackground(Void... params) {
	    if (TYPE_DELETE == mType) {
		return mDatabaseManager.find();
	    }
	    //if (TYPE_ADD == mType)
	    return AppUtil.getAllApps(AllAppActivity.this, false);
	}

	@Override
	protected void onPostExecute(List<AppInfo> result) {
	    mAllAppsAdapter = new AllAppsAdapter(AllAppActivity.this, result);
	    setListAdapter(mAllAppsAdapter);
	    mProgressBar.setVisibility(View.GONE);
	}
    }
}
