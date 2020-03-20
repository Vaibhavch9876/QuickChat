package com.example.quickchat;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private DatabaseReference mChatRef;
    private DatabaseReference mChats;

    String current_uid;
    String mUid;
    String userName;
    String current_image;
    String m_image;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private EditText messageEditText;
    private ImageButton sendMessageButton;

    private FirebaseUser mUser;

    public FirebaseRecyclerAdapter<Chat, ChatHolder> firebaseRecyclerAdapter;

    public LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        mToolbar = findViewById(R.id.chatAppBar);

        current_uid = getIntent().getStringExtra("uid");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mChatRef = mRootRef.child("Chats");
        mChats = mChatRef.child(mUid).child(current_uid);



        mRootRef.child("Users").child(current_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("name").getValue().toString();
                current_image = dataSnapshot.child("thumb_image").getValue().toString();
                getSupportActionBar().setTitle(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Users").child(mUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                m_image = dataSnapshot.child("thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mSwipeRefreshLayout = findViewById(R.id.chatRefreshLayout);
        mRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.sendMessageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager keyboardService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboardService.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                mRecyclerView.smoothScrollToPosition(firebaseRecyclerAdapter.getItemCount());

                final String message = messageEditText.getText().toString().trim();

                /*if (!TextUtils.isEmpty(message)) {

                    HashMap<String, String> mChatMap = new HashMap<>();

                    mChatMap.put("message", message);
                    mChatMap.put("type", "sent");

                    final HashMap<String, String> currentUserChatMap = new HashMap<>();

                    currentUserChatMap.put("message", message);
                    currentUserChatMap.put("type", "received");

                    final DatabaseReference otherChat = mChatRef.child(mUid).child(current_uid).push();

                    otherChat.setValue(mChatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                final DatabaseReference myChat = mChatRef.child(current_uid).child(mUid).push();

                                myChat.setValue(currentUserChatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            myChat.child("time").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        otherChat.child("time").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i("Message Sent : ", message);
                                                                }
                                                                else {
                                                                    Toast.makeText(ChatActivity.this, "Some Error Occured !", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else {
                                                        Toast.makeText(ChatActivity.this, "Some Error Occurred !", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        } else {
                                            Toast.makeText(ChatActivity.this, "Some Error Occurred !", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(ChatActivity.this, "Some Error Occured !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                 */

                if (!TextUtils.isEmpty(message)) {

                    Map<String, Object> myChatMap = new HashMap<>();

                    myChatMap.put("message", message);
                    myChatMap.put("type", "sent");
                    myChatMap.put("time", ServerValue.TIMESTAMP);

                    final Map<String, Object> otherChatMap = new HashMap<>();

                    otherChatMap.put("message", message);
                    otherChatMap.put("type", "received");
                    otherChatMap.put("time", ServerValue.TIMESTAMP);

                    mChatRef.child(mUid).child(current_uid).push().updateChildren(myChatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mChatRef.child(current_uid).child(mUid).push().updateChildren(otherChatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.i("Sending Message ", "Succesful");

                                            Map lastChatMap = new HashMap();
                                            lastChatMap.put(mUid + "/" + current_uid + "/lastchat", ServerValue.TIMESTAMP);
                                            lastChatMap.put(current_uid + "/" + mUid + "/lastchat", ServerValue.TIMESTAMP);

                                            mRootRef.child("Friends").updateChildren(lastChatMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if (task.isSuccessful()) {
                                                        Log.i("Sending Message ", "Succesful");
                                                    }
                                                    else {
                                                        Toast.makeText(ChatActivity.this, "Some Error occurred !", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        } else {
                                            Toast.makeText(ChatActivity.this, "Some Error Occurred !", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ChatActivity.this, "Some Error Occured !", Toast.LENGTH_SHORT);
                            }
                        }
                    });

                }

                mRecyclerView.smoothScrollToPosition(firebaseRecyclerAdapter.getItemCount());

                messageEditText.setText("");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Chat> options = new FirebaseRecyclerOptions.Builder<Chat>()
                .setQuery(mChats, Chat.class)
                .build();


        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Chat, ChatHolder>(options) {

                    @Override
                    protected void onBindViewHolder(@NonNull ChatHolder viewHolder, int position, @NonNull Chat model) {

                        final String current_uid = getRef(position).getKey();

                        viewHolder.setValues(model, current_uid);

                        mRecyclerView.scrollToPosition(firebaseRecyclerAdapter.getItemCount());

                    }

                    @NonNull
                    @Override
                    public ChatActivity.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat, parent, false);

                        mRecyclerView.smoothScrollToPosition(firebaseRecyclerAdapter.getItemCount());

                        return new ChatActivity.ChatHolder(view);
                    }
                };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        firebaseRecyclerAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private class ChatHolder extends RecyclerView.ViewHolder {

        private CircleImageView senderImageView;
        private CircleImageView receiverImagView;
        private TextView messageTextView;
        private RelativeLayout messageLayout;
        private TextView dateTimeTextView;
        private CardView messageCard;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            senderImageView = itemView.findViewById(R.id.senderImageView);
            receiverImagView = itemView.findViewById(R.id.receiverImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            messageCard = itemView.findViewById(R.id.messageCard);

        }

        public void setValues(Chat model, String current_uid) {

            if (model.getType().equals("sent")) {
                receiverImagView.setVisibility(View.INVISIBLE);
                senderImageView.setVisibility(View.VISIBLE);
                messageLayout.setGravity(Gravity.END);
                messageCard.setBackgroundColor(Color.parseColor("#FFFFFF"));
                Glide.with(ChatActivity.this).load(m_image).diskCacheStrategy(DiskCacheStrategy.DATA).error(R.drawable.default_pic).into(senderImageView);
            } else {
                senderImageView.setVisibility(View.INVISIBLE);
                receiverImagView.setVisibility(View.VISIBLE);
                messageLayout.setGravity(Gravity.START);
                messageCard.setBackgroundColor(Color.parseColor("#dddddd"));
                Glide.with(ChatActivity.this).load(current_image).diskCacheStrategy(DiskCacheStrategy.DATA).error(R.drawable.default_pic).into(senderImageView);
            }

            SimpleDateFormat sfd = new SimpleDateFormat("     hh:mm a\n dd/MM/yyyy");
            String mDate = sfd.format(new Date(model.getTime()));

            messageTextView.setText(model.getMessage());
            dateTimeTextView.setText(mDate);

        }
    }

}
