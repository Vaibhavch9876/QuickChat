package com.example.quickchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.PrivateKey;
import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputLayout mDisplayname;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateButton;
    private Toolbar regToolbar;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    DatabaseReference mReference;
    FirebaseUser muser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mDisplayname = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreateButton = findViewById(R.id.reg_create_button);
        regToolbar = findViewById(R.id.reg_toolbar);

        setSupportActionBar(regToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(RegistrationActivity.this);

        mAuth = FirebaseAuth.getInstance();

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayname.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please Wait While We Create Your Account...");
                    mProgressDialog.show();

                    register_user(display_name, email, password);

                }

            }
        });

    }


    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    muser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = muser.getUid();

                    String token = FirebaseInstanceId.getInstance().getToken();

                    mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String, String> usermap = new HashMap<>();

                    usermap.put("token", token);
                    usermap.put("name", display_name);
                    usermap.put("status", "Hello everyone!!! This is me");
                    usermap.put("image", "default");
                    usermap.put("thumb_image", "default");

                    mReference.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mProgressDialog.dismiss();
                                Intent mainIntent = new Intent(RegistrationActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            } else {

                                mProgressDialog.hide();
                                Toast.makeText(RegistrationActivity.this, "You Got some Error!!", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                } else {

                    mProgressDialog.hide();
                    Toast.makeText(RegistrationActivity.this, "You Got some Error!!", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }
}
