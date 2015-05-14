package com.nick.instajet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Nick on 12/5/2015.
 */
public class ImageViewLoaderTask extends AsyncTask<Void, Void, Bitmap> {

    private boolean hasCache;
    private ImageView imageView;
    private String imageUrl;
    private HashMap<String, Bitmap> imageCache;
    private ProgressBar progressBar;
    private boolean hasProgressBar;

    public ImageViewLoaderTask(ImageView imageView, ProgressBar progressBar, String imageUrl, HashMap<String, Bitmap> imageCache) {
        this.imageView = imageView;
        this.imageUrl = imageUrl;
        this.imageCache = imageCache;
        this.hasCache = true;
        this.hasProgressBar = true;
        this.progressBar = progressBar;
        this.progressBar.setVisibility(View.VISIBLE);
        Log.e("asd", ""+hasProgressBar);
    }

    public ImageViewLoaderTask(ImageView imageView, ProgressBar progressBar, String imageUrl) {
        this.imageView = imageView;
        this.imageUrl = imageUrl;
        this.imageCache = null;
        this.hasCache = false;
        this.hasProgressBar = true;
        this.progressBar = progressBar;
        this.progressBar.setVisibility(View.VISIBLE);
        Log.e("asd", ""+hasProgressBar);
    }

    public ImageViewLoaderTask(ImageView imageView, String imageUrl, HashMap<String, Bitmap> imageCache) {
        this.imageView = imageView;
        this.imageUrl = imageUrl;
        this.imageCache = imageCache;
        this.hasCache = true;
        this.hasProgressBar = false;
    }

    public ImageViewLoaderTask(ImageView imageView, String imageUrl) {
        this.imageView = imageView;
        this.imageUrl = imageUrl;
        this.imageCache = null;
        this.hasCache = false;
        this.hasProgressBar = false;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap profilePicBmp = null;
        InputStream in = null;
        try {
            in = new URL(imageUrl).openStream();
            profilePicBmp = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return profilePicBmp;
    }

    @Override
    protected void onPostExecute(Bitmap imageBmp) {
        if (hasCache) {
            imageCache.put(imageUrl, imageBmp);
        }
        imageView.setImageBitmap(imageBmp);
        if (hasProgressBar) {
            progressBar.setVisibility(View.GONE);
        } else {
            Log.e("asd", "no progress bar");
        }
    }
}
