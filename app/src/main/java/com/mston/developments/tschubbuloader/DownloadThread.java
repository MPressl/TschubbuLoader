//Copywrite (C) Martin Pressl 2017
package com.mston.developments.tschubbuloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * This thread initiates the download of the video.
 */

public class DownloadThread implements Runnable {
    private SetTagActivity context;
    private final String downloadLink;
    private final String title;
    private final String artist;
    private final int format;

    public DownloadThread(String link, String artist, String title,
                          SetTagActivity context, int format){
        this.downloadLink = link;
        this.context = context;
        this.title = title;
        this.artist = artist;
        this.format = format;

    }
    @Override
    public void run() {
        if(downloadLink == null){
            Log.d("Conversion Thread", " download link is null!");
            return;
        }
        boolean hasPermission = (ContextCompat.checkSelfPermission(this.context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            Log.d(TAG, "Has no permission! Ask!");
            context.askWritePermission();
            this.context.setDownloadStarted(false, false, null, null, null);
        }else {
            wGetter.downloadVideo(this.context, this.downloadLink,
                    this.format, this.artist, this.title);
            this.context.setDownloadStarted(false, true, null, null, null);
        }
        return;

    }
}
