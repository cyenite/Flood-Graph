package com.project.grace.floodmeterapp;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;

import static com.project.grace.floodmeterapp.Constant.LEVEL_1;
import static com.project.grace.floodmeterapp.Constant.LEVEL_2;
import static com.project.grace.floodmeterapp.Constant.LEVEL_3;
import static com.project.grace.floodmeterapp.Constant.LEVEL_4;
import static com.project.grace.floodmeterapp.Constant.LEVEL_5;
import static com.project.grace.floodmeterapp.Constant.NOTIF_TITLE;

public class ServiceProvider extends Service {

    private static final String TAG = "ServiceProvider";
    private boolean isRunning = false;
    private Timestamp time;
    float lastReading = 0.0f;
    private static BufferedReader reader;

    private final String strUrl = "http://philsensors.asti.dost.gov.ph/php/24hrs.php?stationid=954&fbclid=IwAR3uykazMjdYDWTvFFjADihonicb6hh57Fb1CHCfhbXTNjQ45WCbLjGNYoo";
    private URL url = null;
    private String data = "";
    private StringBuffer sb = new StringBuffer();
    double result = 0;
    HttpURLConnection connection;


    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");

        WaterLevelMonitoring monitoring = new WaterLevelMonitoring();
        monitoring.execute();

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        return Service.START_STICKY;

    }

    public void sendNotification(String title, String message) {

        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.i(TAG, "Service onDestroy");
    }


    class WaterLevelMonitoring extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            //            <editor-fold desc="Thrad for rainfall API">
            String line = "";
            while (true) {
                try {
                    url = new URL(strUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    InputStream iStream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(iStream));
                    line = reader.readLine();
                    JSONObject jsonObject = new JSONObject(line);
                    jsonObject.toString();

                    JSONArray jsonArray = jsonObject.getJSONArray("Data");
                    //For Values

                    ArrayList<Entry> jsonMap = new ArrayList<>();

                    JSONObject jObject = jsonArray.getJSONObject(0);
                    String dateRecord = jObject.getString("Datetime Read");

                    if (lastReading < Float.parseFloat(jObject.getString("Waterlevel"))) {
                        if (Float.parseFloat(jObject.getString("Waterlevel")) <= 1) {
                            sendNotification(NOTIF_TITLE, LEVEL_5);
                        } else if (Float.parseFloat(jObject.getString("Waterlevel")) <= 2) {
                            sendNotification(NOTIF_TITLE, LEVEL_4);
                        } else if (Float.parseFloat(jObject.getString("Waterlevel")) <= 3) {
                            sendNotification(NOTIF_TITLE, LEVEL_3);
                        } else if (Float.parseFloat(jObject.getString("Waterlevel")) <= 4) {
                            sendNotification(NOTIF_TITLE, LEVEL_2);
                        } else {
                            sendNotification(NOTIF_TITLE, LEVEL_1);
                        }
                    } else if (lastReading > Float.parseFloat(jObject.getString("Waterlevel")))
                        sendNotification("Water Level Monitoring", "The water level is decreasing!");
                    else {
                        sendNotification("Water Level Monitoring", "The water level is steady!");
                    }


                    lastReading = Float.parseFloat(jObject.getString("Waterlevel"));
                    // here you put ean as key and nr as value
                    Thread.sleep(240000);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;

                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
