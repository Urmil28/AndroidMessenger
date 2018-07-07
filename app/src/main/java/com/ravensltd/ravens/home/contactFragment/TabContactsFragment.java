package com.ravensltd.ravens.home.contactFragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ravensltd.ravens.ChatUI.ChatInterfaceActivity;
import com.ravensltd.ravens.R;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.phoneNumber;

//package com.ravensltd.ravens.home.contactFragment;
public class TabContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String [] mColumnProjection=new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private String morderby=ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;

    List<ContactListInfo> data=new ArrayList<>();

    private boolean fisrtTimeLoder;

    private AdapterContactsItem myAdapter;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mStorageRef;

    private String mStatus,mProfile;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        mAuth=FirebaseAuth.getInstance();

        View view=inflater.inflate(R.layout.fragment_tab_contacts,container,false);
        fisrtTimeLoder=false;
        getdata();
        recyclerView=(RecyclerView)view.findViewById(R.id.contacts_recyclerview);
        myAdapter=new AdapterContactsItem(getActivity(),data);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                try {

                    final ContactListInfo contact;
                    contact = myAdapter.data.get(position);

                    String phone=contact.getPhoneno();

                    if(phone.length()==10){
                        phone="+91"+phone;
                    }

                    mDatabaseRef=FirebaseDatabase.getInstance().getReference().child("user_id").child(phone);

                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                String uId = dataSnapshot.child("uid").getValue().toString();
                                Intent chatintent = new Intent(getActivity(), ChatInterfaceActivity.class);
                                chatintent.putExtra("user_name",contact.getContactName());
                                chatintent.putExtra("user_id",uId);
                                startActivity(chatintent);

                               // Toast.makeText(getActivity(),uId,Toast.LENGTH_SHORT).show();
                            }catch(Exception e){
                                Log.e("my application",e.getMessage());
                                Toast.makeText(getActivity(),"Sorry, but since this person is not using Ravens you can not start conversation",Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }catch (Exception e){
                    Log.e("mY application","Error",e);
                }
            }
            @Override
            public void onLongclick(View view, int position) {
                Toast.makeText(getActivity(),"delete contact"+position,Toast.LENGTH_SHORT).show();
            }
        }));

        return view;
    }

    public void getdata(){
        if(fisrtTimeLoder==false) {
            getLoaderManager().initLoader(1, null, this);
            fisrtTimeLoder=true;
        }
        else {
            getLoaderManager().restartLoader(1, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id==1) {
            return new CursorLoader(getContext(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, mColumnProjection, null, null, morderby);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                final ContactListInfo info=new ContactListInfo();
                info.setContactName(cursor.getString(0));
                info.setPhoneno(cursor.getString(1));

                String phone=info.getPhoneno();

                if(phone.length()==10){
                    phone="+91"+phone;
                }

                if (phone.length()==13) {

                    mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("user_id").child(phone);

                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                String uId = dataSnapshot.child("uid").getValue().toString();
                                    data.add(info);
                                    myAdapter.notifyDataSetChanged(); // this tells adapter that data has added or deleted

                            } catch (Exception e) {
                                Log.e("my application", e.getMessage());
                                //Toast.makeText(getActivity(),"Sorry, but since this person is not using Ravens you can not start conversation",Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                myAdapter.notifyDataSetChanged(); // this tells adapter that data has added or deleted
            }
        } else {
            //if mp is null handle in adapter
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
