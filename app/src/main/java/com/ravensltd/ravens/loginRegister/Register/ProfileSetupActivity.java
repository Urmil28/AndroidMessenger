package com.ravensltd.ravens.loginRegister.Register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ravensltd.ravens.R;
import com.ravensltd.ravens.home.HomeActivity;
import com.ravensltd.ravens.sqliteDatabase.DatabaseHelper;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.http.entity.ByteArrayEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileSetupActivity extends AppCompatActivity {

    //Activity
    private Button mbtNext;
    private Toolbar msetupToolbar;
    private EditText mstatus;
    private EditText musername;
    private CircleImageView mprofilephoto;
    private FloatingActionButton mFab;
    private String mPhone;
    private static final int GALLERY_PICK = 1;

    //Process Dialog
    private ProgressDialog mprogress;

    //firebase
    private FirebaseUser currentUser;
    private DatabaseReference mdatabaseref;

    //storage
    private StorageReference mStorageRef;

    private String downloadImageUri;
    private String thumbDownloadURL;
    //flag
    private static int flag=0;

    //public DatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        mPhone=getIntent().getStringExtra("phone");

        //creating local database
        //mDb=new DatabaseHelper(this);


        // getting currenrt user and setting database refference
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        mdatabaseref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        msetupToolbar = (Toolbar) findViewById(R.id.setup_profile_toolbar);
        setSupportActionBar(msetupToolbar);
        getSupportActionBar().setTitle("Set Your Profile");

        mstatus = (EditText) findViewById(R.id.profile_setup_status);
        musername = (EditText) findViewById(R.id.profile_setup_username);
        mprofilephoto = (CircleImageView) findViewById(R.id.profile_setup_photo);
        mFab = (FloatingActionButton) findViewById(R.id.profile_setup_fab);

        mbtNext = (Button) findViewById(R.id.setup_profile_bt_next);

        mbtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = musername.getText().toString();
                String image = "default";
                String status = mstatus.getText().toString();

                musername.setError(null);
                View focusView =null;
                if(name.isEmpty()){
                    musername.setError(getString(R.string.error_field_required));
                    musername.requestFocus();
                }
                else {
                    if(status.isEmpty()){
                        status="How you doin ?..";
                    }

                    mprogress = new ProgressDialog(ProfileSetupActivity.this);
                    mprogress.setTitle("Setting your profile");
                    mprogress.setMessage("Please wait...");
                    mprogress.setCanceledOnTouchOutside(false);
                    mprogress.show();


                    //adding data to database
                    HashMap<String, String> mp = new HashMap<>();
                    mp.put("user_name", name);
                    mp.put("status", status);
                    if (flag == 0) {
                        mp.put("profile_photo", image); //put default only if image is not set
                        mp.put("thumb_image", "default");

                    }else {
                        mp.put("profile_photo", downloadImageUri);
                        mp.put("thumb_image", thumbDownloadURL);
                    }
                    mdatabaseref.setValue(mp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                /*if(flag==1) {
                                    mDb.insertProfileInfo(musername.getText().toString(),
                                            mstatus.getText().toString(),
                                            mPhone,
                                            currentUser.getUid());
                                           // imageViewToByte(mprofilephoto));
                                    Toast.makeText(getApplicationContext(),"successfully added to local database",Toast.LENGTH_SHORT).show();
                                }else{
                                    mDb.insertProfileInfo(musername.getText().toString(),
                                            mstatus.getText().toString(),
                                            mPhone,
                                            currentUser.getUid());
                                            //imageViewToByte(mprofilephoto));
                                }*/

                                Intent HomeIntent = new Intent(ProfileSetupActivity.this, HomeActivity.class);
                                startActivity(HomeIntent);
                                finish();
                            } else {
                                //handle error here
                            }
                        }
                    });
                }
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent.createChooser(galleryIntent, "Select profile photo"), GALLERY_PICK);
            }
        });

    }

    private byte[] imageViewToByte(CircleImageView imag){
        Bitmap bm=((BitmapDrawable)imag.getDrawable()).getBitmap();
        ByteArrayOutputStream st=new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,st);
        byte[] byteArray =st.toByteArray();
        return byteArray;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1, 1).start(ProfileSetupActivity.this);
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mprogress = new ProgressDialog(ProfileSetupActivity.this);
                mprogress.setTitle("Uploading image");
                mprogress.setMessage("Please wait...");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();


                final Uri resultUri = result.getUri();
                File thumbFile=new File(resultUri.getPath());

                final String currentUserID = currentUser.getUid();

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

                StorageReference filePath = mStorageRef.child("profile_image").child(currentUserID + ".jpg");
                final StorageReference thumbImageFilePath=mStorageRef.child("profile_image").child("thumbs").child(currentUserID+".jpg");

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
                                        mprofilephoto.setImageURI(resultUri);
                                        flag=1;
                                    }
                                    else {
                                        Toast.makeText(ProfileSetupActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                        mprogress.dismiss();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(ProfileSetupActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
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
}
//call from VerifyNumberActivity on success in signinwithcredential method
//there is no need to pass phone through bundle


// on this Activity if user pressed back button ask him
