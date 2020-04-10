package com.meng.bc.activity.main;

import android.app.*;
import android.os.*;
import android.webkit.*;
import com.google.gson.*;
import com.meng.bc.activity.*;
import com.meng.bc.fragment.*;
import com.meng.bc.javaBean.*;
import com.meng.bc.libs.*;

public class Login extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setUserAgentString(MainActivity.instance.userAgent);
        webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(true);
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().removeAllCookie();
        webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return true;
				}

				@Override
				public void onPageFinished(final WebView view, final String url) {
					super.onPageFinished(view, url);
					//	MainActivity.instance.showToast(url);
					if (url.equals("https://passport.bilibili.com/login")) {
						if (SJFSettings.getPhone() != -1 && SJFSettings.getPassword() != null) {
							view.evaluateJavascript(Tools.AndroidContent.readAssetsString("patchDelete"), null);
							//	view.evaluateJavascript(String.format(Tools.AndroidContent.readAssetsString("patchInput"), SJFSettings.getPhone(), SJFSettings.getPassword()), null);
							MainActivity.instance.threadPool.execute(new Runnable(){

									@Override
									public void run() {
										try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {}
										MainActivity.instance.runOnUiThread(new Runnable(){

												@Override
												public void run() {
													view.evaluateJavascript("javascript:document.querySelectorAll('.btn-login')[0].click();", null);
												}
											});
									}
								});
						}
					}
					if (!url.equals("https://www.bilibili.com/")) {
						return;
					}
					CookieManager cookieManager = CookieManager.getInstance();
					SJFSettings.setCookie((cookieManager.getCookie(url) == null ? "null" : cookieManager.getCookie(url)));
					MainActivity.instance.threadPool.execute(new Runnable() {
							@Override
							public void run() {
								BilibiliUserInfo bilibiliPersonInfo = new Gson().fromJson(Tools.Network.getSourceCode("https://api.bilibili.com/x/space/myinfo?jsonp=jsonp", SJFSettings.getCookie()), BilibiliUserInfo.class);
								SJFSettings.setUID(bilibiliPersonInfo.data.mid);
								runOnUiThread(new Runnable() {

										@Override
										public void run() {
											finish();
										}
									});

							}
						});
				}
			});
        webView.loadUrl("https://passport.bilibili.com/login");
    }
}

