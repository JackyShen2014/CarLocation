package com.carlocation.demo;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.carlocation.R;
import com.carlocation.comm.messaging.RankType;
import com.carlocation.view.MainActivity;
import com.carlocation.view.UserService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends ListActivity {
    private final static String LOG_TAG = "DemoActivity";

    private static List<String> mItems;
    private ListView mList;
    private UserService mUserService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserService = (UserService)getIntent().getExtras().getSerializable("mUserService");

        mList = getListView();
        mItems = new ArrayList<>();

        //Reflect all announced public methods in UserService
        try{
            Method[] methods = UserService.class.getDeclaredMethods();
            for (int i=0;i<methods.length;i++) {
                mItems.add(methods[i].getName());
            }
        }catch (SecurityException e){
            Log.e(LOG_TAG,"onCreate():NoSuchMethodException!");
            e.printStackTrace();
        }

        setListAdapter(new ArrayAdapter<>(DemoActivity.this,android.R.layout.simple_list_item_1,mItems));

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0:
                        break;
                    case 3:
                        //getTerminalId()
                        Toast.makeText(DemoActivity.this,mUserService.getTerminalId(),Toast.LENGTH_SHORT).show();

                        break;
                    case 12:
                        //Send ImTxtMsg

                        break;
                    default:break;
                }
            }
        });



    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
