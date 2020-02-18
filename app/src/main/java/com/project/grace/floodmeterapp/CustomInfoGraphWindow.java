package com.project.grace.floodmeterapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomInfoGraphWindow implements GoogleMap.InfoWindowAdapter {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final View mWindow;
    private final Context mContext;

    private StorageReference mStorageRef;
    private String thisDate;
    private Bitmap my_image;
    private static LineData lineData;
    private ArrayList<Entry> jsonMap;

    public CustomInfoGraphWindow(Context context, ArrayList<Entry> entry) {
        this.mContext = context;
        this.jsonMap = entry;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_graph_info_window, null);
    }

    private void setupChart(View view) {

        if (jsonMap.size() > 0) {

            LineChart lineChart = view.findViewById(R.id.line_chart_graph);
            lineChart.setNoDataText("Tap to refresh.");

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
        }
    }

    private void renderWindowText(Marker marker, View view) {

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        Date todayDate = new Date();
        String thisDate = df.format(todayDate);

        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.title);

        if (!title.equals(""))
            tvTitle.setText(title);

        String snippet = marker.getSnippet();
        TextView tvSnippest = view.findViewById(R.id.snippet);

        if (!snippet.equals(""))
            tvSnippest.setText(snippet);


        setupChart(view);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}
