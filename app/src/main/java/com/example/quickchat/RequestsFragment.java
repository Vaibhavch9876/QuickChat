package com.example.quickchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private RecyclerView mRequestList;
    private View mView;

    private FirebaseAuth mAuth;
    private DatabaseReference mRequestReference;
    private DatabaseReference mFriendRequestReference;
    private DatabaseReference mFriendsReference;

    private String mUid;
    public String myName;
    private FirebaseRecyclerAdapter<Request, RequestsFragment.RequestViewHolder> requestRecyclerAdapter;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_requests, container, false);


        mRequestList = mView.findViewById(R.id.requestRecyclerView);

        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("Users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        mRequestReference = mFriendRequestReference.child(mUid);

        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mRequestReference.keepSynced(true);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Request> requestOptions =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(mRequestReference, Request.class)
                        .build();

        requestRecyclerAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(requestOptions) {

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_request_layout, parent, false);

                return new RequestsFragment.RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Request model) {

                final String user_key = getRef(position).getKey();

                holder.setValues(user_key, mUid);

                if (model.getType().equals("sent")) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }

            }
        };

        requestRecyclerAdapter.startListening();

        mRequestList.setAdapter(requestRecyclerAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        requestRecyclerAdapter.stopListening();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        public Button acceptButton;
        public Button declineButton;
        public CircleImageView profilePic;
        public TextView requestUserName;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            acceptButton = itemView.findViewById(R.id.requestAcceptButton);
            declineButton = itemView.findViewById(R.id.requestDeclineButton);
            profilePic = itemView.findViewById(R.id.requestProfilePic);
            requestUserName = itemView.findViewById(R.id.requestUserName);

        }

        public void setValues(final String current_user, final String mUid) {

            FirebaseDatabase.getInstance().getReference().child("Users").child(current_user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final String otherName = dataSnapshot.child("name").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                    Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_pic).into(profilePic);
                    requestUserName.setText(otherName);

                    final String current_uid = current_user;
                    final String m_current_user = mUid;


                    acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            acceptButton.setEnabled(false);
                            declineButton.setEnabled(false);

                            mFriendRequestReference.child(m_current_user).child(current_user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        mFriendRequestReference.child(current_user).child(m_current_user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    HashMap<String, Object> myRequestMap = new HashMap<>();

                                                    myRequestMap.put("date", ServerValue.TIMESTAMP);
                                                    myRequestMap.put("name", otherName);
                                                    myRequestMap.put("lastchat", new Long(0));

                                                    final HashMap<String, Object> otherRequestMap = new HashMap<>();

                                                    otherRequestMap.put("date", ServerValue.TIMESTAMP);
                                                    otherRequestMap.put("name", myName);
                                                    otherRequestMap.put("lastchat", new Long(0));

                                                    mFriendsReference.child(m_current_user).child(current_uid);

                                                    mFriendsReference.child(m_current_user).child(current_uid).setValue(myRequestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {
                                                                mFriendsReference.child(current_uid).child(m_current_user).setValue(otherRequestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {

                                                                            Toast.makeText(getContext(), "Accepted Successfully", Toast.LENGTH_SHORT).show();

                                                                        }
                                                                        else {
                                                                            Toast.makeText(getContext(), "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                                        }

                                                                    }
                                                                });
                                                            }
                                                            else {
                                                                Toast.makeText(getContext(), "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });

                                                }
                                                else {
                                                    Toast.makeText(getContext(), "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                    }
                                    else {
                                        Toast.makeText(getContext(), "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });

                    declineButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            acceptButton.setEnabled(false);
                            declineButton.setEnabled(false);

                            mFriendRequestReference.child(m_current_user).child(current_user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        mFriendRequestReference.child(current_user).child(m_current_user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Request Cancelled Succesfully", Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    Toast.makeText(getContext(), "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                    }
                                    else {
                                        Toast.makeText(getContext(), "Some Error Occurred!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });

                    profilePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent profileIntent = new Intent(getContext(), OtherUserProfileActivity.class);
                            profileIntent.putExtra("uid", current_user);
                            startActivity(profileIntent);

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }
}