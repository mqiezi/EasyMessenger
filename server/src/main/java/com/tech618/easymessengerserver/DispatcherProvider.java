package com.tech618.easymessengerserver;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by 82538 on 2018/6/3.
 */

public class DispatcherProvider extends ContentProvider {

    public static final String PROJECTION_MAIN[] = {"main"};

    //public static final Uri URI = Uri.parse("content://org.qiyi.video.svg.dispatcher/main");
    public static final String URI_SUFFIX="qiyi.svg.dispatcher";

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return DispatcherCursor.generateCursor(new Binder(){
            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                Log.d("DispatcherProvider", "onTransact called, code =" + code);
                return super.onTransact(code, data, reply, flags);
            }
        });
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
