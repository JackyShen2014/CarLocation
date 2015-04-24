package com.carlocation.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.carlocation.R;
import com.carlocation.comm.IMessageService;
import com.carlocation.comm.messaging.LocationMessage;
import com.carlocation.comm.messaging.TerminalType;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     *User Service
     */
    private UserService mUserService;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

    //LOG IN Views
    private EditText field_usrName;
    private EditText field_pasWord;
    private Button button_logIn;

    private String mUserName;
    private String mPasWord;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        findViews();
        setListeners();

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

        /**
         * An example to check translate() method
         */
        /*Log.d("Jacky","Test LocationMessge.translate() begin!");
        LocationMessage examplLM = new LocationMessage(123L, TerminalType.TERMINAL_CAR,10.01,10.02,9.01f);
        examplLM.translate();
        Log.d("Jacky","Test LocationMessge.translate() end!");*/

	}

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void findViews(){
        field_usrName = (EditText)findViewById(R.id.userName);
        field_pasWord = (EditText)findViewById(R.id.passWord);
        button_logIn = (Button)findViewById(R.id.logIn);
    }

    public void setListeners(){
        button_logIn.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                MainActivity.this.start();
            }
        });
    }

    protected void start(){
        if(field_usrName.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "Pls enter UserName!", Toast.LENGTH_SHORT).show();
            field_usrName.requestFocus();
            return;
        }else {
            mUserName = field_usrName.getText().toString();
        }
        if(field_pasWord.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "Pls enter Pass Word!", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        }else {
            mPasWord = field_pasWord.getText().toString();
        }

        new send().execute();
    }

    private class send extends AsyncTask<String, Void, Void> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Void doInBackground(String... params) {
            //Retrieve Native Service and Send LogIn MSG to server to login.
            /**
             * Retrieve native service.
             */
            mUserService = new UserService(((CarLocationApplication)getApplicationContext()).getService());

            /**
             * An example for how to use UserService to send MSG to Server
             */
            mUserService.logIn(mUserName, mPasWord);

            return null;
        }
    }


    @Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

}
