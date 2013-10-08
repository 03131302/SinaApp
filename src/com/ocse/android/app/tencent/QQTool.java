package com.ocse.android.app.tencent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import com.tencent.weibo.sdk.android.api.WeiboAPI;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.tencent.weibo.sdk.android.component.Authorize;
import com.tencent.weibo.sdk.android.component.sso.AuthHelper;
import com.tencent.weibo.sdk.android.component.sso.OnAuthListener;
import com.tencent.weibo.sdk.android.component.sso.WeiboToken;
import com.tencent.weibo.sdk.android.model.AccountModel;

/**
 * User: Administrator
 * Date: 13-10-4
 * Time: 上午11:40
 */
public class QQTool {
    private Activity activity;
    private Context context;

    public QQTool(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void auth(long appid, String app_secket) {
        AuthHelper.register(activity.getApplicationContext(), appid, app_secket, new OnAuthListener() {
            @Override
            public void onWeiBoNotInstalled() {
                Toast.makeText(activity, "onWeiBoNotInstalled", 1000)
                        .show();
                Intent i = new Intent(activity, Authorize.class);
                activity.startActivity(i);
            }

            @Override
            public void onWeiboVersionMisMatch() {
                Toast.makeText(activity, "onWeiboVersionMisMatch",
                        1000).show();
                Intent i = new Intent(activity, Authorize.class);
                activity.startActivity(i);
            }

            @Override
            public void onAuthFail(int result, String err) {
                Toast.makeText(activity, "result : " + result, 1000)
                        .show();
            }

            @Override
            public void onAuthPassed(String name, WeiboToken token) {
                Toast.makeText(activity, "passed", 1000).show();
                Util.saveSharePersistent(context, "ACCESS_TOKEN", token.accessToken);
                Util.saveSharePersistent(context, "EXPIRES_IN", String.valueOf(token.expiresIn));
                Util.saveSharePersistent(context, "OPEN_ID", token.openID);
                Util.saveSharePersistent(context, "REFRESH_TOKEN", "");
                Util.saveSharePersistent(context, "CLIENT_ID", Util.getConfig().getProperty("APP_KEY"));
                Util.saveSharePersistent(context, "AUTHORIZETIME",
                        String.valueOf(System.currentTimeMillis() / 1000l));
            }
        });
        AuthHelper.auth(activity, "http://t.qq.com/my03131302");
    }

    public void updateStatus(String text, Bitmap bitmap) {
        AccountModel accountModel = new AccountModel(Util.getSharePersistent(context, "ACCESS_TOKEN"));
        WeiboAPI weiboAPI = new WeiboAPI(accountModel);
        weiboAPI.addPic(context, text, "json", 0d, 0d, bitmap, 0, 0, null, null, 4);
        Log.v("腾讯微博发送结束", text);
    }
}
