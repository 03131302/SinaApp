package com.ocse.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import com.ocse.android.app.http.DoubanUtil;
import com.ocse.android.app.http.LazyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Administrator
 * Date: 13-9-20
 * Time: 下午12:21
 */
public class DoubanList extends Activity {

    ListView list;
    LazyAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doubanlistinfo);
        list = (ListView) findViewById(R.id.list);
        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        maps.addAll(DoubanUtil.getMovieList());
        maps.addAll(DoubanUtil.getBookList());
        Log.v("豆瓣JSON信息：", maps.toString());
        adapter = new LazyAdapter(this, maps);
        list.setAdapter(adapter);
    }
}