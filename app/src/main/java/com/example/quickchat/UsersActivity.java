package com.example.quickchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;

    private DatabaseReference mDatabaseReference;

    private FirebaseUser mUser;

    public FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter;

    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.usersAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mSwipeRefreshLayout = findViewById(R.id.userSwipeLayout);
        mRecyclerView = findViewById(R.id.userRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(false);

    }

    @Override
    protected void onStart() {
        super.onStart();

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPrefetchDistance(5)
                .setPageSize(12)
                .build();


        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setLifecycleOwner(this)
                .setQuery(mDatabaseReference, Users.class)
                .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                        final String user_key = getRef(position).getKey();

                        holder.setName(model.getName());
                        holder.setStatus(model.getStatus());
                        holder.setImage(model.getThumb_image());

                        if (user_key.equals(mUser.getUid())) {
                            holder.mView.setVisibility(View.GONE);
                            holder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent otherProfileIntent = new Intent(UsersActivity.this, OtherUserProfileActivity.class);
                                otherProfileIntent.putExtra("uid", user_key);
                                startActivity(otherProfileIntent);

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.users_single_layout, parent, false);

                        return new UsersViewHolder(view);                    }
                };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();

        mDatabaseReference.keepSynced(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();

        firebaseRecyclerAdapter.stopListening();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView singleUserName;
        TextView singleUserStatus;
        CircleImageView singleUserPic;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            singleUserName = mView.findViewById(R.id.singleUserName);
            singleUserStatus = mView.findViewById(R.id.singleUserStatus);
            singleUserPic = mView.findViewById(R.id.singleUserPic);

        }

        public void setName(String name) {
            singleUserName.setText(name);
        }

        public void setStatus(String status) {
            singleUserStatus.setText(status);
        }

        public void setImage(String thumb_image) {
            if (!thumb_image.equals("default")) {
                Picasso.get().load(thumb_image).placeholder(R.drawable.default_pic).into(singleUserPic);
            }
        }
    }

}
