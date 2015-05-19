package com.carlocation.demo;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.carlocation.R;
import com.carlocation.comm.messaging.RankType;
import com.carlocation.view.UserService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends ListActivity {
    private final static String LOG_TAG = "DemoActivity";

    private final static List<String> mItems = new ArrayList<>();
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_demo);
        mList = getListView();

        try{
            Method[] methods = UserService.class.getMethods();
            for (Method method:methods) {
                mItems.add(method.getName());
            }
        }catch (SecurityException e){
            Log.e(LOG_TAG,"onCreate():NoSuchMethodException!");
            e.printStackTrace();
        }

        setListAdapter(new ArrayAdapter<String>(DemoActivity.this,android.R.layout.simple_list_item_1,mItems));

        mList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
