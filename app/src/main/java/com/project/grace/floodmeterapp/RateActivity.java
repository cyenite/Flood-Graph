package com.project.grace.floodmeterapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RateActivity extends AppCompatActivity {


    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String thisDate;
    private SpinnerAdapter adapter;
    private String weatherInfo;
    private FusedLocationProviderClient client;
    private TextView txtWeather, txtFloodLevel;
    private RadioButton rbtnStatus;
    private RadioGroup rStatusGroup;
    private Button submitButton;
    double locale[];
    private ImageButton imageView;
    private ImageButton imageView2;
    private Bitmap imageBitmap;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private StorageReference mStorageRef;

    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        locale = bundle.getDoubleArray("locale");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        Date todayDate = new Date();
        thisDate = df.format(todayDate);

        imageView = findViewById(R.id.image);
        imageView2 = findViewById(R.id.imageView3);

        imageView.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.project.grace.floodmeterapp.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra("data", photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        });

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("crowdsource").child(thisDate);

        ArrayList<ItemData> list = new ArrayList<>();
        list.add(new ItemData("Cloudy", R.mipmap.ic_cloudy));
        list.add(new ItemData("Light Rainfall", R.mipmap.ic_rain_low));
        list.add(new ItemData("Medium Rainfall", R.mipmap.ic_rain_medium));
        list.add(new ItemData("Heavy Rainfall", R.mipmap.ic_rain_high));
        list.add(new ItemData("Thunderstorm", R.mipmap.ic_rain_thunder));
        Spinner sp = findViewById(R.id.spinner);

        txtWeather = findViewById(R.id.txtWeather);
        adapter = new SpinnerAdapter(this, R.layout.spinner_weather, R.id.txt, list);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                weatherInfo = adapter.getWeatherInfo(i);
                txtWeather.setText("Weather: " + weatherInfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button button1 = (Button) findViewById(R.id.submit_button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageBitmap == null){
                    Snackbar.make(v, "Take A Photo.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                List<LatLng> points = new ArrayList<>();
                points.add(new LatLng(7.085500, 125.614343));
                points.add(new LatLng(7.085500, 125.614343));
                points.add(new LatLng(7.086071, 125.617847));
                points.add(new LatLng(7.087436, 125.616721));

                double positionX = locale[0];
                double positionY = locale[1];
//
//                double positionX = 7.0658;
//                double positionY = 125.5967;

                double[] vertX = new double[]{
                        7.084118f, 7.086073f, 7.087450f, 7.085497f
                };
                double[] vertY = new double[]{
                        125.615469f, 125.617858f, 125.616718f, 125.614337f
                };

                if (isInsideLocation(4, vertX, vertY, positionX, positionY)){
                    System.out.println("inside");
                }

                CrowdSource cs = new CrowdSource();
                cs.setCrowdsource(0);
                cs.setLat((double) positionX);
                cs.setLon((double) positionY);
                cs.setTag(weatherInfo);
                cs.setDateAdded(thisDate);
                cs.setUserID(user.getUid());
                myRef.child((user.getUid())).setValue(cs);

                mStorageRef = FirebaseStorage.getInstance().getReference("crowdsource").child(thisDate);

                Bitmap bitmap = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = mStorageRef.child(user.getUid()).putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        System.out.println("failed");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        System.out.println("SUccess");
                    }
                });

                Intent intent = new Intent(RateActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putDoubleArray("locale", new double[]{positionX, positionY}); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();

//                Bundle bundle = new Bundle();
//                bundle.putDoubleArray("locale", new double[]{positionX, positionY});
//                Fragment fragment = MapsFragment.getInstance();
//                fragment.setArguments(bundle);
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                ft.replace(R.id.content_frame, fragment);
//                ft.commit();
            }
        });


        // *** Initialize radio button components
        // *** Changes made by Matt Lagat
        rStatusGroup = findViewById(R.id.radioGroup);
        txtFloodLevel = findViewById(R.id.txtFloodLevel);
        // *** End of code lines

        rStatusGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the RadioButton selected
            rbtnStatus = findViewById(checkedId);
            txtFloodLevel.setText("Flood Level: " + rbtnStatus.getText());
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //    Algorithm for detecting if it is outside or inside...
    public boolean isInsideLocation(int nvert, double[] vertx, double[] verty, double testx, double testy) {
        int i, j;
        boolean c = false;
        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
            if (((verty[i] > testy) != (verty[j] > testy)) && (testx < (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i]))
                c = !c;
        }
        return c;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView2.setImageBitmap(imageBitmap);
        }
    }

}
