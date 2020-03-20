package com.example.quickchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mRegButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegButton = findViewById(R.id.start_reg_button);

    }

    public void login(View view) {

        Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(loginIntent);

    }

    public void register(View view) {

        Intent regIntent = new Intent(StartActivity.this, RegistrationActivity.class);
        startActivity(regIntent);

    }

}
