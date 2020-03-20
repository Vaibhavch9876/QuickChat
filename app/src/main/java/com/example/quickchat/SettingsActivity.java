package com.example.quickchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.vistrav.pop.Pop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GET_FROM_GALLERY = 12;
    private DatabaseReference mReference;
    private FirebaseUser mUser;

    private CircleImageView profileImage;
    private TextView displayName;
    private TextView myIdTextView;
    private TextView statusTextView;

    private Button changeImageButton;
    private Button changeStatusButton;

    public TextInputLayout newStatus;

    private StorageReference mStorageReference;

    public ProgressDialog mProgressDialog;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.profileAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profile_image);
        displayName = findViewById(R.id.name_textview);
        statusTextView = findViewById(R.id.status_textview);
        changeImageButton = findViewById(R.id.change_image_button);
        changeStatusButton = findViewById(R.id.change_status_button);
        myIdTextView = findViewById(R.id.myIdTextView);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mUser.getUid();

        myIdTextView.setText("ID : " + uid);

        mProgressDialog = new ProgressDialog(SettingsActivity.this);

        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mReference.keepSynced(true);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                displayName.setText(name);
                statusTextView.setText(status);

                if (!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_pic).into(profileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_pic).into(profileImage);
                        }
                    });
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference();

    }

    public void changeStatus(View view) {

        Pop.on(this)
                .with()
                .title("CHANGE STATUS")
                .cancelable(false)
                .layout(R.layout.status_dialog)
                .when(new Pop.Yah() {
                    @Override
                    public void clicked(DialogInterface dialog, View view) {

                        String status = newStatus.getEditText().getText().toString().trim();

                        mReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Changed Successfully..", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(SettingsActivity.this, "Something Went Wrong..", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                })
                .when(new Pop.Nah() { // ignore if dont need negative button
                    @Override
                    public void clicked(DialogInterface dialog, View view) {
                    }
                })
                .show(new Pop.View() { // assign value to view element
                    @Override
                    public void prepare(View view) {

                        newStatus = view.findViewById(R.id.statusChangeTextView);
                        newStatus.getEditText().setText(statusTextView.getText());

                    }
                });

    }

    public void ChangeImage(View view) {

        getProfileImage();

    }

    public void getProfileImage() {

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GET_FROM_GALLERY);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {

            Uri selectedImage = data.getData();
            CropImage.activity(selectedImage).setAspectRatio(1, 1).start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri imageUri = CropImage.getActivityResult(data).getUri();

                String uid = mUser.getUid();
                final StorageReference mProfileStorage = mStorageReference.child("profiles").child(uid);
                final StorageReference mThumbStorage = mStorageReference.child("thumbnails").child(uid);

                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setTitle("Updating Picture");
                mProgressDialog.setMessage("Please Wait While We Update Your Picture...");
                mProgressDialog.show();

                File thumb_filepath = new File(imageUri.getPath());

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(100)
                            .setMaxWidth(100)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbArrayBytes = baos.toByteArray();

                mProfileStorage.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProfileStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                mReference.child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mThumbStorage.putBytes(thumbArrayBytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        mThumbStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                mReference.child("thumb_image").setValue(uri.toString())
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                mProgressDialog.dismiss();
                                                                                Toast.makeText(SettingsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                ).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        mProgressDialog.dismiss();
                                                                        Toast.makeText(SettingsActivity.this, "Some Error", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(SettingsActivity.this, "Error Uploading Thumbnail", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            Toast.makeText(SettingsActivity.this, "Some Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        });


                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Exception error = result.getError();

            }

        }

    }

}
