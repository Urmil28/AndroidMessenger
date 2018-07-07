package com.ravensltd.ravens;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ravensltd.ravens.home.HomeActivity;
import com.ravensltd.ravens.loginRegister.StartActivity;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;


public class OfflineCapabilities extends Application {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabaseRef;
    private DatabaseReference mDatabaseRef;
    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        mAuth=FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null) {

            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mDatabaseRef.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        //mDatabaseRef.child("lastseen").setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        /*
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUserDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                    mUserDatabaseRef.child("online").setValue("true");

                    // User is signed in
                    Log.d("my application", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out open Login Activity and don't come back therfore finish
                    Log.d("my application", "onAuthStateChanged:signed_out");

                    sendToStart();
                }
                // ...
            }
        };*/

    }
    /*
    public void sendToStart(){
        Intent startintent=new Intent(OfflineCapabilities.this, StartActivity.class);
        startActivity(startintent);
    }*/

}


/*      CharSequence options[]=new CharSequence[]{"open profile","send message"};

        final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Select Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {

           @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //open profile
                }
                else{
                    //sendmessage
                }
            }
        });
        builder.show();

*/