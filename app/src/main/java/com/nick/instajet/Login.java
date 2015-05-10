package com.nick.instajet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.CookieManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nicholas on 019, 19 Dec.
 */
public class Login extends Activity implements InstagramApiHandlerTaskListener {

	SharedPreferences sharedPrefs;
	SharedPreferences.Editor sharedPrefsEditor;
    Context context = this;

    private static final String USER_INFO_REQUEST_TEMPLATE = "https://api.instagram.com/v1/users/self/?access_token=%1$s";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookie();
        WebView webview = (WebView) findViewById(R.id.webview_login);
        webview.getSettings().setSaveFormData(false);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // we handle the url ourselves if it's a network url (http / https)
                return !URLUtil.isNetworkUrl(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("access_token")) {
                    String[] urlSplit = url.split("access_token=");
                    String accessToken = urlSplit[1];
                    sharedPrefs = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE);
                    sharedPrefsEditor = sharedPrefs.edit();
                    if (sharedPrefsEditor == null) {
                        Toast.makeText(Login.this, "nooooooo", Toast.LENGTH_LONG).show();
                    } else {
                        sharedPrefsEditor.putString("accessToken", accessToken);
                        sharedPrefsEditor.putBoolean("isLoggedIn", true);
                        sharedPrefsEditor.apply();
                        sendUserInfoRequest();
                    }
	            }
            }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("https://instagram.com/oauth/authorize/?client_id=a3f9fc7b2b4e43b99228418f4363cbec&redirect_uri=http://nicholaslum444.github.io/instajet-callback.html&response_type=token");
    }

    private void sendUserInfoRequest() {
        String accessToken = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE).getString("accessToken", "notoken");
        String apiUrl = String.format(USER_INFO_REQUEST_TEMPLATE, accessToken);
        InstagramApiHandlerTask dataGetter = new InstagramApiHandlerTask(this);
        dataGetter.execute(apiUrl);
        Log.e("asd", "info request sent");
    }

    @Override
    public void receiveApiResponse(JSONObject j) {
        Log.e("asd", "info request response received");
        saveUserInfoToPreferences(j);
    }

    private void saveUserInfoToPreferences(JSONObject o) {
        try {
            JSONObject data = o.getJSONObject("data");
            String username = data.getString("username");
            String profilePictureUrl = data.getString("profile_picture");
            String userId = data.getString("id");
            String fullname = data.getString("full_name");

            SharedPreferences.Editor spe = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE).edit();
            spe.putString("username", username);
            spe.putString("profilePictureUrl", profilePictureUrl);
            spe.putString("userId", userId);
            spe.putString("fullname", fullname);

            spe.apply();

            onLoginCompleted();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void onLoginCompleted() {
        Intent intent = new Intent(context, Home.class);
        startActivity(intent);
        finish();
    }
}
