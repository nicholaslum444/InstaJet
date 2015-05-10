package com.nick.instajet;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloaderService
        extends IntentService
        implements InstagramApiHandlerTaskListener {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_URL_DOWNLOAD = "com.nick.instajet.action.url_download";

    // TODO: Rename parameters
    private static final String PARAM_FULL_URL = "com.nick.instajet.extra.full_url";

    private static final String apiUrlTemplate = "https://api.instagram.com/v1/media/shortcode/%1$s?access_token=%2$s";
    private static final String imageFilenameTemplate = "%1$s_%2$s_image.jpg";
    private static final String coverFilenameTemplate = "%1$s_%2$s_cover.jpg";
    private static final String videoFilenameTemplate = "%1$s_%2$s_video.mp4";

    private static final int DATA_TYPE_IMAGE = 0;
    private static final int DATA_TYPE_VIDEO = 1;
    private static final int DATA_TYPE_ERROR = -1;

    private String accessToken = "188264189.a3f9fc7.5c81d69e6de642d2a9dfdedfb1d237d8";
    private String fullUrl = "";

    /**
     * Starts this service to perform action URL_DOWNLOAD with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startUrlDownload(Context context, String fullUrl) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_URL_DOWNLOAD);
        intent.putExtra(PARAM_FULL_URL, fullUrl);
        context.startService(intent);
    }

    public DownloaderService() {
        super("DownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_URL_DOWNLOAD.equals(action)) {
                final String fullUrl = intent.getStringExtra(PARAM_FULL_URL);
                this.fullUrl = fullUrl;
                handleDownloadUrl(fullUrl);
            }
        }
    }

    @Override
    public void receiveApiResponse(JSONObject o) {
        if (o == null) {
            Log.e("asd", "o is null");
            showServerNoResponseNotif();
        } else {
            Log.e("asd", o.toString());
            continueHandleDownload(o);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleDownloadUrl(String fullUrl) {
        Log.e("asd", "handing download url");
        String shortcode = getShortcode(fullUrl);
        callInstagramApi(shortcode);
    }

    private void continueHandleDownload(JSONObject apiResponseObj) {
        Log.e("asd", "continue handing download");
        switch (getDataType(apiResponseObj)) {
            case DATA_TYPE_IMAGE :
                downloadImg(getImageUrl(apiResponseObj), getShortcode(apiResponseObj), getUsername(apiResponseObj));
                break;

            case DATA_TYPE_VIDEO :
                downloadVideo(getImageUrl(apiResponseObj), getVideoUrl(apiResponseObj), getShortcode(apiResponseObj), getUsername(apiResponseObj));
                break;

            default :
                Log.e("asd", "not img or video");
                break;
        }
    }

    private String getShortcode(String fullUrl) {
        // example full url: http://instagram.com/p/wYN2jBmoVQ/

        // to remove the final "/" to prevent confusion during split
        while (fullUrl.endsWith("/")) {
            fullUrl = fullUrl.substring(0, fullUrl.length() - 1);
        }

        // get the short code, which is the last item in the split array
        String[] urlStringSplit = fullUrl.split("/");
        String shortcode = urlStringSplit[urlStringSplit.length - 1];

        return shortcode;
    }

    private void callInstagramApi(String shortcode) {
        accessToken = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE).getString("accessToken", "notoken");

        // construct the api url
        String apiUrl = String.format(apiUrlTemplate, shortcode, accessToken);

        // execute the json getter
        InstagramApiHandlerTask dataGetter = new InstagramApiHandlerTask(this);
        dataGetter.execute(apiUrl);
    }

    private String getShortcode(JSONObject o) {
        try {
            JSONObject data = o.getJSONObject("data");
            String link = data.getString("link");
            String shortcode = getShortcode(link);
            return shortcode;

        } catch (JSONException e) {
            e.printStackTrace();
            return "no shortcode";
        }
    }

    private String getUsername(JSONObject o) {
        try {
            JSONObject data = o.getJSONObject("data");
            JSONObject user = data.getJSONObject("user");
            String username = user.getString("username");
            return username;

        } catch (JSONException e) {
            e.printStackTrace();
            return "no username";
        }
    }

    private int getDataType(JSONObject o) {
        try {
            JSONObject data = o.getJSONObject("data");
            String type = data.getString("type");
            switch (type) {
                case "image" :
                    return DATA_TYPE_IMAGE;

                case "video" :
                    return DATA_TYPE_VIDEO;

                default :
                    return DATA_TYPE_ERROR;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return DATA_TYPE_ERROR;
        }
    }

    private String getImageUrl(JSONObject o) {
        try {
            JSONObject data = o.getJSONObject("data");
            JSONObject images = data.getJSONObject("images");
            JSONObject stdResImage = images.getJSONObject("standard_resolution");
            String imageUrl = stdResImage.getString("url");

            return imageUrl;

        } catch (JSONException e) {
            e.printStackTrace();
            return "dead link";
        }
    }

    private String getVideoUrl(JSONObject o) {
        try {
            JSONObject data = o.getJSONObject("data");
            JSONObject videos = data.getJSONObject("videos");
            JSONObject stdResVideo = videos.getJSONObject("standard_resolution");
            String videoUrl = stdResVideo.getString("url");

            return videoUrl;

        } catch (JSONException e) {
            e.printStackTrace();
            return "dead link";
        }
    }

    private void downloadImg(String imgUrl, String shortcode, String username) {
        // TODO
        Log.e("asd", "handing download img " + imgUrl);
        String filename = String.format(imageFilenameTemplate, username, shortcode);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri parsedImageUri = Uri.parse(imgUrl);

        DownloadManager.Request imageRequest = new DownloadManager.Request(parsedImageUri);
        imageRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        imageRequest.allowScanningByMediaScanner();
        imageRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        dm.enqueue(imageRequest);
    }

    private void downloadVideo(String coverUrl, String videoUrl, String shortcode, String username) {
        // TODO
        Log.e("asd", "handing download vid " + videoUrl);
        String coverFilename = String.format(coverFilenameTemplate, username, shortcode);
        String videoFilename = String.format(videoFilenameTemplate, username, shortcode);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri parsedImageUri = Uri.parse(coverUrl);
        Uri parsedVideoUri = Uri.parse(videoUrl);

        DownloadManager.Request coverRequest = new DownloadManager.Request(parsedImageUri);
        coverRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        coverRequest.allowScanningByMediaScanner();
        coverRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, coverFilename);

        dm.enqueue(coverRequest);

        DownloadManager.Request videoRequest = new DownloadManager.Request(parsedVideoUri);
        videoRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        videoRequest.allowScanningByMediaScanner();
        videoRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoFilename);

        dm.enqueue(videoRequest);
    }

    private void showServerNoResponseNotif() {
        NotificationCompat.Builder n = new NotificationCompat.Builder(this);
        n.setSmallIcon(R.drawable.ic_launcher);
        n.setContentTitle("Download Failed");
        n.setContentText("Please try again.");
        n.setAutoCancel(true);

        Intent homeIntent = new Intent(this, Home.class);
        TaskStackBuilder sb = TaskStackBuilder.create(this);
        sb.addParentStack(Home.class);
        sb.addNextIntent(homeIntent);
        PendingIntent homePendingIntent = sb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        n.setContentIntent(homePendingIntent);

        NotificationCompat.BigTextStyle nbig = new NotificationCompat.BigTextStyle();
        nbig.setBigContentTitle("Download Failed");
        nbig.setSummaryText("Error code: 0x00bb6afa541");
        nbig.bigText("Media could not be downloaded. Please check the selection or the URL and try again.");
        n.setStyle(nbig);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(this.fullUrl.hashCode(), n.build());
    }
}
