package com.example.meteors.plantheal;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;

public class RewardActivity extends AppCompatActivity {

    private TextView pointsTextView;
    private Button uploadImageButton;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;
    private Uri filePath;
    private static final String TAG = "RewardActivity";
    private int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        pointsTextView = findViewById(R.id.points_text_view);
        uploadImageButton = findViewById(R.id.upload_image_button);

        // Retrieve the current user's points from Firestore when the activity is launched
        retrievePoints();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        String collectionName = "users";
        String documentName = user.getUid();

        firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.getResult().isEmpty()) {
                    // Collection exists, continue with normal logic
                } else {
                    // Collection does not exist, create it
                    firestore.collection("users").document(user.getUid()).set(new HashMap<>())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Users collection created successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error creating users collection", e);
                                }
                            });
                }
            }
        });


        firestore.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    Long points = snapshot.getLong("points");
                    if (points == null) {
                        Log.e("RewardActivity", "Points field is null in Firestore document");
                        points = 0L;
                    }
                    pointsTextView.setText(String.valueOf(points));
                } else {
                    Log.e("RewardActivity", "Failed to retrieve points from Firestore", task.getException());
                }
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        SharedPreferences pref = getSharedPreferences("ImageUploadPref", 0);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if user has uploaded an image today
                if (pref.getString("lastUpload", "").equals(today)) {
                    Toast.makeText(RewardActivity.this, "You can only upload one image per day", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check for permissions
                if (ContextCompat.checkSelfPermission(RewardActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(RewardActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(RewardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RewardActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {
                    // Open dialog for choosing camera or gallery
                    AlertDialog.Builder builder = new AlertDialog.Builder(RewardActivity.this);
                    builder.setTitle("Choose Image Source");
                    builder.setItems(new CharSequence[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                // Camera
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(takePictureIntent, 0);
                                }
                            } else {
                                // Gallery
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto, 1);
                            }
                        }
                    });
                    builder.show();

                    // Update last uploaded date
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("lastUpload", today);
                    editor.apply();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Show progress dialog
            progressDialog = new ProgressDialog(RewardActivity.this);
            progressDialog.setTitle("Uploading Image");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            Uri imageUri = null;
            if (requestCode == 0) {
                // Image taken from camera
                if (data != null && data.getExtras() != null) {
                    imageUri = (Uri) data.getExtras().get("data");
                }
            } else if (requestCode == 1) {
                // Image selected from gallery
                imageUri = data.getData();
            }
            if (imageUri != null) {
                // Upload image to Firebase Storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                final StorageReference imageRef = storageRef.child("images/" + user.getUid() + "/" + imageUri.getLastPathSegment());
                UploadTask uploadTask = imageRef.putFile(imageUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("RewardActivity", "Failed to upload image to Firebase Storage", exception);
                        Toast.makeText(RewardActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Add image URL to user's Firestore document
                                firestore.collection("users").document(user.getUid()).update("images", FieldValue.arrayUnion(uri.toString()));
                                // Add points to user's Firestore document
                                firestore.collection("users").document(user.getUid()).update("points", FieldValue.increment(10));
                                // Update points text view
                                pointsTextView.setText(String.valueOf(Integer.parseInt(pointsTextView.getText().toString()) + 10));
                                Toast.makeText(RewardActivity.this, "Image uploaded and 10 points added", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrievePoints();
    }

    private void handleImageUploadSuccess() {
        // Save the current user's points to Firestore
        savePoints();
    }

    private void savePoints() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .set(new Points(points))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Points successfully saved!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error saving points: ", e);
                        }
                    });
        }
    }

    private void retrievePoints() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Points points = documentSnapshot.toObject(Points.class);
                                if (points != null) {
                                    updatePointsTextView(points.getPoints());
                                }
                            } else {
                                Log.d(TAG, "No such document!");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error getting document: ", e);
                        }
                    });
        }
    }

    private void updatePointsTextView(int points) {
        TextView pointsTextView = findViewById(R.id.points_text_view);
        pointsTextView.setText(String.valueOf(points));
    }

}