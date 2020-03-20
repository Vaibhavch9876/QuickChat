package com.example.quickchat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.vistrav.pop.Pop;

import java.net.MalformedURLException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private View mView;

    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private DatabaseReference mFriendsReference;

    private String mUid;
    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = mView.findViewById(R.id.friendsRecyclerView);

        fab = mView.findViewById(R.id.addFriendButton);

        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(mUid);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mFriendsReference.keepSynced(true);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> friendsOptions =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendsReference.orderByChild("name"), Friends.class)
                        .build();

        friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friendsOptions) {

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new FriendsFragment.FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Friends model) {
                final String user_key = getRef(position).getKey();

                holder.setValues(user_key, model.getDate());

            }
        };

        friendsRecyclerAdapter.startListening();

        mFriendsList.setAdapter(friendsRecyclerAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            private TextInputLayout idTextView;

            @Override
            public void onClick(View v) {

                Pop.on(getActivity())
                        .with()
                        .title("ENTER ID ")
                        .cancelable(false)
                        .layout(R.layout.status_dialog)
                        .when(new Pop.Yah() {
                            @Override
                            public void clicked(DialogInterface dialog, View view) {

                                final String current_uid = idTextView.getEditText().getText().toString().trim();

                                FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {

                                            Intent otherProfileIntent = new Intent(getContext(), OtherUserProfileActivity.class);
                                            otherProfileIntent.putExtra("uid", current_uid);
                                            startActivity(otherProfileIntent);

                                        }
                                        else {
                                            Toast.makeText(getContext(), "Invalid Id ", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

                                idTextView = view.findViewById(R.id.statusChangeTextView);
                                idTextView.setHint("Enter Id. ");

                            }
                        });

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        friendsRecyclerAdapter.stopListening();
    }

    private class FriendsViewHolder extends RecyclerView.ViewHolder {
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setValues(final String user_key, final Long dateString) {

            FirebaseDatabase.getInstance().getReference().child("Users").child(user_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
                    String mDate = sfd.format(new Date(dateString));

                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                    final CircleImageView userSinglePic = itemView.findViewById(R.id.singleUserPic);
                    TextView userSingleName = itemView.findViewById(R.id.singleUserName);
                    TextView userSingleStatus = itemView.findViewById(R.id.singleUserStatus);
                    TextView userDateTextView = itemView.findViewById(R.id.dateTextView);
                    final LinearLayout userLinearLayout = itemView.findViewById(R.id.singleUserLinearLayout);
                    final Button unfriendButton = itemView.findViewById(R.id.unfriendButton);
                    unfriendButton.setVisibility(View.VISIBLE);

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemView.setClickable(false);
                            unfriendButton.setEnabled(false);

                            final String user_id = user_key;
                            Intent otherProfileIntent = new Intent(getContext(), OtherUserProfileActivity.class);
                            otherProfileIntent.putExtra("uid", user_id);
                            startActivity(otherProfileIntent);

                        }
                    });

                    unfriendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            itemView.setClickable(false);
                            unfriendButton.setEnabled(false);

                            final String current_uid = user_key;

                            Pop.on(getActivity())
                                    .with()
                                    .layout(R.layout.unfriend_dialog)
                                    .cancelable(false)
                                    .when(new Pop.Nah() {
                                        @Override
                                        public void clicked(DialogInterface dialog, @Nullable View view) {
                                            itemView.setClickable(true);
                                            unfriendButton.setEnabled(true);
                                        }
                                    })
                                    .when(new Pop.Yah() {
                                        @Override
                                        public void clicked(DialogInterface dialog, @Nullable View view) {

                                            FirebaseDatabase.getInstance().getReference().child("Friends").child(mUid).child(current_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        FirebaseDatabase.getInstance().getReference().child("Friends").child(current_uid).child(mUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
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

                                            itemView.setClickable(true);
                                            unfriendButton.setEnabled(true);

                                        }
                                    })
                                    .show();

                        }
                    });

                    userDateTextView.setText(mDate);
                    userDateTextView.setVisibility(View.VISIBLE);
                    Picasso.get().load(thumb_image).placeholder(R.drawable.default_pic).into(userSinglePic);
                    userSingleName.setText(name);
                    userSingleStatus.setText(status);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }
}
