package com.nick.instajet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Home extends Activity implements ResponseGetterTaskListener {
	
	// TODO set this to the user's own via authing
	String accessToken = "188264189.1fb234f.1cab308d94f24fabbc9dba5895a417ca";
	
	// TODO send this to the Strings xml
	String apiUrlTemplate = "https://api.instagram.com/v1/media/shortcode/%1$s"
			+ "?access_token=%2$s";
	
	JSONObject responseObject;
	
	long imageDownloadId;
	long videoDownloadId;

    SharedPreferences sharedPrefs;
	
	String shortcode;
	
	/**
	 * This method is called when the Download button is pressed
	 * 
	 * @param v the Button that was pressed
	 */
	public void startDownloadProcess(View v) {
		// get the user typed url string
		EditText urlField = (EditText) findViewById(R.id.EditTextUrlField);
		String urlString = urlField.getText().toString();

		// to remove the final "/" to prevent confusion during split
		while (urlString.endsWith("/")) {
			urlString = urlString.substring(0, urlString.length() - 1);
		}
		
		// get the short code, which is the last item in the split array 
		String[] urlStringSplit = urlString.split("/");
		shortcode = urlStringSplit[urlStringSplit.length - 1];

		// retrieve the accessToken everytime we try to download
		accessToken = sharedPrefs.getString("accessToken", null);
		if (accessToken == null) {
			checkLogin();
		}

		// construct the api url
		String apiUrl = String.format(apiUrlTemplate, shortcode, accessToken);
		
		// execute the json getter
		ResponseGetterTask dataGetter = new ResponseGetterTask(this);
		dataGetter.execute(apiUrl);
		
		// pause download process while waiting for the response
		
		// TODO DEBUG
		// Toast.makeText(this, apiUrl, Toast.LENGTH_LONG).show();
		
	}
	
	@Override
	public void updateResponseObject(JSONObject j) {
		try {
			responseObject = new JSONObject(j.toString());
			continueDownloadProcess();
			
		} catch (JSONException e) {
			// making the new json object got problem. 
			e.printStackTrace();
		}
	}

	private void continueDownloadProcess() {
		boolean isOk = checkFor200();
		if (isOk) {
			String type = determineDataType();
			executeBasedOnType(type);
			
		} else {
			// TODO received object not ok
		}
	}

	private void executeBasedOnType(String type) {
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		
		// TODO extract the mess inside the ifelse to another method
		if (type.equals("image")) {
			try {
				// TODO extract magic strings
				JSONObject data = responseObject.getJSONObject("data");
				JSONObject images = data.getJSONObject("images");
				JSONObject stdResImage = images.getJSONObject("standard_resolution");
				String imageUrl = stdResImage.getString("url");
				/* debug */Log.w("home", imageUrl);
				Uri parsedImageUri = Uri.parse(imageUrl);
				Request imageRequest = new Request(parsedImageUri);
				imageRequest.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				imageRequest.allowScanningByMediaScanner();
				imageRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, shortcode+"_image.jpg");
				/* debug */Log.w("home", parsedImageUri.toString());
				imageDownloadId = dm.enqueue(imageRequest);
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		} else if (type.equals("video")) {
			try {
				// TODO extract magic strings
				JSONObject data = responseObject.getJSONObject("data");
				// we will download both the video and the cover pic

				// get the cover pic first
				JSONObject images = data.getJSONObject("images");
				JSONObject stdResImage = images.getJSONObject("standard_resolution");
				String imageUrl = stdResImage.getString("url");
				/* debug */Log.w("home", imageUrl);
				Uri parsedImageUri = Uri.parse(imageUrl);
				Request imageRequest = new Request(parsedImageUri);
				imageRequest.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				imageRequest.allowScanningByMediaScanner();
				imageRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, shortcode+"_cover.jpg");
				/* debug */Log.w("home", parsedImageUri.toString());
				imageDownloadId = dm.enqueue(imageRequest);
				
				// then get the video itself
				JSONObject videos = data.getJSONObject("videos");
				JSONObject stdResVideo = videos.getJSONObject("standard_resolution");
				String videoUrl = stdResVideo.getString("url");
				/* debug */Log.w("home", videoUrl);
				Uri parsedVideoUri = Uri.parse(videoUrl);
				Request videoRequest = new Request(parsedVideoUri);
				videoRequest.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				videoRequest.allowScanningByMediaScanner();
				videoRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, shortcode+"_video.mp4");
				/* debug */Log.w("home", parsedVideoUri.toString());
				videoDownloadId = dm.enqueue(videoRequest);
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		} else {
			// TODO throw error for weird type
		}
	}
	
	private boolean checkFor200() {
		try {
			JSONObject data = responseObject.getJSONObject("meta");
			int type = data.getInt("code");
			return type == 200;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String determineDataType() {
		try {
			JSONObject data = responseObject.getJSONObject("data");
			String type = data.getString("type");
			return type;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void testing(View v) {
		EditText urlField = (EditText) findViewById(R.id.EditTextUrlField);
		urlField.setText("http://instagram.com/p/wYN2jBmoVQ/");
	}
	
	public void testing2(View v) {
		EditText urlField = (EditText) findViewById(R.id.EditTextUrlField);
		urlField.setText("http://instagram.com/p/wNjVZvmoZx/");
	}
	
	
	
	
	
	
	
	/** ON CREATE */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		// TODO if not logged in, then open popup to login
		sharedPrefs = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE);
		checkLogin();
	}

	private void checkLogin() {
		boolean isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false);
		if (!isLoggedIn) {
			openLoginPage();
		}
	}

	private void openLoginPage() {
		Intent intent = new Intent(this, Login.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
