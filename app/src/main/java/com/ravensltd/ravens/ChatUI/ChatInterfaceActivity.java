//call from clicking the username of contacts or chat fragment
//parent activity homeActivity //done
/*
opens chat interface
from send button and its click listener msg is send to firebase*/
package com.ravensltd.ravens.ChatUI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ravensltd.ravens.OtherUserProfile.OtherUserProfileActivity;
import com.ravensltd.ravens.R;
import com.ravensltd.ravens.RelativeTime;
import com.ravensltd.ravens.profileSetting.ProfileSettingActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
//package com.ravensltd.ravens.ChatUI;
public class ChatInterfaceActivity extends AppCompatActivity {

    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private String mchatUser;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileView;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private FloatingActionButton mBtSend;
    private FloatingActionButton mBtAttach;
    private EditText mEtMessage;

    private RecyclerView mMessageRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private final List<MessageItemInfo> mMessageList=new ArrayList<>();
    private LinearLayoutManager mlinearLayoutManager;
    private MessageAdapter mMessageAdapter;


    private static final int TOTAL_ITEMS_TO_LOAD=15;
    private int mCurrentPage=1;

    private int itemPosition=0;
    private String mLastkey="";
    private String mPrevKey="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_interface);

        String name=getIntent().getStringExtra("user_name");
        mchatUser=getIntent().getStringExtra("user_id");



        mChatToolbar = (Toolbar) findViewById(R.id.chat_interface_toolbar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar=getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);
        actionBar.setDisplayShowTitleEnabled(false);


        //custom bar

        mTitleView=(TextView)findViewById(R.id.custom_chatbar_display_name);
        mLastSeenView=(TextView)findViewById(R.id.cuatom_chatbar_last_seen);
        mProfileView=(CircleImageView)findViewById(R.id.custom_chatbar_image);
        mTitleView.setText(name);

        //chat interface activity
        mBtAttach=(FloatingActionButton)findViewById(R.id.chat_interface_attach);
        mBtSend=(FloatingActionButton)findViewById(R.id.chat_interface_send);
        mEtMessage=(EditText)findViewById(R.id.chat_interface_edittext);


        mRootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();


        //chat user data // loading of image is still remaining
        mRootRef.child("Users").child(mchatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").getValue().toString();
                final String profilePhoto=dataSnapshot.child("profile_photo").getValue().toString();

                //retrieve image here from picassa

                if(online.equals("true")){
                    mLastSeenView.setText("Online");
                }
                else{

                    RelativeTime relativeTime=new RelativeTime();

                    long lastTime=Long.parseLong(online);

                    String lastSeenTime=relativeTime.getTimeAgo(lastTime,getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);
                }

                if(!profilePhoto.equals("default")) {
                    Picasso.with(ChatInterfaceActivity.this).load(profilePhoto)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.defaultcontact).into(mProfileView, new Callback() {
                        @Override
                        public void onSuccess() {
                            //image successfully retrived from offline
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ChatInterfaceActivity.this).load(profilePhoto).placeholder(R.mipmap.defaultcontact).into(mProfileView);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //to open otheruser activity
        mChatToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent otherIntent=new Intent(ChatInterfaceActivity.this, OtherUserProfileActivity.class);
                otherIntent.putExtra("userName",mTitleView.getText());
                startActivity(otherIntent);
            }
        });


        //to add chat instatnce in database
        mRootRef.child("chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mchatUser)){

                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("chat/" + mCurrentUserId + "/" + mchatUser,chatAddMap);
                    chatUserMap.put("chat/" + mchatUser + "/" + mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                //
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //to handle sending msg
        mBtSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });



        //Recycler View
        mMessageAdapter=new MessageAdapter(mMessageList);
        mMessageRecyclerView=(RecyclerView)findViewById(R.id.chat_interface_recycler);
        mRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.chat_interface_swiperefresh);
        mlinearLayoutManager=new LinearLayoutManager(this);
        mMessageRecyclerView.setHasFixedSize(true);
        mMessageRecyclerView.setLayoutManager(mlinearLayoutManager);
        mMessageRecyclerView.setAdapter(mMessageAdapter);

        loadMessages();





        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPosition=0;

                loadMoreMessages();

            }
        });



    }

    private void loadMoreMessages() {

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mchatUser);

        Query messageQuery=messageRef.orderByKey().endAt(mLastkey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessageItemInfo mItemInfo=dataSnapshot.getValue(MessageItemInfo.class);
                String messageKey=dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    mMessageList.add(itemPosition++,mItemInfo);
                }else {
                    mPrevKey=mLastkey;
                }

                if(itemPosition==1){

                    mLastkey=messageKey;
                }

                mMessageAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);

                mlinearLayoutManager.scrollToPositionWithOffset(TOTAL_ITEMS_TO_LOAD,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void loadMessages() {

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mchatUser);

        Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                MessageItemInfo mItemInfo=dataSnapshot.getValue(MessageItemInfo.class);
                String messageKey=dataSnapshot.getKey();
                itemPosition++;

                if(itemPosition==1){
                    mLastkey=messageKey;
                    mPrevKey=messageKey;
                }

                mMessageList.add(mItemInfo);
                mMessageAdapter.notifyDataSetChanged();

                mMessageRecyclerView.scrollToPosition(mMessageList.size()-1);

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        String messsage=mEtMessage.getText().toString();

        if(!TextUtils.isEmpty(messsage)){
            String currentUserRef="messages/" +  mCurrentUserId + "/" + mchatUser;
            String chatUserRef="messages/" +  mchatUser + "/" + mCurrentUserId;

            DatabaseReference messageRef=mRootRef.child("messages")
                    .child(mCurrentUserId).child(mchatUser).push();

            String pushId=messageRef.getKey();

            Map messageMap=new HashMap();
            messageMap.put("message",messsage);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);


            Map messageUserMap=new HashMap();

            messageUserMap.put(currentUserRef + "/" +pushId, messageMap);
            messageUserMap.put(chatUserRef + "/" +pushId, messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                }
            });

            mEtMessage.setText("");

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null){
            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid().toString()).child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

    }


}
//call from clicking the username of contacts or chat fragment
//parent activity homeActivity //done
//create own toolbar
//set onclick listener for toolbar open new activity
//pass image,name whule calling and initialise parameters here
