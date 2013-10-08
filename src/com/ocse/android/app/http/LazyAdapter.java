package com.ocse.android.app.http;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ocse.android.app.R;
import com.ocse.android.app.sina.SinaTool;

import java.util.List;
import java.util.Map;

public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private List<Map<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;

    public LazyAdapter(Activity a, List<Map<String, String>> d) {
        activity = a;
        data = d;
        inflater = activity.getLayoutInflater();
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.doubanitem, null);
        }
        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView artist = (TextView) vi.findViewById(R.id.artist);
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);

        Map<String, String> song;
        song = data.get(position);
        title.setText(song.get("title"));
        artist.setText(song.get("title"));
        if (SinaTool.getStatus_map().contains(song.get("title"))) {
            RelativeLayout relativeLayout = (RelativeLayout) vi.findViewById(R.id.listall);
            relativeLayout.setBackgroundColor(Color.GREEN);
        }
        Log.v("豆瓣JSON信息图片地址：", song.get("small"));
        imageLoader.displayImage(song.get("small"), thumb_image);
        return vi;
    }
}