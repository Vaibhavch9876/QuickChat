package com.example.quickchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class OtherUserProfileActivity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mStatusTextView;
    private TextView totalFriendsTextView;
    private TextView idTextView;
    private Button profileDeclineRequestBtton;
    private Button profileSendRequestBtton;


    private DatabaseReference mFriendsReference;
    private DatabaseReference mFriendRequestReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mReference;
    private DatabaseReference mNotificationReference;

    private FirebaseUser mUser;

    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mImageView = findViewById(R.id.profilePicImageView);
        mNameTextView = findViewById(R.id.profileNameTextView);
        mStatusTextView = findViewById(R.id.profileStatusTextView);
        totalFriendsTextView = findViewById(R.id.profileTotalFriendsTextView);
        profileSendRequestBtton = findViewById(R.id.profileFriendRequestButton);
        profileDeclineRequestBtton = findViewById(R.id.profileDeclineRequestButton);
        idTextView = findViewById(R.id.idTextView);

        mCurrentState = "not friends";

        final String current_uid = getIntent().getStringExtra("uid");

        idTextView.setText(current_uid);

        mReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        final String mUid = mUser.getUid();

        mUserReference = mReference.child("Users").child(current_uid);
        mFriendRequestReference = mReference.child("Requests");
        mFriendsReference = mReference.child("Friends");
        mNotificationReference = mReference.child("Notifications");


        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mNameTextView.setText(dataSnapshot.child("name").getValue().toString());
                mStatusTextView.setText(dataSnapshot.child("status").getValue().toString());
                Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.default_pic).into(mImageView);

                profileDeclineRequestBtton.setEnabled(false);
                profileDeclineRequestBtton.setVisibility(View.INVISIBLE);

                mFriendRequestReference.child(mUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(current_uid)) {

                            String requestType = dataSnapshot.child(current_uid).child("type").getValue().toString();

                            if (requestType.equals("sent")) {

                                mCurrentState = "request sent";
                                profileSendRequestBtton.setText("Cancel Friend Request");

                            }
                            else if (requestType.equals("received")) {

                                mCurrentState = "request received";
                                profileSendRequestBtton.setText("Accept Friend Request");

                                profileDeclineRequestBtton.setVisibility(View.VISIBLE);
                                profileDeclineRequestBtton.setEnabled(true);

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mFriendsReference.child(mUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(current_uid)) {

                            mCurrentState = "friends";
                            profileSendRequestBtton.setText("Unfriend");

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFriendsReference.child(current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalFriendsTextView.setText("Total Friends : " + String.valueOf((int) dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileSendRequestBtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                profileSendRequestBtton.setEnabled(false);

                // ---------- Send Friend Request ------------- //
                if (mCurrentState.equals("not friends")) {

                    mFriendRequestReference.child(mUid).child(current_uid).child("type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mFriendRequestReference.child(current_uid).child(mUid).child("type")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            mCurrentState = "request sent";
                                            profileSendRequestBtton.setText("Cancel Friend Request");

                                            Toast.makeText(OtherUserProfileActivity.this, "Request Sent Succesfully", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });

                            }
                            else {
                                Toast.makeText(OtherUserProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }

                            profileSendRequestBtton.setEnabled(true);
                        }
                    });

                }


                // -------------- Cancel Friend Request ------------- //
                if (mCurrentState.equals("request sent")) {

                    profileSendRequestBtton.setEnabled(false);

                    mFriendRequestReference.child(mUid).child(current_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mFriendRequestReference.child(current_uid).child(mUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            Toast.makeText(OtherUserProfileActivity.this, "Request Cancelled Succesfully", Toast.LENGTH_SHORT).show();

                                            mCurrentState = "not friends";
                                            profileSendRequestBtton.setText("Send Friend Request");

                                        }
                                        else {
                                            Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                            }
                            else {
                                Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                            }

                            profileSendRequestBtton.setEnabled(true);
                        }
                    });

                }


                // ------------- Friend Request Received ----------- //

                if (mCurrentState.equals("request received")) {

                    profileSendRequestBtton.setEnabled(false);
                    profileDeclineRequestBtton.setEnabled(false);

                    mFriendRequestReference.child(mUid).child(current_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mFriendRequestReference.child(current_uid).child(mUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            FirebaseDatabase.getInstance().getReference().child("Users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    final String myName = dataSnapshot.child("name").getValue().toString();

                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                                                            String otherName = dataSnapshot1.child("name").getValue().toString();

                                                            HashMap<String, Object> myRequestMap = new HashMap<>();

                                                            myRequestMap.put("date", ServerValue.TIMESTAMP);
                                                            myRequestMap.put("name", otherName);
                                                            myRequestMap.put("lastchat", new Long(0));

                                                            final HashMap<String, Object> otherRequestMap = new HashMap<>();

                                                            otherRequestMap.put("date", ServerValue.TIMESTAMP);
                                                            otherRequestMap.put("name", myName);
                                                            otherRequestMap.put("lastchat", new Long(0));

                                                            mFriendsReference.child(mUid).child(current_uid).setValue(myRequestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        mFriendsReference.child(current_uid).child(mUid).setValue(otherRequestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()) {

                                                                                    Toast.makeText(OtherUserProfileActivity.this, "Accepted Successfully", Toast.LENGTH_SHORT).show();


                                                                                    mCurrentState = "friends";
                                                                                    profileSendRequestBtton.setText("Unfriend");

                                                                                    profileDeclineRequestBtton.setVisibility(View.INVISIBLE);

                                                                                }
                                                                                else {
                                                                                    Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred !", Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });

                                                                    }
                                                                    else {
                                                                        Toast.makeText(OtherUserProfileActivity.this, "Some Error Occured !", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            });

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                            /*
                                            mFriendsReference.child(mUid).child(current_uid).child("date").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        mFriendsReference.child(current_uid).child(mUid).child("date").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    Toast.makeText(OtherUserProfileActivity.this, "Accepted Successfully", Toast.LENGTH_SHORT).show();


                                                                    mCurrentState = "friends";
                                                                    profileSendRequestBtton.setText("Unfriend");

                                                                    profileDeclineRequestBtton.setVisibility(View.INVISIBLE);

                                                                }
                                                                else {
                                                                    Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });
                                                    }
                                                    else {
                                                        Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });*/

                                        }
                                        else {
                                            Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                            }
                            else {
                                Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                            }

                            profileSendRequestBtton.setEnabled(true);
                        }
                    });

                }


                // ------------ Already Friends (UnFriend) ------------- //

                if (mCurrentState.equals("friends")) {

                    mFriendsReference.child(mUid).child(current_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mFriendsReference.child(current_uid).child(mUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            FirebaseDatabase.getInstance().getReference().child("Chats").child(mUid).child(current_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        FirebaseDatabase.getInstance().getReference().child("Chats").child(current_uid).child(mUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    Toast.makeText(OtherUserProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();

                                                                    mCurrentState = "not friends";
                                                                    profileSendRequestBtton.setText("Send Friend Request");

                                                                }
                                                                else {
                                                                    Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                    else {
                                                        Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });

                                        }
                                        else {
                                            Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                            }
                            else {
                                Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                            }

                            profileSendRequestBtton.setEnabled(true);
                        }
                    });

                }

            }
        });

        profileDeclineRequestBtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCurrentState.equals("request received")) {

                    profileSendRequestBtton.setEnabled(false);
                    profileDeclineRequestBtton.setEnabled(false);

                    mFriendRequestReference.child(mUid).child(current_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mFriendRequestReference.child(current_uid).child(mUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            Toast.makeText(OtherUserProfileActivity.this, "Declined Succesfully", Toast.LENGTH_SHORT).show();
                                            profileDeclineRequestBtton.setVisibility(View.INVISIBLE);

                                            profileSendRequestBtton.setText("Send Friend Request");
                                            mCurrentState = "not friends";

                                        }
                                        else {
                                            Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                            }
                            else {
                                Toast.makeText(OtherUserProfileActivity.this, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                            }

                            profileSendRequestBtton.setEnabled(true);
                        }
                    });
                }

            }
        });

    }

}
