package com.ques_4_ass;

import android.net.Uri;

public class ImageItem {
    public final Uri uri;
    public final String name;
    public final long size;
    public final long lastModified;

    public ImageItem(Uri uri, String name, long size, long lastModified) {
        this.uri = uri;
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
    }
}
