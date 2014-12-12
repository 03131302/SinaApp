package com.ocse.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ocse.android.app.http.DoubanUtil;
import com.ocse.android.app.sina.SinaTool;
import com.ocse.android.app.sql.DBManager;
import com.ocse.android.app.sql.Douban;
import com.ocse.android.app.tencent.QQTool;
import com.tencent.weibo.sdk.android.api.util.Util;
import weibo4j.Oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private CheckBox checkBox = null;
    private PopupWindow popupWindow = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkedChanged(isChecked);
            }
        });
    }

    public void checkedChanged(final boolean isChecked) {
        if (isChecked) {
            EditText sinaUsername = (EditText) findViewById(R.id.sinaUsername);
            EditText sinaPassword = (EditText) findViewById((R.id.sinaPassword));
            if (!"".equals(sinaUsername.getText())) {
                ((EditText) findViewById(R.id.doubanUsername)).setText(sinaUsername.getText());
                ((EditText) findViewById(R.id.doubanPassword)).setText(sinaPassword.getText());
                ((EditText) findViewById(R.id.qqUsername)).setText(sinaUsername.getText());
                ((EditText) findViewById(R.id.qqPassword)).setText(sinaPassword.getText());
            }
        } else {
            ((EditText) findViewById(R.id.doubanUsername)).setText("");
            ((EditText) findViewById(R.id.doubanPassword)).setText("");
            ((EditText) findViewById(R.id.qqUsername)).setText("");
            ((EditText) findViewById(R.id.qqPassword)).setText("");
        }
    }

    public void sinaAuthOk(View view) {
        String url = (String) SinaTool.getSinaURLMap().get("URL");
        String code = url.substring(url.indexOf("code=") + 5, url.length());
        Log.v("截取授权码：", code);
        Oauth oauth = (Oauth) SinaTool.getSinaURLMap().get("Oauth");
        SinaTool sinaTool = new SinaTool();
        sinaTool.oauthEnd(MainActivity.this, oauth, view, code);
    }

    public void weobo(final View view) {
        SinaTool sinaTool = new SinaTool();
        sinaTool.auth(this, view);
    }

    public void qqStatr(final View view) {
        QQTool qqTool = new QQTool(MainActivity.this);
        long appid = Long.valueOf(Util.getConfig().getProperty("APP_KEY"));
        String app_secket = Util.getConfig().getProperty("APP_KEY_SEC");
        qqTool.auth(appid, app_secket);
    }

    public void startDo(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.douban, null);
        layout.setBackgroundResource(R.drawable.doubanstyle);
        popupWindow = new PopupWindow(layout, 550, 800, true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corners_view));
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupWindow.setFocusable(true);
        DBManager dbManager = new DBManager(this);
        List<Douban> doubans = dbManager.query();
        if (doubans != null) {
            if (doubans.size() == 0) {
                addDoubanData(dbManager);
            } else {
                Douban douban = doubans.get(0);
                EditText tag = (EditText) popupWindow.getContentView().findViewById(R.id.tag);
                EditText start = (EditText) popupWindow.getContentView().findViewById(R.id.starttext);
                EditText end = (EditText) popupWindow.getContentView().findViewById(R.id.endtext);
                Log.v("数据库信息：", douban.toString());
                tag.setText(douban.getTag());
                start.setText(String.valueOf(douban.getStart()));
                end.setText(String.valueOf(douban.getEnd()));
            }
        } else {
            addDoubanData(dbManager);
        }
        popupWindow.update();
    }

    private void addDoubanData(DBManager dbManager) {
        List<Douban> list = new ArrayList<Douban>();
        Douban douban = new Douban("励志", 0, 10);
        list.add(douban);
        dbManager.add(list);
    }

    public void doubanOK(final View view) {
        final DBManager dbManager = new DBManager(this);
        final EditText tag = (EditText) popupWindow.getContentView().findViewById(R.id.tag);
        final EditText start = (EditText) popupWindow.getContentView().findViewById(R.id.starttext);
        final EditText end = (EditText) popupWindow.getContentView().findViewById(R.id.endtext);
        final String tagText = tag.getText().toString();
        final String startText = start.getText().toString();
        final String endText = end.getText().toString();
        RequestParams params = new RequestParams();
        params.put("tag", tag.getText().toString());
        params.put("start", start.getText().toString());
        params.put("end", end.getText().toString());
        params.put("alt", "json");
        int startInt = Integer.parseInt(start.getText().toString());
        int endInt = Integer.parseInt(end.getText().toString());
        params.put("count", String.valueOf(endInt - startInt));
        Log.v("输出豆瓣信息：", "开始.....");
        DoubanUtil.get("v2/movie/search", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                List<Map<String, String>> maps = DoubanUtil.getInfoList(response, "subjects");
                DoubanUtil.setMovieList(maps);
                Log.v("输出豆瓣信息：", maps.toString());
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
                Log.v("输出豆瓣信息出错：", s);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                RequestParams params_sub = new RequestParams();
                params_sub.put("tag", tag.getText().toString());
                params_sub.put("start", start.getText().toString());
                params_sub.put("end", end.getText().toString());
                params_sub.put("alt", "json");
                int startInt = Integer.parseInt(start.getText().toString());
                int endInt = Integer.parseInt(end.getText().toString());
                params_sub.put("count", String.valueOf(endInt - startInt));
                Log.v("输出豆瓣信息：", "开始.....");
                DoubanUtil.get("v2/book/search", params_sub, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        List<Map<String, String>> mapBookes = DoubanUtil.getInfoList(response, "books");
                        DoubanUtil.setBookList(mapBookes);
                        Log.v("输出豆瓣信息：", mapBookes.toString());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.v("输出豆瓣信息：", "结束");
                        Douban douban = new Douban(tagText, Integer.parseInt(startText), Integer.parseInt(endText));
                        dbManager.updateAge(douban);
                        Button button = (Button) findViewById(R.id.button1);
                        button.setEnabled(true);
                        weobo(view);
                        qqStatr(view);
                    }
                });
            }
        });
        popupWindow.dismiss();
    }

    public void showStatus(View view) {
        Intent myIntent = new Intent(MainActivity.this, DoubanList.class);
        MainActivity.this.startActivity(myIntent);
    }
}
