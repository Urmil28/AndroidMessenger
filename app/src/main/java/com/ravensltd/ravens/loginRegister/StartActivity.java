package com.ravensltd.ravens.loginRegister;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.ravensltd.ravens.R;
import com.ravensltd.ravens.loginRegister.Register.VerifyPhoneNumberActivity;

public class StartActivity extends AppCompatActivity {

    private Button btAgree;
    private Toolbar startToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btAgree=(Button)findViewById(R.id.start_bt_agree);
        startToolbar=(Toolbar)findViewById(R.id.start_toolbar);
        setSupportActionBar(startToolbar);
        getSupportActionBar().setTitle("Welcome To Ravens");


        btAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent verifyIntent=new Intent(StartActivity.this, VerifyPhoneNumberActivity.class);
                startActivity(verifyIntent);
                finish();
            }
        });

    }
}

/* call from HomeActivity.ProfileInfo when from send to start method
or VerifyPhoneNumberActivity if pressed back
 */
