package com.example.shoatman.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.*;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;

import com.android.volley.AuthFailureError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;

import java.nio.charset.MalformedInputException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;

import javax.crypto.NoSuchPaddingException;


public class MainActivity extends AppCompatActivity {

    /* Configurations */
    public final static String AUTHORITY_URL = "https://login.microsoftonline.com/microsoft.com";
    /*  Client ID is used by the app to identify itself in Azure AD */
    public final static String CLIENT_ID = "ad01133f-1c15-4c61-ad18-0b9478a4d2b3";
    public static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    /* Graph Resource ID or URI */
    public final static String RESOURCE = "34c122e9-32d6-431a-a7ae-c57da0767fd3";
    private AuthenticationContext mAuthContext;
    private static final String LOG_TAG = "ADAL";

    private TextView mOutputMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mOutputMessageView = (TextView) findViewById(R.id.outputMessage);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the authentication context.
        if (mAuthContext != null) {
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void performLogin (final View v)
    {
        try {
            mAuthContext = new AuthenticationContext(MainActivity.this, MainActivity.AUTHORITY_URL, false);
        }catch(NoSuchAlgorithmException exAlg){
            Log.e(LOG_TAG, exAlg.getMessage());
        }catch(NoSuchPaddingException exPad){
            Log.e(LOG_TAG, exPad.getMessage());
        }

        mAuthContext.acquireToken(MainActivity.this, MainActivity.RESOURCE,
                CLIENT_ID, REDIRECT_URI, PromptBehavior.Always,
                new AuthenticationCallback<AuthenticationResult>()
                {
                    @Override
                    public void onError(Exception exc) {
                        Log.e(LOG_TAG, exc.getMessage());
                    }

                    @Override
                    public void onSuccess(AuthenticationResult result)
                    {

                        if (result != null && !result.getIdToken().isEmpty())
                        {
                            final String bearerHeader = "Bearer " + result.getAccessToken();
                            JsonArrayRequest jsObjRequest = new JsonArrayRequest
                                    ("http://10.0.2.2:5000/api/claims", new Response.Listener<JSONArray>() {

                                        @Override
                                        public void onResponse(JSONArray response) {
                                            mOutputMessageView.setText("Response: " + response.toString());
                                        }
                                    }, new Response.ErrorListener() {

                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // TODO Auto-generated method stub
                                            Log.e(LOG_TAG, error.getMessage());
                                        }
                                    }){
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String>  params = new HashMap<String, String>();
                                            params.put("Authorization", bearerHeader);
                                            return params;
                                        }
                                    };

                            VolleyQueueSingleton volley = VolleyQueueSingleton.getInstance(v.getContext().getApplicationContext());
                            volley.addToRequestQueue(jsObjRequest);
                            //mOutputMessageView.setText(result.getIdToken());
                            Log.d(LOG_TAG, "ID Token: " + result.getIdToken());
                            Log.d(LOG_TAG, "Access Token: " + result.getAccessToken());
                        }else{
                            Log.e(LOG_TAG, "ID Token is not available");
                        }
                    }
                });
    }


}
