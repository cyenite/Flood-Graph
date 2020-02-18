package com.project.grace.floodmeterapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final View mWindow;
    private final Context mContext;

    private StorageReference mStorageRef;
    private String thisDate;
    private Bitmap my_image;

    public CustomInfoWindow(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.info_window, null);
    }

    private void renderWindowText(Marker marker, View view) {

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        Date todayDate = new Date();
        String thisDate = df.format(todayDate);

        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.marker_title);

        if (!title.equals(""))
            tvTitle.setText(title);

        String snippet = marker.getSnippet();
        TextView tvSnippest = view.findViewById(R.id.maker_snippet);

        if (!snippet.equals(""))
            tvSnippest.setText(snippet);

        ImageView imageView = view.findViewById(R.id.pic);


        mStorageRef = FirebaseStorage.getInstance().getReference("crowdsource").child(thisDate);
        StorageReference ref = mStorageRef.child(snippet);
        try {
            final File localFile = File.createTempFile("Images", "bmp");
            ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                my_image = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                imageView.setImageBitmap(my_image);
            }).addOnFailureListener(e -> {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                imageView.setImageResource(R.drawable.camera);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
