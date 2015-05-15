package com.nick.instajet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Nick on 15/5/2015.
 */
public class PhotoStreamListAdapter extends BaseAdapter implements ListAdapter {

    public static final String POSTED_DATE_TEMPLATE = "d MMM yyyy";
    public static final String POSTED_TIME_TEMPLATE = "h : mm a";

    private final Activity caller;
    private final JSONArray mediaList;

    private HashMap<String, Bitmap> imageCache;

    public PhotoStreamListAdapter(Activity caller, JSONArray mediaList) {
        super();
        this.mediaList = mediaList;
        this.caller = caller;
        this.imageCache = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mediaList.length();
    }

    @Override
    public Object getItem(int position) {
        return mediaList.opt(position);
    }

    @Override
    public long getItemId(int position) {
        JSONObject user = mediaList.optJSONObject(position);
        return user.optLong("id");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = caller.getLayoutInflater().inflate(R.layout.listitem_photo_stream, null);
        JSONObject mediaData = mediaList.optJSONObject(position);
        setupMediaItem(view, mediaData, position);
        return view;
    }

    private void setupMediaItem(View rowView, final JSONObject mediaData, final int position) {
        Button buttonDownloadMedia = (Button) rowView.findViewById(R.id.ButtonDownloadMedia);
        buttonDownloadMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloaderService.startDirectMediaDownload(caller, mediaData.toString());
            }
        });

        // username
        TextView textViewUsername = (TextView) rowView.findViewById(R.id.TextViewUsername);
        JSONObject user = mediaData.optJSONObject("user");
        String username = user.optString("username", "(Username not available)");
        textViewUsername.setText(username);

        // data type
        int dataType = getDataType(mediaData);
        TextView textViewMediaType = (TextView) rowView.findViewById(R.id.TextViewMediaType);
        textViewMediaType.setText(getDataTypeText(dataType));

        // image itself
        ImageView imageViewPhotoStreamImage = (ImageView) rowView.findViewById(R.id.ImageViewPhotoStreamImage);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.ProgressBarImageLoadProgress);
        JSONObject images = mediaData.optJSONObject("images");
        JSONObject stdRes = images.optJSONObject("standard_resolution");
        String imageUrl = stdRes.optString("url");
        loadMediaImage(imageViewPhotoStreamImage, progressBar, imageUrl);

        //date and time
        TextView textViewPostedDate = (TextView) rowView.findViewById(R.id.TextViewPostedDate);
        TextView textViewPostedTime = (TextView) rowView.findViewById(R.id.TextViewPostedTime);
        String postedTimeString = mediaData.optString("created_time", null); // in seconds
        Calendar c = Calendar.getInstance(Locale.getDefault());
        Long postedTime = Long.parseLong(postedTimeString) * 1000; // convert to mil
        c.setTimeInMillis(postedTime);
        c.setTimeZone(TimeZone.getDefault());
        Date d = new Date();
        d.setTime(postedTime);
        String dateString = new SimpleDateFormat(POSTED_DATE_TEMPLATE).format(c.getTime());
        textViewPostedDate.setText(dateString);
        String timeString = new SimpleDateFormat(POSTED_TIME_TEMPLATE).format(c.getTime());
        textViewPostedTime.setText(timeString);
        Log.i("asd", c.getTime().getTime() + "");
        Log.i("asd", postedTimeString);
        Log.i("asd", postedTime +"");
        Log.i("asd", dateString);
        Log.i("asd", timeString);
    }

    public static int getDataType(JSONObject o) {
        try {
            String type = o.getString("type");
            switch (type) {
                case "image" :
                    return DownloaderService.DATA_TYPE_IMAGE;

                case "video" :
                    return DownloaderService.DATA_TYPE_VIDEO;

                default :
                    return DownloaderService.DATA_TYPE_ERROR;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return DownloaderService.DATA_TYPE_ERROR;
        }
    }

    private String getDataTypeText(int dataType) {
        switch (dataType) {
            case DownloaderService.DATA_TYPE_IMAGE :
                return "Image";

            case DownloaderService.DATA_TYPE_VIDEO :
                return "Video";

            case DownloaderService.DATA_TYPE_ERROR :
                return "Error";

            default:
                return "Unknown";
        }
    }

    private void loadMediaImage(ImageView imageViewPhotoStreamImage, ProgressBar progressBar, String imageUrl) {
        if (imageCache.containsKey(imageUrl)) {
            imageViewPhotoStreamImage.setImageBitmap(imageCache.get(imageUrl));
        } else {
            new ImageViewLoaderTask(imageViewPhotoStreamImage, progressBar, imageUrl, imageCache).execute();
        }

        if (imageCache.size() >= 51) {
            Log.e("asd", "clearing old cache to save space");
            imageCache = new HashMap<>();
        }
    }
}
