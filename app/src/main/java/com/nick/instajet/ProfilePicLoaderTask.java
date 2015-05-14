package com.nick.instajet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Nick on 12/5/2015.
 */
public class ProfilePicLoaderTask extends AsyncTask<Void, Void, Bitmap> {

    private boolean hasCache;
    private ImageView imageViewProfilePic;
    private String profilePicUrl;
    private HashMap<String, Bitmap> profilePicCache;

    public ProfilePicLoaderTask(ImageView imageViewProfilePic, String profilePicUrl, HashMap<String, Bitmap> profilePicCache) {
        this.imageViewProfilePic = imageViewProfilePic;
        this.profilePicUrl = profilePicUrl;
        this.profilePicCache = profilePicCache;
        this.hasCache = true;
    }

    public ProfilePicLoaderTask(ImageView imageViewProfilePic, String profilePicUrl) {
        this.imageViewProfilePic = imageViewProfilePic;
        this.profilePicUrl = profilePicUrl;
        this.profilePicCache = null;
        this.hasCache = false;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap profilePicBmp = null;
        InputStream in = null;
        try {
            in = new URL(profilePicUrl).openStream();
            profilePicBmp = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return profilePicBmp;
    }

    @Override
    protected void onPostExecute(Bitmap profilePicBmp) {
        if (hasCache) {
            profilePicCache.put(profilePicUrl, profilePicBmp);
        }
        imageViewProfilePic.setImageBitmap(profilePicBmp);
    }
}
