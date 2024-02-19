package com.example.selectvideo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_VIDEO = 1;
    private Button selectVideoButton;
    private Button uploadVideoButton;
    private VideoView videoPreview;
    private Uri videoUri;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Video...");

        selectVideoButton = findViewById(R.id.selectVideoButton);
        uploadVideoButton = findViewById(R.id.uploadVideoButton);
        videoPreview = findViewById(R.id.videoPreview);

        // Add media controls to the VideoView
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPreview);
        videoPreview.setMediaController(mediaController);

        selectVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideoFromGallery();
            }
        });

        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideoToFirebase();
            }
        });
    }

    private void selectVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
    }

    private void uploadVideoToFirebase() {
        if (videoUri != null) {
            // Create a reference to "videos/[filename]"
            StorageReference videoRef = storageReference.child("videos/" + System.currentTimeMillis());

            // Show progress dialog
            progressDialog.show();

            // Upload file to Firebase Storage
            UploadTask uploadTask = videoRef.putFile(videoUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Video uploaded successfully
                // Handle successful upload (e.g., show a success message)
                Toast.makeText(MainActivity.this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();

                // Dismiss progress dialog
                progressDialog.dismiss();
            }).addOnFailureListener(e -> {
                // Handle unsuccessful upload (e.g., show an error message)
                Toast.makeText(MainActivity.this, "Video not uploaded", Toast.LENGTH_SHORT).show();

                // Dismiss progress dialog
                progressDialog.dismiss();
            }).addOnProgressListener(taskSnapshot -> {
                // Update progress dialog
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading " + ((int) progress) + "%");
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get selected video URI
            videoUri = data.getData();

            // Display the video preview
            videoPreview.setVideoURI(videoUri);
            videoPreview.start();
        }
    }
}
