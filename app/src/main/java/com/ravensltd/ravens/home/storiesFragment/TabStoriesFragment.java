/*
creting story by selecting image and then in onActivityresult
and then uploading that image as story

downloading stories in getstory method and putting them in data list
*/

package com.ravensltd.ravens.home.storiesFragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ravensltd.ravens.R;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

//package com.ravensltd.ravens.home.storiesFragment;
public class TabStoriesFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private StoryAdapter myAdapter;


    private String mCurrentUserId;
    private boolean liked=false;
    private static final int GALLERY_PICK = 1;

    private ProgressDialog mprogress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageRef;

    private static String mPushId;
    private static int mFlag=0;



    private String downloadImageUri;
    private String thumbDownloadURL;

    private final List<StoryItemInfo> mStoryItemInfoList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_tab_stories,container,false);

        mAuth=FirebaseAuth.getInstance();
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("stories");
        mStorageRef = FirebaseStorage.getInstance().getReference();


        mCurrentUserId=mAuth.getCurrentUser().getUid();
        getStories();
        mRecyclerView=(RecyclerView)view.findViewById(R.id.stories_recycleriew);
        myAdapter=new StoryAdapter(getActivity(),mStoryItemInfoList);
        mRecyclerView.setAdapter(myAdapter);
        LinearLayoutManager mLayoutManager=new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFab=(FloatingActionButton)view.findViewById(R.id.stories_fabaddstory);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFlag=0;
                mPushId=null;

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent.createChooser(galleryIntent, "Your Story"), GALLERY_PICK);

            }
        });


        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).start(getContext(),this);
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mprogress = new ProgressDialog(getActivity());
                mprogress.setTitle("Adding your story");
                mprogress.setMessage("Please wait...");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();


                final Uri resultUri = result.getUri();
                File thumbFile=new File(resultUri.getPath());


                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(getContext())
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


                DatabaseReference databaseReference=mDatabaseReference.push();

                mPushId=databaseReference.getKey();

                StorageReference filePath = mStorageRef.child("story_image").child(mPushId+".jpg");
                final StorageReference thumbImageFilePath=mStorageRef.child("story_image").child("thumbs").child(mPushId+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            downloadImageUri = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbImageFilePath.putBytes(thumbByte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    thumbDownloadURL=thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()) {
                                        mprogress.dismiss();
                                        mFlag=1;



                                        Map mp = new HashMap<>();
                                        mp.put("uid", mCurrentUserId);
                                        mp.put("likes", 0);
                                        mp.put("time", ServerValue.TIMESTAMP);
                                        mp.put("story_image",downloadImageUri);
                                        mp.put("story_image_thumb",thumbDownloadURL);


                                        mDatabaseReference.child(mPushId).updateChildren(mp, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                //StoryItemInfo storyItemInfo;

                                                //mStoryItemInfoList.add(storyItemInfo);
                                                Toast.makeText(getActivity(),"Your story addded successfully",Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                    else {
                                        Toast.makeText(getActivity(), "Error occurred", Toast.LENGTH_SHORT).show();
                                        mprogress.dismiss();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(getActivity(), "Error occurred", Toast.LENGTH_SHORT).show();
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


    public void getStories(){

        DatabaseReference databaseReference=mDatabaseReference;

        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                StoryItemInfo storyItemInfo=dataSnapshot.getValue(StoryItemInfo.class);

                String storyKey=dataSnapshot.getKey();

                storyItemInfo.setStorykey(storyKey);

                mStoryItemInfoList.add(storyItemInfo);
                //adapter
                myAdapter.notifyDataSetChanged();
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


}
