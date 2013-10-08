package com.ocse.android.app.sina;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.ocse.android.app.R;
import com.ocse.android.app.http.DoubanUtil;
import com.ocse.android.app.http.FileCache;
import com.ocse.android.app.tencent.QQTool;
import weibo4j.Oauth;
import weibo4j.Timeline;
import weibo4j.http.ImageItem;
import weibo4j.model.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Administrator
 * Date: 13-9-24
 * Time: 下午3:04
 */
public class SinaTool {

    private static Map<String, Object> sinaURLMap = new HashMap<String, Object>();
    private static PopupWindow popupWindow = null;
    private static List<String> status_map = new ArrayList<String>();

    public void auth(final Activity activity, final View view) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View layout = inflater.inflate(R.layout.sina, null);
        layout.setBackgroundResource(R.drawable.doubanstyle);
        popupWindow = new PopupWindow(layout, 650, 750, true);
        popupWindow.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rounded_corners_view));
        Handler myHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String url = msg.getData().getString("URL") + "&display=mobile";
                WebView webView = (WebView) popupWindow.getContentView().findViewById(R.id.sinawebview);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        sinaURLMap.put("URL", url);
                        Log.v("新浪微博URL：", url);
                    }
                });
                webView.loadUrl(url);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                popupWindow.setFocusable(true);
                popupWindow.update();
            }
        };
        OuthThread outhThread = new OuthThread(myHandler);
        Thread thread = new Thread(outhThread);
        thread.start();
    }

    private class OuthThread implements Runnable {
        private Handler myHandler;

        public OuthThread(Handler handler) {
            myHandler = handler;
        }

        public void run() {
            Message message = new Message();
            Oauth oauth = new Oauth();
            try {
                String url = oauth.authorize("code", "", "");
                Bundle bundle = new Bundle();
                bundle.putString("URL", url);
                message.setData(bundle);
                sinaURLMap.put("Oauth", oauth);
                myHandler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Handler oauthEnd(final Activity activity, final Oauth oauth, final View view, final String code) {
        final Handler myHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                final String c = msg.getData().getString("code");
                Toast toast = Toast.makeText(view.getContext(),
                        "授权完成！授权码：" + c, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        update(activity, c, view.getContext());
                    }
                }).start();
                popupWindow.dismiss();
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String codeTemp = oauth.getAccessTokenByCode(code).getAccessToken();
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("code", codeTemp);
                    message.setData(bundle);
                    myHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return myHandler;
    }

    public void update(final Activity activity, final String oauth, Context context) {
        FileCache fileCache = new FileCache(context);
        sendMessage(activity, fileCache, oauth);
    }

    public static Map<String, Object> getSinaURLMap() {
        return sinaURLMap;
    }

    private final void sendMessage(final Activity activity, final FileCache fileCache, final String oauth) {
        List<Map<String, String>> maps = DoubanUtil.getMovieList();
        coreSendMessage(activity, fileCache, oauth, maps);
        List<Map<String, String>> mapsBook = DoubanUtil.getBookList();
        coreSendBookesMessage(activity, fileCache, oauth, mapsBook);
    }

    private void coreSendBookesMessage(final Activity activity, final FileCache fileCache, final String oauth,
                                       List<Map<String, String>> maps) {
        Log.v("豆瓣信息汇总", maps.toString());
        if (maps != null) {
            for (Map<String, String> map : maps) {
                String id = map.get("id");
                final String title = map.get("title");
                Log.v("输出豆瓣信息", "开始获取信息：" + id);
                DoubanUtil.get("/v2/book/" + id, null, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        Log.v("输出豆瓣信息", response);
                        Map<String, String> maps = DoubanUtil.getBook(response);
                        if (maps != null) {
                            StringBuffer stringBuffer = new StringBuffer("[yeah],");
                            stringBuffer.append("好书：" + maps.get("title"));
                            if (maps.get("originalTitle") != null && !"".equals(maps.get("originalTitle"))) {
                                stringBuffer.append("[" + maps.get("originalTitle") + "]");
                            }
                            stringBuffer.append(",作者:" + maps.get("author"));
                            stringBuffer.append("," + maps.get("summary"));
                            getBitmap(activity, maps.get("large"), fileCache, stringBuffer, oauth, maps);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable, String s) {
                        super.onFailure(throwable, s);
                        Log.v("输出豆瓣信息出错", s);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.v("获取豆瓣信息结束，标题", title);
                    }
                });
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void coreSendMessage(final Activity activity, final FileCache fileCache, final String oauth,
                                 List<Map<String, String>> maps) {
        Log.v("豆瓣信息汇总", maps.toString());
        if (maps != null) {
            for (Map<String, String> map : maps) {
                String id = map.get("id");
                final String title = map.get("title");
                Log.v("输出豆瓣信息", "开始获取信息：" + id);
                DoubanUtil.get("/v2/movie/subject/" + id, null, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        Log.v("输出豆瓣信息", response);
                        Map<String, String> maps = DoubanUtil.getMove(response);
                        if (maps != null) {
                            StringBuffer stringBuffer = new StringBuffer("[yeah],");
                            stringBuffer.append("电影：" + maps.get("title"));
                            if (maps.get("originalTitle") != null && !"".equals(maps.get("originalTitle"))) {
                                stringBuffer.append("[" + maps.get("originalTitle") + "]");
                            }
                            stringBuffer.append("," + maps.get("summary"));
                            getBitmap(activity, maps.get("large"), fileCache, stringBuffer, oauth, maps);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable, String s) {
                        super.onFailure(throwable, s);
                        Log.v("输出豆瓣信息出错", s);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.v("获取豆瓣信息结束，标题", title);
                    }
                });
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getBitmap(final Activity activity, final String url, FileCache fileCache, final StringBuffer stringBuffer,
                           final String oauth, final Map<String, String> maps) {
        Log.v("豆瓣大图片地址", url);
        final File f = fileCache.getFile(url);
        Log.v("豆瓣大图片存储", f.getAbsolutePath());
        //从网络
        try {
            AsyncHttpClient client = new AsyncHttpClient();
            String[] allowedContentTypes = new String[]{"image/png", "image/jpeg"};
            client.get(url, new BinaryHttpResponseHandler(allowedContentTypes) {
                @Override
                public void onSuccess(byte[] bytes) {
                    FileOutputStream fout = null;
                    try {
                        fout = new FileOutputStream(f);
                        fout.write(bytes);
                        String content = stringBuffer.toString();
                        if (stringBuffer.length() >= 140) {
                            content = stringBuffer.substring(0, 130) + "..." + maps.get("alt");
                        }
                        ImageItem pic = null;
                        try {
                            pic = new ImageItem("pic", bytes);
                            String s = java.net.URLEncoder.encode(content, "utf-8");
                            Timeline tl = new Timeline();
                            tl.client.setToken(oauth);
                            Status status = tl.UploadStatus(s, pic);
                            Log.v("微博发送完成", status.getText());
                            status_map.add(maps.get("title"));
                            QQTool qqTool = new QQTool(activity);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            qqTool.updateStatus(content.replaceAll("\\[yeah\\]", "/大兵"), bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fout != null) {
                            try {
                                fout.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<String> getStatus_map() {
        return status_map;
    }
}
