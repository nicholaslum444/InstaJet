package com.nick.instajet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by Nicholas on 019, 19 Dec.
 */
public class Login extends Activity {

	SharedPreferences sharedPrefs;
	SharedPreferences.Editor sharedPrefsEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WebView webview = (WebView) findViewById(R.id.webview_login);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // we handle the url ourselves if it's a network url (http / https)
                return ! URLUtil.isNetworkUrl(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
	            if (url.contains("access_token")) {
		            String[] urlSplit = url.split("access_token=");
		            String accessToken = urlSplit[1];
		            Toast.makeText(Login.this, accessToken, Toast.LENGTH_LONG).show();
		            sharedPrefs = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE);
		            sharedPrefsEditor = sharedPrefs.edit();
		            if (sharedPrefsEditor == null) {
			            Toast.makeText(Login.this, "nooooooo", Toast.LENGTH_LONG).show();
		            } else {
			            sharedPrefsEditor.putString("accessToken", accessToken);
			            sharedPrefsEditor.putBoolean("isLoggedIn", true);
			            sharedPrefsEditor.commit();
			            finish();
		            }
	            }
            }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("https://instagram.com/oauth/authorize/?client_id=a3f9fc7b2b4e43b99228418f4363cbec&redirect_uri=http://nicholaslum444.github.io/&response_type=token");


    }
}