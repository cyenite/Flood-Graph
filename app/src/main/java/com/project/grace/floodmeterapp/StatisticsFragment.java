package com.project.grace.floodmeterapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatisticsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageView imageView;
    BarChart barChart;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String thisDate;
    ArrayList<BarEntry> entries = new ArrayList<>();
    double locale[];
    private TextView txtWeather;
    SpinnerAdapter adapter;
    String weatherInfo;
    View rootView;
    private final String message = "You are not connected to the internet!";

    private OnFragmentInteractionListener mListener;

    public static StatisticsFragment getInstance() {
        return new StatisticsFragment();
    }

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        locale = (double[]) getArguments().get("locale");

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        Date todayDate = new Date();
        thisDate = df.format(todayDate);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("crowdsource").child(thisDate);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_statistics, container, false);
        barChart = (BarChart) rootView.findViewById(R.id.bar_chart);
        ArrayList<ItemData> list = new ArrayList<>();
        list.add(new ItemData("Today", R.drawable.sun));
        list.add(new ItemData("Yesterday", R.drawable.sun));
        list.add(new ItemData("Weekly", R.drawable.sun));
        list.add(new ItemData("Monthly", R.drawable.sun));
        list.add(new ItemData("Yearly", R.drawable.sun));

        Spinner sp = rootView.findViewById(R.id.chart_spinner);
        adapter = new SpinnerAdapter(this.getActivity(), R.layout.spinner_weather, R.id.txt, list);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(this);

        populateChart();
        return rootView;
    }

    private String getYesterdayDateString() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        return df.format(yesterday());
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public boolean isInsideLocation(int nvert, double[] vertx, double[] verty, double testx, double testy) {
        int i, j;
        boolean c = false;
        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
            if (((verty[i] > testy) != (verty[j] > testy)) &&
                    (testx < (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i]))
                c = !c;
        }
        return c;
    }

    private void populateChart() {

        ProgressDialog progressDialog;
        progressDialog = ProgressDialog.show(rootView.getContext(), "Fetching Data from database...", "Please wait");
        barChart.setNoDataText("Please wait while fetching data from database.");
        myRef.addValueEventListener(new ValueEventListener() {
            ArrayList<BarEntry> entries = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int cloudy = 0;
                int lightRain = 0;
                int mediumRain = 0;
                int heavyRain = 0;
                int thunderstorm = 0;
                int unknown = 0;

                CrowdSource rf = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    rf = snapshot.getValue(CrowdSource.class);

                    if (isInsideLocation((float) rf.getLat(), (float) rf.getLon())) {
                        switch (rf.getTag()) {
                            case "Cloudy":
                                cloudy++;
                                break;
                            case "Light Rainfall":
                                lightRain++;
                                break;
                            case "Medium Rainfall":
                                mediumRain++;
                                break;
                            case "Heavy Rainfall":
                                heavyRain++;
                                break;
                            case "Thunderstorm":
                                thunderstorm++;
                                break;
                        }

                    } else {
                        unknown++;
                    }
                }

                entries.add(new BarEntry(1f, cloudy, "Cloudy"));
                entries.add(new BarEntry(2f, lightRain, "Light Rainfall"));
                entries.add(new BarEntry(3f, mediumRain, "Medium Rainfall"));
                entries.add(new BarEntry(4f, heavyRain, "Heavy Rainfall"));
                entries.add(new BarEntry(5f, thunderstorm, "Thunderstorm"));
                entries.add(new BarEntry(6f, unknown, "Unknown Ratings."));


                if (entries.size() > 0) {
                    String[] dataLables = new String[]{"Cloudy", "Light Rainfall", "Medium Rainfall", "Heavy Rainfall", "Thunderstorm", "Unknown"};
                    BarDataSet barSet = new BarDataSet(entries, "");
                    ArrayList<IBarDataSet> barDataSet = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();
                    Collections.addAll(labels, dataLables);

                    barSet.setStackLabels(new String[]{"Cloudy", "Light Rainfall", "Medium Rainfall", "Heavy Rainfall", "Thunderstorm"});
                    barSet.setColor(Color.rgb(76, 136, 247));
                    barSet.setBarBorderColor(Color.rgb(243, 106, 53));
                    barSet.setValueTextColor(Color.rgb(254, 254, 254));

                    barSet.setColors(new int[]{
                                    R.color.color1, R.color.color2, R.color.color3,
                                    R.color.color4, R.color.color5, R.color.color6}, getContext());

                    barSet.setHighLightAlpha(110);

                    barSet.setValueTextSize(10f);
                    barSet.setAxisDependency(YAxis.AxisDependency.LEFT);


                    BarData barData = new BarData(barSet);
                    barData.setValueTextSize(14f);

                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setValueFormatter((value, axis) -> {
                        int intValue = (int) value-1;
                        return (labels.size() > intValue && intValue >= 0) ? labels.get(intValue) : "";
                    });
                    xAxis.setTextColor(Color.rgb(255, 255, 255));
                    xAxis.setGranularity(1f);
                    xAxis.setGranularityEnabled(true);

                    Legend legend = barChart.getLegend();
                    legend.setTextColor(Color.rgb(255, 255, 255));
                    legend.setTextSize(5f);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {//wait for activity to load
                        @Override
                        public void run() {
                            //for(BarEntry b : avgScores)
                            //  Log.d("entry", ""+b.getX()+" "+b.getY());
                            barChart.setData(barData);
                            barChart.setFitBars(true); // make the x-axis fit exactly all bars
                            barChart.invalidate();
                        }
                    }, 200);


                } else {
                    barChart.setNoDataText("No data available!");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


            public boolean isInsideLocation(float testx, float testy) {
                double[] vertx = new double[]{
                        7.084118f, 7.086073f, 7.085497f, 7.087450f
                };
                double[] verty = new double[]{
                        125.615469f, 125.616718f, 125.614337f,125.617858f
                };

                int nvert = 4;
                int i, j;
                boolean c = false;
                for (i = 0, j = nvert - 1; i < nvert; j = i++) {
                    if (((verty[i] > testy) != (verty[j] > testy)) &&
                            (testx < (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i]))
                        c = !c;
                }
                return c;
            }
        });


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        barChart.invalidate();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Spinner s = view.findViewById(R.id.chart_spinner);
        try {
            if (s.getSelectedItem() != null) {
                System.out.println(s.getSelectedItem());
                if (s.getSelectedItem().equals("Today")) {
                    myRef = database.getReference("crowdsource").child(thisDate);
                    populateChart();
                } else if (s.getSelectedItem().equals("Yesterday")) {
                    myRef = database.getReference("crowdsource").child(getYesterdayDateString());
                    populateChart();
                } else if (s.getSelectedItem().equals("Weekly")) {
//                    myRef = database.getReference("crowdsource").child(thisDate);
                } else if (s.getSelectedItem().equals("Monthly")) {
//                    myRef = database.getReference("crowdsource").child(thisDate);
                } else if (s.getSelectedItem().equals("Yearly")) {

                }
            } else {
                myRef = database.getReference("crowdsource").child(thisDate);
            }
            System.out.println(String.valueOf(s.getSelectedItemPosition()));
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    class LabelFormatter implements IAxisValueFormatter {
        private final String[] mLabels;

        public LabelFormatter(String[] labels) {
            mLabels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mLabels[(int) value];
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

