package com.ravensltd.ravens.loginRegister;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ravensltd.ravens.R;
import com.ravensltd.ravens.home.HomeActivity;
import com.ravensltd.ravens.loginRegister.Register.VerifyPhoneNumberActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText userName;
    private EditText password;
    private TextView register;
    private TextView forgotpswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName=(EditText) findViewById(R.id.login_et_username);
        password=(EditText) findViewById(R.id.login_et_password);
        register=(TextView) findViewById(R.id.login_tv_register);
        forgotpswd=(TextView)findViewById(R.id.login_tv_forgotpassword);

        Button btlogin=(Button)findViewById(R.id.login_bt_login);

        btlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(view);
            }
        });

        try{

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent=new Intent(LoginActivity.this,VerifyPhoneNumberActivity.class);
                startActivity(registerIntent);
            }
        });
        }catch (Exception e){
            Log.e("my app", "Exception", e);
        }

        forgotpswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"handle this",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void attemptLogin(View view){

        //set errors to null
        userName.setError(null);
        password.setError(null);

        View focusView =null;
        Boolean cancel=false;

        String usName=userName.getText().toString();
        String pass=password.getText().toString();
        //userName missing
        if(usName.isEmpty()){
            userName.setError(getString(R.string.error_field_required));
            focusView=userName;
            cancel=true;
        }
        // password is too short
        else if (pass.length()<6){
            password.setError(getString(R.string.error_invalid_password));
            focusView=password;
            cancel=true;
            forgotpswd.setVisibility(View.VISIBLE);
        }
        // password incorrect
        else if(!pass.equals(usName)){
            password.setError(getString(R.string.error_incorrect_password));
            focusView=password;
            cancel=true;
            forgotpswd.setVisibility(View.VISIBLE);
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Intent homeIntent=new Intent(LoginActivity.this,HomeActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }
}
//not used
// had call from HomeActivity.ProfileInfo  sendtostart