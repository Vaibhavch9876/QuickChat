package com.example.quickchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import me.ydcool.lib.qrmodule.encoding.QrGenerator;

public class QrActivity extends AppCompatActivity {

    private TextView nameTextView;
    private TextView idTextView;
    private ImageView qrImageView;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mToolbar = findViewById(R.id.qr_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Your QR");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameTextView = findViewById(R.id.qrNameTextView);
        idTextView = findViewById(R.id.qrIdTextView);
        qrImageView = findViewById(R.id.qrImageView);

        idTextView.append(mUid);

        FirebaseDatabase.getInstance().getReference().child("Users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    nameTextView.setText(dataSnapshot.child("name").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Bitmap qrCode = null;
        try {
            qrCode = new QrGenerator.Builder()
                    .content(mUid)
                    .qrSize(200)
                    .margin(2)
                    .color(Color.BLACK)
                    .bgColor(Color.WHITE)
                    .ecc(ErrorCorrectionLevel.H)
                    .encode();
        } catch (WriterException e) {
            e.printStackTrace();
        }

        qrImageView.setImageBitmap(qrCode);

    }
}
