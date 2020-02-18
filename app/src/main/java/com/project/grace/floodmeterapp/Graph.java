package com.project.grace.floodmeterapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.Collection;
import java.util.Collections;


public class Graph extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private LineChart lineChart;
    private static String TAG = "Rain Gauge Chart";
    private static LineData lineData;
    private static InputStream iStream;
    private static BufferedReader reader;

    private final String strUrl = "http://philsensors.asti.dost.gov.ph/php/24hrs.php?stationid=954&fbclid=IwAR3uykazMjdYDWTvFFjADihonicb6hh57Fb1CHCfhbXTNjQ45WCbLjGNYoo";
    private URL url = null;
    private String data = "";
    private StringBuffer sb = new StringBuffer();
    double result = 0;
    HttpURLConnection connection;


    private final String message = "You are not connected to the internet!";
    private boolean isDataConstant = true;
    private View rootView;
    private ProgressDialog progressDialog;

    private OnFragmentInteractionListener mListener;

    public static Graph getInstance() {
        return new Graph();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_graph, container, false);

        setupChart();

        WaterLevelMonitoring viewData = new WaterLevelMonitoring();
        viewData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }


    private void setupChart() {
        lineChart = (LineChart) rootView.findViewById(R.id.line_chart);
        lineChart.setNoDataText("Tap to refresh.");
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void showAlert(String message) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
            dialog.setMessage(message);
            dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                }
            });
            dialog.show();
        } catch (Exception e) {
        }
    }


    class WaterLevelMonitoring extends AsyncTask<Void, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            //            <editor-fold desc="Thrad for rainfall API">
            try {
                url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                iStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(iStream));
                String line = "";

                while ((line = reader.readLine()) != null) {
                    data += line;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (data.equals("")) {
                        isDataConstant = true;
                    } else {
                        isDataConstant = false;
                    }

                    JSONObject jsonObject = new JSONObject(data);
                    jsonObject.toString();

                    JSONArray jsonArray = jsonObject.getJSONArray("Data");
                    //For Values

                    ArrayList<Entry> jsonMap = new ArrayList<>();

                    int pos = 0;
                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        String dateRecord = jObject.getString("Datetime Read");
                        float waterLevel = Float.parseFloat(jObject.getString("Waterlevel"));
                        Timestamp time = Timestamp.valueOf(jObject.getString("Datetime Read"));
                        jsonMap.add(new Entry((float) (pos + 2), waterLevel));
                        pos++;
                        // here you put ean as key and nr as value
                    }

                    LineDataSet set1 = new LineDataSet(jsonMap, "Water Level");
                    set1.setFillAlpha(110);
                    set1.setLineWidth(2.5f);
                    set1.setColor(Color.rgb(66, 103, 178));
                    set1.setCircleColor(Color.rgb(240, 238, 70));
                    set1.setCircleRadius(2f);
                    set1.setFillColor(Color.rgb(240, 238, 70));
                    set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    set1.setDrawValues(true);
                    set1.setValueTextSize(5f);
                    set1.setValueTextColor(Color.rgb(240, 238, 70));
                    set1.setLineWidth(2f);
                    set1.setAxisDependency(YAxis.AxisDependency.LEFT);

                    lineData = new LineData();
                    lineData.addDataSet(set1);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);
                    lineData = new LineData(dataSets);
                    lineChart.getAxisLeft().setTextColor(Color.rgb(247, 77, 24));
                    lineChart.getXAxis().setTextColor(Color.rgb(247, 77, 24));
                    lineChart.getLegend().setTextColor(Color.rgb(247, 77, 24));


                    lineChart.setData(lineData);
                    lineChart.setData(lineData);
                    if (iStream != null && reader != null) {
                        reader.close();
                        iStream.close();
                    }
                } catch (JSONException ignored) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.disconnect();
            }
            lineChart.notifyDataSetChanged();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            lineChart.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(rootView.getContext(),
                    "Fetching API Data...",
                    "Please patiently wait.");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            lineChart.notifyDataSetChanged();
            showAlert("New Data!");
        }
    }

}
