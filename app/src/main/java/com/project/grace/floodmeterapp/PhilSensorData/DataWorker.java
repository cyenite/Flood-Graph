package com.project.grace.floodmeterapp.PhilSensorData;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.project.grace.floodmeterapp.Graph;

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

public class DataWorker {

    private static InputStream iStream;
    private static BufferedReader reader;

    private final String strUrlMcArthur = "http://philsensors.asti.dost.gov.ph/php/24hrs.php?stationid=1907";
    private final String strUrlLacsonBirdge = "http://philsensors.asti.dost.gov.ph/php/24hrs.php?stationid=1446";
    private final String strUrlMatinaBridge = "http://philsensors.asti.dost.gov.ph/php/24hrs.php?stationid=954&fbclid=IwAR3uykazMjdYDWTvFFjADihonicb6hh57Fb1CHCfhbXTNjQ45WCbLjGNYoo";
    private URL url = null;
    private String data = "";
    private StringBuffer sb = new StringBuffer();
    private HttpURLConnection connection;

    private ArrayList<Entry> wlmsDataMcArthur = new ArrayList<>();
    private ArrayList<Entry> wlmsDataLacson = new ArrayList<>();
    private ArrayList<Entry> wlmsDataMatina = new ArrayList<>();

    public DataWorker() {
        WaterLevelMonitoring viewData = new WaterLevelMonitoring();
        viewData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public ArrayList<Entry> getMacArthurBridgeAPIData(){
        return wlmsDataMcArthur;
    }

    public ArrayList<Entry> getLacsonBridgeAPIData(){
        return wlmsDataLacson;
    }

    public ArrayList<Entry> getMatinaBridgeAPIData(){
        return wlmsDataMatina;
    }


    class WaterLevelMonitoring extends AsyncTask<Void, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            //            <editor-fold desc="Thrad for rainfall API">
            try {

                String line = "";


                //Lacson Bridge
                url = new URL(strUrlLacsonBirdge);
                connection = (HttpURLConnection) url.openConnection();
                iStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(iStream));


                while ((line = reader.readLine()) != null) {
                    data += line;
                }

                if (!data.equals("")) {
                    JSONObject jsonObject = new JSONObject(data);
                    jsonObject.toString();

                    JSONArray jsonArray = jsonObject.getJSONArray("Data");
                    //For Values
                    int pos = 0;
                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        String dateRecord = jObject.getString("Datetime Read");
                        float waterLevel = Float.parseFloat(jObject.getString("Waterlevel"));
                        Timestamp time = Timestamp.valueOf(jObject.getString("Datetime Read"));
                        wlmsDataLacson.add(new Entry((float) (pos + 2), waterLevel));
                        pos++;
                        // here you put ean as key and nr as value
                    }

                }


                //Mc Arthur Birdge
                url = new URL(strUrlMcArthur);
                connection = (HttpURLConnection) url.openConnection();
                iStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(iStream));
                line = "";
                data = "";

                while ((line = reader.readLine()) != null) {
                    data += line;
                }


                if (!data.equals("")) {
                    JSONObject jsonObject = new JSONObject(data);
                    jsonObject.toString();

                    JSONArray jsonArray = jsonObject.getJSONArray("Data");
                    //For Values
                    int pos = 0;
                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        String dateRecord = jObject.getString("Datetime Read");
                        float waterLevel = Float.parseFloat(jObject.getString("Waterlevel"));
                        Timestamp time = Timestamp.valueOf(jObject.getString("Datetime Read"));
                        wlmsDataMcArthur.add(new Entry((float) (pos + 2), waterLevel));
                        pos++;
                        // here you put ean as key and nr as value
                    }
                }

                //Matina Pangi Birdge
                url = new URL(strUrlMatinaBridge);
                connection = (HttpURLConnection) url.openConnection();
                iStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(iStream));
                line = "";
                data = "";

                while ((line = reader.readLine()) != null) {
                    data += line;
                }


                if (!data.equals("")) {
                    JSONObject jsonObject = new JSONObject(data);
                    jsonObject.toString();

                    JSONArray jsonArray = jsonObject.getJSONArray("Data");
                    //For Values
                    int pos = 0;
                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        String dateRecord = jObject.getString("Datetime Read");
                        float waterLevel = Float.parseFloat(jObject.getString("Waterlevel"));
                        Timestamp time = Timestamp.valueOf(jObject.getString("Datetime Read"));
                        wlmsDataMatina.add(new Entry((float) (pos + 2), waterLevel));
                        pos++;
                        // here you put ean as key and nr as value
                    }
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException ignored) {
                ignored.printStackTrace();
            } finally {
                try {
                    if (iStream != null && reader != null) {
                        reader.close();
                        iStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.disconnect();
            }
            return null;
        }
    }
}
