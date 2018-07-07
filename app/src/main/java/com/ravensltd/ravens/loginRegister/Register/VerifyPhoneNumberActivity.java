package com.ravensltd.ravens.loginRegister.Register;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ravensltd.ravens.R;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumberActivity extends AppCompatActivity {


    private static final String TAG = "VerifyPhoneNumberActivity";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private DatabaseReference mdatabaserefuser;
    private FirebaseUser mcurrentUser;



    private TextView message;
    private TextView sendSMS;
    private TextView alertmessage;
    private EditText phone;
    private EditText otp;
    Button btverify;
    private Toolbar registertoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        //Assigning Views
        message=(TextView)findViewById(R.id.createaccount_tv_message);
        sendSMS=(TextView)findViewById(R.id.createaccount_tv_sendmessage);;
        alertmessage=(TextView)findViewById(R.id.createaccount_tv_alertmessage);;
        phone=(EditText)findViewById(R.id.createaccount_et_phone);
        otp=(EditText)findViewById(R.id.createaccount_et_verificationcode);
        btverify=(Button)findViewById(R.id.createaccount_bt_verify);


        registertoolbar=(Toolbar)findViewById(R.id.verifyphone_toolbar);
        setSupportActionBar(registertoolbar);
        getSupportActionBar().setTitle("Verify your phone number");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); // this is to be removed later

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                //Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;


                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                //Log.w(TAG, "onVerificationFailed", e);

                mVerificationInProgress = false;

                Toast.makeText(getApplicationContext(),"Verification failed",Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                    phone.setError("Invalid phone number.");

                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "verification code has been send on your number :" + verificationId);
                Toast.makeText(getApplicationContext(),"verification code has been send on your number ",Toast.LENGTH_SHORT).show();
                otp.setVisibility(View.VISIBLE);
                btverify.setVisibility(View.VISIBLE);

                mVerificationId = verificationId;
                mResendToken = token;

                //reauthentication process will come here

            }
        };
        // [END phone_auth_callbacks]

        sendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone.setError(null);
                View focusView =null;
                if(!phone.getText().toString().isEmpty()) {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phone.getText().toString(),        // Phone number to verify
                            60,                                // Timeout duration
                            TimeUnit.SECONDS,                  // Unit of timeout
                            VerifyPhoneNumberActivity.this,    // Activity (for callback binding)
                            mCallbacks);                       // OnVerificationStateChangedCallbacks

                    mVerificationInProgress = true;
                }
                else {
                    phone.setError(getString(R.string.error_field_required));
                    phone.requestFocus();
                }
            }
        });

        btverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otp.setError(null);
                View focusView =null;
                if(!otp.getText().toString().isEmpty()) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp.getText().toString());

                    signInWithPhoneAuthCredential(credential);
                }
                else {
                    otp.setError(getString(R.string.error_field_required));
                    otp.requestFocus();
                }
            }
        });

    }




    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mcurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = mcurrentUser.getUid();

                            mdatabaserefuser= FirebaseDatabase.getInstance().getReference().child("user_id").child(phone.getText().toString()).child("uid");

                            mdatabaserefuser.setValue(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent profileIntent = new Intent(VerifyPhoneNumberActivity.this, ProfileSetupActivity.class);
                                    profileIntent.putExtra("phone",phone.getText().toString());
                                    startActivity(profileIntent);
                                    finish();
                                }
                            });

                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),"Verification failed",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
// [END sign_in_with_phone]



}

//call from Start.ProfileInfo
