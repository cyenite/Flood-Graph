package com.project.grace.floodmeterapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import static com.project.grace.floodmeterapp.Constant.ERROR_KEY;
import static com.project.grace.floodmeterapp.Constant.ERROR_KEY_MESSAGE;
import static com.project.grace.floodmeterapp.Constant.NO_INTERNET;
import static com.project.grace.floodmeterapp.Constant.NO_LOCATION;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    FirebaseAuth firebaseAuth;
    TextView txtEmailHeader;
    TextView txtNameHeader;

    //    Changes
    private Location locale = null;
    private final String CHANNEL_ID = "";

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }

        init();
        startService(new Intent(this, ServiceProvider.class));

        if (!isGPSEnabled()) {
            turnGPSOn();
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.getUid();
        firebaseAuth = FirebaseAuth.getInstance();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        txtEmailHeader = headerView.findViewById(R.id.userEmail);
        txtNameHeader = headerView.findViewById(R.id.userName);
        txtEmailHeader.setText(firebaseAuth.getCurrentUser().getEmail());
        txtNameHeader.setText(firebaseAuth.getCurrentUser().getDisplayName());
    }



    private void showAlert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message);
        dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
            }
        });
        dialog.show();
    }

    public synchronized void showMap() {
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putDoubleArray("locale", new double[]{locale.getLatitude(), locale.getLongitude()});
        fragment = MapsFragment.getInstance();
        fragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
    }

    private void turnGPSOn() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isGPSEnabled() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    protected void onStop() {
        if (googleApiClient.isConnected())
            googleApiClient.disconnect();
        super.onStop();
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showNoLocationData();
    }

    private void showNoLocationData() {
        Fragment noLocation = new NoLocation();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, noLocation);
        ft.commit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("ERROR");
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    locale = location;
                    showMap();
                } else {
                    showNoLocationData();
                }
            }
        });
    }

    public void onConnectionSuspended(int i) {
        Log.e("MainActivity", "Connection suspended");
    }

    //try back twice pressed
    boolean doubleBackPressed = false;

    @Override
    public void onBackPressed() {
        if (doubleBackPressed) {
            super.onBackPressed();
        } else {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Bundle bundle;

        switch (id) {
            case R.id.nav_map:
                if (locale == null) {
                    showAlert(NO_LOCATION);
                    fragment = new NoLocation();
                    bundle = new Bundle();
                    bundle.putInt(ERROR_KEY, R.drawable.location_pin);
                    bundle.putString(ERROR_KEY_MESSAGE, NO_LOCATION);
                    fragment.setArguments(bundle);
                } else {
                    bundle = new Bundle();
                    bundle.putDoubleArray("locale", new double[]{locale.getLatitude(), locale.getLongitude()});
                    fragment = MapsFragment.getInstance();
                    fragment.setArguments(bundle);
                }
                break;
            case R.id.nav_graph:
                if (isConnectedToInternet()) {
                    fragment = Graph.getInstance();
                } else {
                    showAlert(NO_INTERNET);
                    fragment = new NoLocation();
                    bundle = new Bundle();
                    bundle.putInt(ERROR_KEY, R.drawable.wifi);
                    bundle.putString(ERROR_KEY_MESSAGE, NO_INTERNET);
                    fragment.setArguments(bundle);
                }
                break;
            case R.id.nav_rate:
                if (locale != null) {

                    Intent intent = new Intent(this, RateActivity.class);
                    bundle = new Bundle();
                    bundle.putDoubleArray("locale", new double[]{locale.getLatitude(), locale.getLongitude()});
                    intent.putExtras(bundle); //Put your id to your next Intent
                    startActivity(intent);
                    finish();
                } else {
                    showAlert(NO_LOCATION);
                    fragment = new NoLocation();
                    bundle = new Bundle();
                    bundle.putInt(ERROR_KEY, R.drawable.location_pin);
                    bundle.putString(ERROR_KEY_MESSAGE, NO_LOCATION);
                    fragment.setArguments(bundle);
                }
                break;
            case R.id.nav_statistics:
                if (isConnectedToInternet()) {
                    bundle = new Bundle();
                    fragment = StatisticsFragment.getInstance();
                    fragment.setArguments(bundle);
                } else {
                    showAlert(NO_INTERNET);
                    fragment = new NoLocation();
                    bundle = new Bundle();
                    bundle.putInt(ERROR_KEY, R.drawable.wifi);
                    bundle.putString(ERROR_KEY_MESSAGE, NO_INTERNET);
                    fragment.setArguments(bundle);
                }
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        } else {
            return false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}

