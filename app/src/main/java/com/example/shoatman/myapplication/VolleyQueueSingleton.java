package com.example.shoatman.myapplication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import android.content.Context;


/**
 * https://developer.android.com/training/volley/requestqueue.html
 */
public class VolleyQueueSingleton {
    private static VolleyQueueSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private VolleyQueueSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyQueueSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyQueueSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
