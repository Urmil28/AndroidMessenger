package com.ravensltd.ravens.profileSetting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ravensltd.ravens.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileSettingActivity extends AppCompatActivity {

    //android layout
    private CircleImageView mPhoto;
    private TextView mUsername;
    private TextView mStatus;
    private TextView mPhone;
    private FloatingActionButton mfab;
    private static final int GALLERY_PICK=1;

    //progress dialoge
    private ProgressDialog mprogress;


    //firebase
    private FirebaseUser muser;
    private DatabaseReference mdatabaseref;
    //storage
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Activity
        mPhoto=(CircleImageView) findViewById(R.id.profile_photo);
        mUsername=(TextView)findViewById(R.id.profile_username);
        mStatus=(TextView)findViewById(R.id.profile_status);
        mPhone=(TextView)findViewById(R.id.profile_phone);
        mfab= (FloatingActionButton) findViewById(R.id.profile_fab);


        //firebase
        muser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=muser.getUid();
        mdatabaseref= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mdatabaseref.keepSynced(true); // for offline compatibilty

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mdatabaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username=dataSnapshot.child("user_name").getValue().toString();
                final String profilePhoto=dataSnapshot.child("profile_photo").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String thumbImage=dataSnapshot.child("thumb_image").getValue().toString();

                mUsername.setText(username);
                mStatus.setText(status);
                //retriving image froom internet is hectic job so library is used
                if(!profilePhoto.equals("default")) {
                    Picasso.with(ProfileSettingActivity.this).load(profilePhoto)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.defaultcontact).into(mPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            //image successfully retrived from offline
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ProfileSettingActivity.this).load(profilePhoto).placeholder(R.mipmap.defaultcontact).into(mPhoto);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //Activity onclicklistener
        mStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_info=mStatus.getText().toString();
                Intent statusIntent=new Intent(ProfileSettingActivity.this,StatusSettingActivity.class);
                statusIntent.putExtra("current_status",status_info);
                startActivity(statusIntent);
                //status
            }
        });

        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select image"),GALLERY_PICK);
                // we can create new Activity to handle this change image action

                //for croping image we added library   compile 'com.theartofdev.edmodo:android-image-cropper:2.4.+'
                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ProfileSettingActivity.this);
                        */
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){

            Uri imageUri=data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(ProfileSettingActivity.this);
        }



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mprogress=new ProgressDialog(ProfileSettingActivity.this);
                mprogress.setTitle("Uploading image");
                mprogress.setMessage("Please wait...");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();


                Uri resultUri = result.getUri();

                File thumbFile=new File(resultUri.getPath());

                final String currentUserID=muser.getUid();


                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbByte = baos.toByteArray();

                StorageReference filePath=mStorageRef.child("profile_image").child(currentUserID+".jpg");
                final StorageReference thumbImageFilePath=mStorageRef.child("profile_image").child("thumbs").child(currentUserID+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            mprogress.dismiss();
                            final String downloadImageUri=task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumbImageFilePath.putBytes(thumbByte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumbDownloadURL=thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()) {

                                        Map update_hashmap=new HashMap();
                                        update_hashmap.put("profile_photo",downloadImageUri);
                                        update_hashmap.put("thumb_image",thumbDownloadURL);
                                        mdatabaseref.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    mprogress.dismiss();
                                                    Toast.makeText(ProfileSettingActivity.this, "Successfully changed image", Toast.LENGTH_SHORT).show();
                                                }
                                            }


                                        });
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(ProfileSettingActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();
                            mprogress.dismiss();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(muser!=null){
            mdatabaseref.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(muser!=null) {
           // mdatabaseref.child("online").setValue(ServerValue.TIMESTAMP);
           // mdatabaseref.child("lastseen").setValue(ServerValue.TIMESTAMP);
        }
    }

}

//call from Account Setting item of navigatroin drawer



//changes made for offline compatibility
//mdatabaseref.keepSynced(true);
//original Picasso.with(ProfileSettingActivity.this).load(profilePhoto).placeholder(R.mipmap.defaultcontact).into(mPhoto);
// now Picasso.with(ProfileSettingActivity.this).load(profilePhoto).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.defaultcontact).into(mPhoto);
