package com.ocse.android.app.http;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Administrator
 * Date: 13-9-20
 * Time: 上午8:46
 */
public class DoubanUtil {

    private static String BASE_URL = "https://api.douban.com/";

    public static List<Map<String, String>> getMovieList() {
        return movieList;
    }

    public static void setMovieList(List<Map<String, String>> movieList) {
        DoubanUtil.movieList = movieList;
    }

    private static List<Map<String, String>> movieList = new ArrayList<Map<String, String>>();
    private static List<Map<String, String>> bookList = new ArrayList<Map<String, String>>();

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static List<Map<String, String>> getBookList() {
        return bookList;
    }

    public static void setBookList(List<Map<String, String>> bookList) {
        DoubanUtil.bookList = bookList;
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    private static JSONArray getSubjects(String json, String type) {
        JSONObject object = null;
        JSONArray jsonArray = null;
        try {
            object = new JSONObject(json);
            jsonArray = object.getJSONArray(type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public final static List<Map<String, String>> getInfoList(String json, String type) {
        JSONArray jsonArray = getSubjects(json, type);
        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Map<String, String> map = new HashMap<String, String>();
                    JSONObject object = jsonArray.getJSONObject(i);
                    map.put("title", object.getString("title"));
                    boolean o = object.has("original_title");
                    String originalTitle = "";
                    if (!o) {
                        originalTitle = object.getString("origin_title");
                    } else {
                        originalTitle = object.getString("original_title");
                    }
                    map.put("original_title", originalTitle);
                    map.put("alt", object.getString("alt"));
                    map.put("id", object.getString("id"));
                    JSONObject images = object.getJSONObject("images");
                    if (images != null) {
                        map.put("small", images.getString("small"));
                        map.put("large", images.getString("large"));
                        map.put("medium", images.getString("medium"));
                    }
                    maps.add(map);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return maps;
    }

    public final static Map<String, String> getMove(String json) {
        Map<String, String> maps = new HashMap<String, String>();
        try {
            if (json != null && !"".equals(json)) {
                JSONObject object = new JSONObject(json);
                String alt = object.getString("alt");
                maps.put("alt", alt);
                String originalTitle = object.getString("original_title");
                maps.put("originalTitle", originalTitle);
                String summary = object.getString("summary");
                maps.put("summary", summary);
                String title = object.getString("title");
                maps.put("title", title);
                String large = "";
                JSONObject images = object.getJSONObject("images");
                if (images != null) {
                    large = images.getString("large");
                    maps.put("large", large);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return maps;
    }

    public final static Map<String, String> getBook(String json) {
        Map<String, String> maps = new HashMap<String, String>();
        try {
            if (json != null && !"".equals(json)) {
                JSONObject object = new JSONObject(json);
                String alt = object.getString("alt");
                maps.put("alt", alt);
                String originalTitle = object.getString("origin_title");
                maps.put("originalTitle", originalTitle);
                String summary = object.getString("summary");
                maps.put("summary", summary);
                String title = object.getString("title");
                maps.put("title", title);
                String large = "";
                JSONObject images = object.getJSONObject("images");
                if (images != null) {
                    large = images.getString("large");
                    maps.put("large", large);
                }
                String author = "";
                JSONArray authores = object.getJSONArray("author");
                if (authores != null) {
                    for (int i = 0; i < authores.length(); i++) {
                        if (i < authores.length() - 1) {
                            author += authores.getString(i) + ",";
                        } else {
                            author += authores.getString(i);
                        }
                    }
                    maps.put("author", author);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return maps;
    }
}
