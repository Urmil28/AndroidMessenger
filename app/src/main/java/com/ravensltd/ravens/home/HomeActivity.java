/* control is passed to this activity from splash screen
 checks if user is logged-in in onstart() method
 if not sends control to startActivity.
 selects which fragments to be shown in getItem method of sectionpagerAdapter class
   getcount getcharactersequence
 */


package com.ravensltd.ravens.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ravensltd.ravens.home.chatFragment.TabChatFragment;
import com.ravensltd.ravens.home.contactFragment.TabContactsFragment;
import com.ravensltd.ravens.home.storiesFragment.TabStoriesFragment;
import com.ravensltd.ravens.profileSetting.ProfileSettingActivity;
import com.ravensltd.ravens.R;
import com.ravensltd.ravens.loginRegister.StartActivity;
import com.ravensltd.ravens.ravenevar.ChatRavenevarActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabaseRef;


    //navigation header
    private CircleImageView mNavigationProfile;
    private TextView mNavigationUserName;
    private TextView mNavigationStatus;

    //private DatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //mDb=new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setting navigation drawer icon
        mDrawerLayout=(DrawerLayout)findViewById(R.id.home_drawer_layout);
        mActionBarDrawerToggle=new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);



        //navigation
        NavigationView navigationView = (NavigationView) findViewById(R.id.home_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        //View headerView = navigationView.inflateHeaderView(R.layout.home_navigation_header);

        mNavigationProfile=(CircleImageView)headerView.findViewById(R.id.home_navigation_header_profile_photo);
        mNavigationStatus=(TextView)headerView.findViewById(R.id.home_navigation_header_status);
        mNavigationUserName=(TextView)headerView.findViewById(R.id.home_navigation_header_username);


        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUserDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                    mUserDatabaseRef.child("online").setValue("true");

                    // User is signed in
                    //Log.d("my application", "onAuthStateChanged:signed_in:" + user.getUid());

                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");

                    databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String profilePhoto=dataSnapshot.child("profile_photo").getValue().toString();
                            final String name=dataSnapshot.child("user_name").getValue().toString();
                            final String stat=dataSnapshot.child("status").getValue().toString();
                            //retrieve image here from picassa

                            mNavigationUserName.setText(name);
                            mNavigationStatus.setText(stat);
                            if(!profilePhoto.equals("default")) {
                                Picasso.with(HomeActivity.this).load(profilePhoto)
                                        .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.defaultcontact).into(mNavigationProfile, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        //image successfully retrived from offline
                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(HomeActivity.this).load(profilePhoto).placeholder(R.mipmap.defaultcontact).into(mNavigationProfile);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } else {
                    // User is signed out open Login Activity and don't come back therfore finish
                    Log.d("my application", "onAuthStateChanged:signed_out");

                    sendToStart();
                }
                // ...
            }
        };

    }

    public void sendToStart(){
        Intent startintent=new Intent(HomeActivity.this, StartActivity.class);
        startActivity(startintent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUserDatabaseRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home_action_settings) {
            return true;
        }

        if (id == R.id.home_action_logout) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
            return true;
        }

        if(mActionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return true;
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    TabChatFragment tab2=new TabChatFragment();
                    return tab2;
                case 1:
                    TabContactsFragment tab3 = new TabContactsFragment();
                    return tab3;
                case 2:
                    TabStoriesFragment tab1=new TabStoriesFragment();
                    return tab1;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 2:
                    return "Stories";
                case 0:
                    return "CHATS";
                case 1:
                    return "CONTACTS";
            }
            return null;
        }
    }

    //handing navigation drawer
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home_navigation_new_group) {
            Toast.makeText(getApplicationContext(),"new grp",Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.home_navigation_ravenevar) {
            Intent ravenevarIntent=new Intent(HomeActivity.this, ChatRavenevarActivity.class);
            startActivity(ravenevarIntent);
            //Toast.makeText(getApplicationContext(),"ravenevar",Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.home_navigation_account_settings) {
            Intent profileIntent=new Intent(HomeActivity.this,ProfileSettingActivity.class);
            startActivity(profileIntent);

        }

        else if (id == R.id.home_navigation_notdecided) {
            Toast.makeText(getApplicationContext(),"not decided",Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.home_navigation_logout) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}

// call from Splash.ProfileInfo
// call from profilesetup