package com.ravensltd.ravens.profileSetting;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.ravensltd.ravens.R;

public class StatusSettingActivity extends AppCompatActivity {

    private TextInputLayout mstatus;
    private Button mbtok;

    //firebase
    private FirebaseUser muser;
    private DatabaseReference mdatabaseref;


    //prgress
    private ProgressDialog mprogressdialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String status_info=getIntent().getStringExtra("current_status");

        //firebase
        muser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=muser.getUid();
        mdatabaseref= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


        //activity
        mstatus=(TextInputLayout)findViewById(R.id.status_textlayout);
        mstatus.getEditText().setText(status_info);

        mbtok=(Button)findViewById(R.id.status_bt_ok);
        mbtok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //progress
                mprogressdialog=new ProgressDialog(StatusSettingActivity.this);
                mprogressdialog.setTitle("Saving current status");
                mprogressdialog.setMessage("Please wait");
                mprogressdialog.show();

                String status=mstatus.getEditText().getText().toString();
                mdatabaseref.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mprogressdialog.dismiss();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Error occurred",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
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
//call from clicking status in profile activity