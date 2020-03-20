package com.example.quickchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private View mView;

    private FirebaseAuth mAuth;
    private DatabaseReference mFriendsReference;

    SimpleDateFormat sfd;

    private String mUid;
    private FirebaseRecyclerAdapter<Friends, ChatsFragment.FriendsViewHolder> friendsRecyclerAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_chats, container, false);

        mFriendsList = mView.findViewById(R.id.chatRecyclerView);

        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(mUid);

        mFriendsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mFriendsList.setLayoutManager(linearLayoutManager);

        mFriendsReference.keepSynced(true);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        sfd = new SimpleDateFormat("dd-MM-yyyy");


        FirebaseRecyclerOptions<Friends> friendsOptions =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendsReference.orderByChild("lastchat"), Friends.class)
                        .build();

        friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friendsOptions) {

            @NonNull
            @Override
            public ChatsFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new ChatsFragment.FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatsFragment.FriendsViewHolder holder, int position, @NonNull Friends model) {
                final String user_key = getRef(position).getKey();

                holder.setValues(user_key);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String user_id = user_key;
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra("uid", user_id);
                        startActivity(chatIntent);
                    }
                });

            }
        };

        friendsRecyclerAdapter.startListening();

        mFriendsList.setAdapter(friendsRecyclerAdapter);

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

        public void setValues(final String user_key) {

            FirebaseDatabase.getInstance().getReference().child("Users").child(user_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                    final CircleImageView userSinglePic = itemView.findViewById(R.id.singleUserPic);
                    TextView userSingleName = itemView.findViewById(R.id.singleUserName);
                    final TextView userSingleStatus = itemView.findViewById(R.id.singleUserStatus);
                    final TextView userDateTextView = itemView.findViewById(R.id.dateTextView);
                    userDateTextView.setVisibility(View.VISIBLE);
                    userSingleStatus.setTextSize(14.0f);
                    ImageView onlineButton = itemView.findViewById(R.id.onlineButton);

                    if (dataSnapshot.hasChild("online") && (Boolean)dataSnapshot.child("online").getValue()) {
                        onlineButton.setVisibility(View.VISIBLE);
                    }

                    userSingleStatus.setText("");
                    userSingleStatus.setHint("Start Your First Chat");

                    Picasso.get().load(thumb_image).placeholder(R.drawable.default_pic).into(userSinglePic);
                    userSingleName.setText(name);

                    DatabaseReference lastMessageRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(mUid).child(user_key);

                    Query query = lastMessageRef.orderByKey().limitToLast(1);

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot_) {
                            if (dataSnapshot_.getValue() != null) {
                                for (DataSnapshot dataSnapshot1 : dataSnapshot_.getChildren()) {
                                    userSingleStatus.setText(dataSnapshot1.child("message").getValue().toString());

                                    SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a dd/MM/yyyy");
                                    String mDate = sfd.format(new Date(Long.parseLong(dataSnapshot1.child("time").getValue().toString())));

                                    userDateTextView.setText(mDate);

                                }
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


        }
    }
}
