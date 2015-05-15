package com.nick.instajet;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    private static final String ACTION_SHARE_URL_DOWNLOAD = "com.nick.instajet.action.share_url_download";
    private static final String ACTION_PROFILE_PIC_DOWNLOAD = "com.nick.instajet.action.profile_pic_download";
    private static final String ACTION_DIRECT_MEDIA_DOWNLOAD = "com.nick.instajet.action.direct_media_download";

    // TODO: Rename parameters
    private static final String PARAM_SHARE_URL = "com.nick.instajet.extra.share_url";
    private static final String PARAM_PROFILE_DATA_STRING = "com.nick.instajet.extra.profile_data_object";
    private static final String PARAM_MEDIA_DATA_STRING = "com.nick.instajet.extra.direct_media_object";

    private static final String MEDIA_API_REQUEST_TEMPLATE = "https://api.instagram.com/v1/media/shortcode/%1$s?access_token=%2$s";

    private static final String PROFILE_PIC_FILENAME_TEMPLATE = "%1$s_profile_pic.jpg";
    private static final String IMAGE_FILENAME_TEMPLATE = "%1$s_%2$s_image.jpg";
    private static final String COVER_FILENAME_TEMPLATE = "%1$s_%2$s_cover.jpg";
    private static final String VIDEO_FILENAME_TEMPLATE = "%1$s_%2$s_video.mp4";

    public static final int DATA_TYPE_IMAGE = 0;
    public static final int DATA_TYPE_VIDEO = 1;
    public static final int DATA_TYPE_ERROR = -1;

    private String accessToken = "";//"188264189.a3f9fc7.5c81d69e6de642d2a9dfdedfb1d237d8";

    private String shareUrl = "";
    private String profileDataString = "";
    private String mediaDataString = "";

    /**
     * Starts this service to perform action URL_DOWNLOAD with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startShareUrlDownload(Context context, String shareUrl) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_SHARE_URL_DOWNLOAD);
        intent.putExtra(PARAM_SHARE_URL, shareUrl);
        context.startService(intent);
    }

    public static void startProfilePicDownload(Context context, String profileDataString) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_PROFILE_PIC_DOWNLOAD);
        intent.putExtra(PARAM_PROFILE_DATA_STRING, profileDataString);
        context.startService(intent);
    }

    public static void startDirectMediaDownload(Context context, String mediaDataString) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_DIRECT_MEDIA_DOWNLOAD);
        intent.putExtra(PARAM_MEDIA_DATA_STRING, mediaDataString);
        context.startService(intent);
    }

    public DownloaderService() {
        super("DownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SHARE_URL_DOWNLOAD.equals(action)) {
                final String shareUrl = intent.getStringExtra(PARAM_SHARE_URL);
                this.shareUrl = shareUrl;
                handleShareUrlDownload(shareUrl);

            } else if (ACTION_PROFILE_PIC_DOWNLOAD.equals(action)) {
                final String profileDataString = intent.getStringExtra(PARAM_PROFILE_DATA_STRING);
                this.profileDataString = profileDataString;
                handleProfilePicDownload(profileDataString);

            } else if (ACTION_DIRECT_MEDIA_DOWNLOAD.equals(action)) {
                final String mediaDataString = intent.getStringExtra(PARAM_MEDIA_DATA_STRING);
                this.mediaDataString = mediaDataString;
                handleDirectMediaDownload(mediaDataString);
            }
        }
    }

    @Override
    public void receiveApiResponse(JSONObject apiResponseObj) {
        if (apiResponseObj == null) {
            Log.e("asd", "apiResponseObj is null");
            showServerNoResponseNotif();
        } else {
            Log.e("asd", apiResponseObj.toString());
            continueHandleShareUrlDownload(apiResponseObj);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleShareUrlDownload(String shareUrl) {
        Log.e("asd", "handing share url");
        String shortcode = getShortcode(shareUrl);
        sendMediaApiRequest(shortcode);
    }

    private void continueHandleShareUrlDownload(JSONObject responseObj) {
        Log.e("asd", "continue handing share download");
        switch (getDataType(responseObj)) {
            case DATA_TYPE_IMAGE :
                downloadImgWithShortcode(getImageUrl(responseObj), getShortcode(responseObj), getUsername(responseObj));
                break;

            case DATA_TYPE_VIDEO :
                downloadVideoWithShortcode(getImageUrl(responseObj), getVideoUrl(responseObj), getShortcode(responseObj), getUsername(responseObj));
                break;

            default :
                Log.e("asd", "not img or video");
                break;
        }
    }

    private void handleProfilePicDownload(String profileDataString) {
        try {
            JSONObject profileData = new JSONObject(profileDataString);
            String profilePicUrl = profileData.getString("profile_picture");
            String username = profileData.getString("username");
            downloadImgProfile(profilePicUrl, username);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "making json from string failed when doing profile pic");
        }
    }

    private void handleDirectMediaDownload(String mediaDataString) {
        JSONObject mediaData = new JSONObject();
        try {
            mediaData = new JSONObject(mediaDataString);

            switch (getDataTypeMediaObj(mediaData)) {
                case DATA_TYPE_IMAGE :
                    downloadImgDirect(getImageUrlMediaObj(mediaData), getUsernameMediaObj(mediaData), getShortcodeMediaObj(mediaData));
                    break;

                case DATA_TYPE_VIDEO :
                    downloadVideoDirect(getImageUrlMediaObj(mediaData), getVideoUrlMediaObj(mediaData), getUsernameMediaObj(mediaData), getShortcodeMediaObj(mediaData));
                    break;

                default :
                    Log.e("asd", "direct download idk what type");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "making json from string failed");
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

    private void sendMediaApiRequest(String shortcode) {
        accessToken = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE).getString("accessToken", "notoken");
        try {
            accessToken = URLEncoder.encode(accessToken, "UTF-8");
            shortcode = URLEncoder.encode(shortcode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("asd", e.getMessage());
            e.printStackTrace();
        }

        // construct the api url
        String apiUrl = String.format(MEDIA_API_REQUEST_TEMPLATE, shortcode, accessToken);

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

    private String getShortcodeMediaObj(JSONObject o) {
        try {
            String link = o.getString("link");
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

    private String getUsernameMediaObj(JSONObject o) {
        try {
            JSONObject user = o.getJSONObject("user");
            String username = user.getString("username");
            return username;

        } catch (JSONException e) {
            e.printStackTrace();
            return "no username";
        }
    }

    public static int getDataType(JSONObject o) {
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

    public static int getDataTypeMediaObj(JSONObject o) {
        try {
            String type = o.getString("type");
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

    private String getImageUrlMediaObj(JSONObject o) {
        try {
            JSONObject images = o.getJSONObject("images");
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

    private String getVideoUrlMediaObj(JSONObject o) {
        try {
            JSONObject videos = o.getJSONObject("videos");
            JSONObject stdResVideo = videos.getJSONObject("standard_resolution");
            String videoUrl = stdResVideo.getString("url");

            return videoUrl;

        } catch (JSONException e) {
            e.printStackTrace();
            return "dead link";
        }
    }

    private void downloadImgWithShortcode(String imgUrl, String shortcode, String username) {
        // TODO
        Log.e("asd", "handing download img " + imgUrl);
        String filename = String.format(IMAGE_FILENAME_TEMPLATE, username, shortcode);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri parsedImageUri = Uri.parse(imgUrl);

        DownloadManager.Request imageRequest = new DownloadManager.Request(parsedImageUri);
        imageRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        imageRequest.allowScanningByMediaScanner();
        imageRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        dm.enqueue(imageRequest);
    }

    private void downloadVideoWithShortcode(String coverUrl, String videoUrl, String shortcode, String username) {
        // TODO
        Log.e("asd", "handing download vid " + videoUrl);
        String coverFilename = String.format(COVER_FILENAME_TEMPLATE, username, shortcode);
        String videoFilename = String.format(VIDEO_FILENAME_TEMPLATE, username, shortcode);
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

    private void downloadImgProfile(String profilePicUrl, String username) {
        Log.e("asd", "handing download profile pic " + profilePicUrl);
        String filename = String.format(PROFILE_PIC_FILENAME_TEMPLATE, username);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri parsedImageUri = Uri.parse(profilePicUrl);

        DownloadManager.Request imageRequest = new DownloadManager.Request(parsedImageUri);
        imageRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        imageRequest.allowScanningByMediaScanner();
        imageRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        dm.enqueue(imageRequest);
    }

    private void downloadImgDirect(String imgUrl, String username, String shortcode) {
        Log.e("asd", "download image direct");
        String filename = String.format(IMAGE_FILENAME_TEMPLATE, username, shortcode);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri parsedImageUri = Uri.parse(imgUrl);

        DownloadManager.Request imageRequest = new DownloadManager.Request(parsedImageUri);
        imageRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        imageRequest.allowScanningByMediaScanner();
        imageRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        dm.enqueue(imageRequest);
    }

    private void downloadVideoDirect(String coverUrl, String videoUrl, String username, String shortcode) {
        Log.e("asd", "download video direct");
        String coverFilename = String.format(COVER_FILENAME_TEMPLATE, username, shortcode);
        String videoFilename = String.format(VIDEO_FILENAME_TEMPLATE, username, shortcode);
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
        nm.notify(this.shareUrl.hashCode(), n.build());
    }
}
