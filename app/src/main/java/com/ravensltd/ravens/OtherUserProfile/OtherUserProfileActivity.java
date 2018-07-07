package com.ravensltd.ravens.OtherUserProfile;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ravensltd.ravens.R;

public class OtherUserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        String name=getIntent().getStringExtra("userName");

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsingtoolbar);
        appBarLayout.setTitle(name);
    }
}
//call after clicking the toolbar form chat interface