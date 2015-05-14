package com.nick.instajet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Nick on 15/5/2015.
 */
public class PhotoStreamListAdapter extends BaseAdapter implements ListAdapter {

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
        setupMediaItem(view, mediaData);
        return view;
    }

    private void setupMediaItem(View rowView, JSONObject mediaData) {
        TextView textViewUsername = (TextView) rowView.findViewById(R.id.TextViewUsername);
        JSONObject user = mediaData.optJSONObject("user");
        String username = user.optString("username", "(Username not available)");
        textViewUsername.setText(username);

        int dataType = getDataType(mediaData);
        TextView textViewMediaType = (TextView) rowView.findViewById(R.id.TextViewMediaType);
        textViewMediaType.setText(getDataTypeText(dataType));

        ImageView imageViewPhotoStreamImage = (ImageView) rowView.findViewById(R.id.ImageViewPhotoStreamImage);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.ProgressBarImageLoadProgress);

        JSONObject images = mediaData.optJSONObject("images");
        JSONObject stdRes = images.optJSONObject("standard_resolution");
        String imageUrl = stdRes.optString("url");
        loadMediaImage(imageViewPhotoStreamImage, progressBar, imageUrl);
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
    }
}
