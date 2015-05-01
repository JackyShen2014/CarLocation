package com.carlocation.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.carlocation.R;
import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.Notification;

public class LoginActivity extends Activity {

	private static final int PREPARE_UPDATE_MAP = 1;
	private static final int FINISH_UPDATE_MAP = 2;

	private EditText field_usrName;
	private EditText field_pasWord;
	private Button button_logIn;
	private ProgressDialog dialog;

	private UserService mUserService;

	private LocalListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Message.obtain(localHandler, PREPARE_UPDATE_MAP).sendToTarget();

		mListener = new LocalListener();

		field_usrName = (EditText) findViewById(R.id.userName);
		field_pasWord = (EditText) findViewById(R.id.passWord);
		button_logIn = (Button) findViewById(R.id.logIn);

		button_logIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (field_usrName.getText().toString().length() == 0) {
					field_usrName.setError("Pls enter UserName!");
					field_usrName.requestFocus();
					return;
				}
				if (field_pasWord.getText().toString().length() == 0) {
					field_pasWord.setError("Pls enter Pwd!");
					field_pasWord.requestFocus();
					return;
				}
				if (mUserService == null) {
					mUserService = new UserService(
							((CarLocationApplication) getApplicationContext())
									.getService(), mListener);
				}
				mUserService.logIn(field_usrName.getText().toString(),
						field_pasWord.getText().toString());

			}

		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void updateMap() {
		String mapversion = "";
		try {
			InputStream in = this.getAssets().open("env.config");
			Properties prop = new Properties();
			prop.load(in);
			mapversion = prop.getProperty("map_update");
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String sdPath = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath().toString();
		File mapdir = new File(sdPath + "/carlocation_map");
		if (!mapdir.exists()) {
			mapdir.mkdirs();
		}

		SharedPreferences sp = this.getApplicationContext()
				.getSharedPreferences("map", MODE_PRIVATE);
		if (!sp.getString("map_version", "").equals(mapversion)) {
			Editor edit = sp.edit();
			edit.putString("map_version", mapversion);
			edit.commit();

			InputStream in = null;
			OutputStream out = null;
			String[] names = new String[0];
			try {
				names = this.getAssets().list("map");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			int len = 0;
			byte[] b = new byte[1024];
			for (String s : names) {
				try {
					in = this.getAssets().open("map/" + s);
					out = new FileOutputStream(new File(mapdir + "/" + s));
					while ((len = in.read(b)) > 0) {
						out.write(b, 0, len);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}

	}

	private Handler localHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PREPARE_UPDATE_MAP:
				if (dialog != null) {
					dialog.dismiss();
				}
				// TODO move text to resource file
				dialog = ProgressDialog.show(LoginActivity.this, "",
						"正在更新地图数据....", true, false);
				updateMapTask.execute();
				dialog.show();
				break;
			case FINISH_UPDATE_MAP:
				dialog.dismiss();
				break;
			}
		}

	};

	private AsyncTask<Void, Void, Void> updateMapTask = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {
			updateMap();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Message.obtain(localHandler, FINISH_UPDATE_MAP).sendToTarget();
		}

	};

	class LocalListener implements ResponseListener, NotificationListener {

		/**
		 * Unsolicited message notification.
		 * 
		 * @param noti
		 */
		@Override
		public void onNotify(Notification noti) {
			forward(noti);
		}

		@Override
		public void onResponse(Notification noti) {
			forward(noti);
		}

		private void forward(Notification notif) {
			if (notif.notiType == Notification.NotificationType.RESPONSE
					&& notif.result == Notification.Result.SUCCESS) {
				Intent i = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(i);
				finish();
			}
		}
	}

}
