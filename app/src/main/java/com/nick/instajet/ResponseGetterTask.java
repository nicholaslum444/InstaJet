package com.nick.instajet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class ResponseGetterTask extends AsyncTask<String, Void, JSONObject> {
	
	ResponseGetterTaskListener mCaller;
	
	public ResponseGetterTask(ResponseGetterTaskListener caller) {
		mCaller = caller;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		try {
			String apiUrl = params[0];
			
			URL url = new URL(apiUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			
			// check the response code
			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				// connection success
				
				// build json string
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					sb.append(line);
					line = br.readLine();
				}
				br.close();
				String responseContent = sb.toString();
				
				// create json object and return it
				JSONObject j = new JSONObject(responseContent);
				return j;
				
			} else {
				// connection error, return null
				return null;
			}
			
		} catch (MalformedURLException e) {
			// url spoil
			Log.w("rgt", "url");
			e.printStackTrace();
			return null;
			
		} catch (IOException e) {
			// connection error
			Log.w("rgt", "io");
			e.printStackTrace();
			return null;
			
		} catch (JSONException e) {
			// json making fail
			Log.w("rgt", "json");
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(JSONObject j) {
		mCaller.updateResponseObject(j);
	}

}
