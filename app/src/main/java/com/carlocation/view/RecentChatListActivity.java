package com.carlocation.view;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.carlocation.R;

public class RecentChatListActivity extends ActionBarActivity {

	private ListView mTasksListView;
	private List<String> tasks;
	private LocalAdapter mLocalAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_activity);
		mTasksListView = (ListView)findViewById(R.id.task_list_view);
		mLocalAdapter = new LocalAdapter();
		tasks = new ArrayList<String>();
		
		for (int i = 0; i < 35; i++) {
			tasks.add("测试聊天 --记录  " + i);
		}
		
		mTasksListView.setAdapter(mLocalAdapter);
		
		overridePendingTransition(R.animator.left_in, R.animator.left_out); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	class LocalAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return tasks.size();
		}

		@Override
		public Object getItem(int position) {
			return tasks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv;
			if (convertView == null) {
				convertView = LayoutInflater.from(RecentChatListActivity.this).inflate(R.layout.task_item, null);
			}
			tv = (TextView)convertView.findViewById(R.id.task_title);
			tv.setText(tasks.get(position));
			return convertView;
		}
		
	};
	
}
