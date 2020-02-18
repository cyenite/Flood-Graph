package com.project.grace.floodmeterapp;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.grace.floodmeterapp.PhilSensorData.DataWorker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//import com.google.firebase.database.DataSnapshot;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private GoogleMap map;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference refRainFall;
    private FirebaseDatabase database;
    private boolean hasLocation = false;
    private double[] locale;
    private SupportMapFragment mapFragment;
    private View view;
    private ArrayList<CrowdSource> sources = new ArrayList<>();

    private StorageReference mStorageRef;

    public static MapsFragment getInstance() {
        return new MapsFragment();
    }


    public static MapsFragment newInstance(String param1, String param2) {
        MapsFragment frag = new MapsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        frag.setArguments(args);


        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //add 8/8/18
//        setContentView(R.layout.activity_main);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }


    //Changes...
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        Bundle args = getArguments();
        if (args != null && args.containsKey("locale")) {
            locale = (double[]) getArguments().get("locale");
            hasLocation = true;
        }


        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        Date todayDate = new Date();
        String thisDate = df.format(todayDate);

        database = FirebaseDatabase.getInstance();
        System.out.println(database);
        refRainFall = database.getReference("crowdsource").child(thisDate);

        mStorageRef = FirebaseStorage.getInstance().getReference("crowdsource").child(thisDate);
        ArrayList<CrowdSource> data = new ArrayList<>();

        refRainFall.addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Get map of users in datasnapshot
                    if (dataSnapshot.hasChildren()) {
//                        collectCrowdsource((Map<String, Object>) Objects.requireNonNull(dataSnapshot.getValue()));

                        Map<String, CrowdSource> td = new HashMap<String, CrowdSource>();
                        for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                            CrowdSource cs = jobSnapshot.getValue(CrowdSource.class);
                            td.put(jobSnapshot.getKey(), cs);
                        }

                        sources = new ArrayList<>(td.values());
                        List<String> keys = new ArrayList<String>(td.keySet());
                        setupMapMarkers();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //handle databaseError
                    System.out.printf("error");
                }
            });
    }

    private void setupMapMarkers() {
//

        int height = 52;
        int width = 52;
        BitmapDrawable bit = null;
        Bitmap b, smallMarker;

        if (!sources.isEmpty()) {
            for (CrowdSource rf : sources) {
                if (rf != null) {
                    switch (rf.getTag()) {
                        case "Cloudy":
                            bit = (BitmapDrawable) view.getResources().getDrawable(R.drawable.cloudy);
                            b = bit.getBitmap();
                            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            map.addMarker(new MarkerOptions().position(new LatLng(rf.getLat(), rf.getLon()))
                                .title("Cloudy")
                                .snippet(rf.getUserID()))
                                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            break;
                        case "Light Rainfall":
                            bit = (BitmapDrawable) view.getResources().getDrawable(R.drawable.rain_low);
                            b = bit.getBitmap();
                            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            map.addMarker(new MarkerOptions().position(new LatLng(rf.getLat(), rf.getLon()))
                                .title("Light rainfall")
                                .snippet(rf.getUserID()))
                                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        case "Medium Rainfall":
                            bit = (BitmapDrawable) view.getResources().getDrawable(R.drawable.rain_medium);
                            b = bit.getBitmap();
                            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            map.addMarker(new MarkerOptions().position(new LatLng(rf.getLat(), rf.getLon()))
                                .title("Medium rainfall")
                                .snippet(rf.getUserID()))
                                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            break;
                        case "Heavy Rainfall":
                            bit = (BitmapDrawable) view.getResources().getDrawable(R.drawable.rain_high);
                            b = bit.getBitmap();
                            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            map.addMarker(new MarkerOptions().position(new LatLng(rf.getLat(), rf.getLon()))
                                .title("Heavy rainfall")
                                .snippet(rf.getUserID()))
                                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            break;
                        case "Thunderstorm":
                            bit = (BitmapDrawable) view.getResources().getDrawable(R.drawable.thunder);
                            b = bit.getBitmap();
                            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            map.addMarker(new MarkerOptions().position(new LatLng(rf.getLat(), rf.getLon()))
                                .title("Thunderstorm")
                                .snippet(rf.getUserID()))
                                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            break;
                    }
                } else {

                    bit = (BitmapDrawable) view.getResources().getDrawable(R.drawable.unknown_cloud);
                    b = bit.getBitmap();
                    smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    map.addMarker(new MarkerOptions().position(new LatLng(rf.getLat(), rf.getLon()))
                        .title("No Rating")
                        .snippet(rf.getUserID()))
                        .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                }//endif
            }
            DataWorker dataWorker = new DataWorker();
            map.setOnMarkerClickListener( s ->{
                if(s.getSnippet().equals("LACSON WLMS API")){
                    map.setInfoWindowAdapter(new CustomInfoGraphWindow(getActivity(), dataWorker.getLacsonBridgeAPIData()));
                }else if(s.getSnippet().equals("MATINA WLMS API")){
                    map.setInfoWindowAdapter(new CustomInfoGraphWindow(getActivity(), dataWorker.getMatinaBridgeAPIData()));
                } else if(s.getSnippet().equals("MC ARTHUR WLMS API")) {
                    map.setInfoWindowAdapter(new CustomInfoGraphWindow(getActivity(), dataWorker.getMacArthurBridgeAPIData()));
                }else{
                    map.setInfoWindowAdapter(new CustomInfoWindow(getActivity()));
                }
                return false;
            });
        }
    }




    private void drawBounds(int stroke, int fill) {
        PolygonOptions polygonOptions = new PolygonOptions()
            .add(new LatLng(7.085500, 125.614343))
            .add(new LatLng(7.084122, 125.615475))
            .add(new LatLng(7.086071, 125.617847))
            .add(new LatLng(7.087436, 125.616721))
            .strokeColor(stroke)
            .fillColor(fill);

        map.addPolygon(polygonOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMinZoomPreference(10);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);

        drawBounds(Color.BLUE, 0x1F00FF00);

        //Changes...
        if (hasLocation) {
            LatLng engineering = new LatLng(locale[0], locale[1]);
            map.moveCamera(CameraUpdateFactory.newLatLng(engineering));
        } else {
            LatLng engineering = new LatLng(7.085765, 125.616028);
            map.moveCamera(CameraUpdateFactory.newLatLng(engineering));
        }

        map.addMarker(new MarkerOptions().position(new LatLng(7.235784, 125.455271))
            .title("LACSON-LAMANAN BRIDGE, DAVAO CITY").snippet("LACSON WLMS API"));

        map.addMarker(new MarkerOptions().position(new LatLng(7.085774, 125.557903))
            .title("MATINA PANGI BRIDGE, DAVAO CITY").snippet("MATINA WLMS API"));

        map.addMarker(new MarkerOptions().position(new LatLng(7.00282, 125.48485))
            .title("MC ARTHUR BRIDGE, DAVAO CITY").snippet("MC ARTHUR WLMS API"));

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(getActivity(), "Info window clicked",
            Toast.LENGTH_SHORT).show();
    }
}

