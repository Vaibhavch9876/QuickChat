package com.example.quickchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import me.ydcool.lib.qrmodule.activity.QrScannerActivity;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ALL_PERMISSIONS = 101;
    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;

    private ViewPager mViewpager;
    private SectionsPagerAdapter mSectionPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        mtoolbar = findViewById(R.id.main_page_toolbar);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Quick Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mViewpager = findViewById(R.id.main_view_pager);
        mSectionPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewpager.setAdapter(mSectionPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewpager);

    }

    @Override
    public void onStart() {
        super.onStart();


        requestAllPermissions();


        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            sendToStart();

        }

    }

    private void requestAllPermissions() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_ALL_PERMISSIONS);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_ALL_PERMISSIONS);

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ALL_PERMISSIONS: {
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    finishAffinity();
                }
                return;
            }


        }
    }


    private void sendToStart() {

        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.log_out:
                String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                SignOut();
                FirebaseDatabase.getInstance().getReference().child("Users").child(mUid).child("online").setValue(false);
                sendToStart();
                return true;

            case R.id.account_settings:
                Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;

            case R.id.all_users:
                Intent allUserIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(allUserIntent);
                return true;

            case R.id.scan_button:
                Intent intent = new Intent(MainActivity.this, QrScannerActivity.class);
                startActivityForResult(intent, QrScannerActivity.QR_REQUEST_CODE);
                return true;

            case R.id.show_qr:
                Intent qrIntent = new Intent(MainActivity.this, QrActivity.class);
                startActivity(qrIntent);
                return true;

            default:
                return true;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QrScannerActivity.QR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                final String scanned_uid = data.getExtras().getString(QrScannerActivity.QR_RESULT_STR);

                try {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(scanned_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Log.e("Reached", scanned_uid);
                            if (dataSnapshot.getValue() != null) {


                                Intent otherUsersProfileIntent = new Intent(MainActivity.this, OtherUserProfileActivity.class);
                                otherUsersProfileIntent.putExtra("uid", scanned_uid);
                                startActivity(otherUsersProfileIntent);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } catch (DatabaseException e) {
                    Toast.makeText(this, "Invalid QR Code ", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private void SignOut() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("token").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    return;
                } else {
                    Toast.makeText(MainActivity.this, "SomeThing Went Wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mAuth.signOut();

    }
}
