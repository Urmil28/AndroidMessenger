/*
Adapter class for stories
oncreateviewholder inflates the layout of vie to be shown as item of recycler view
onbindviewHolder binds data to viewholder object
getItemcount returns total no of item in recycler view

class holder extended from recyclerview.viewholder creates prototype of view
 */

package com.ravensltd.ravens.home.contactFragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.ravensltd.ravens.R;

//package com.ravensltd.ravens.home.contactFragment;
public class AdapterContactsItem extends RecyclerView.Adapter<AdapterContactsItem.ContactViewHolder> {

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mStorageRef;


    private LayoutInflater inflater;
    List<ContactListInfo> data= Collections.emptyList();

    public AdapterContactsItem(Context context, List<ContactListInfo> data) {
        inflater = LayoutInflater.from(context);
        this.data=data;
    }
    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.fragment_contactlist,parent,false);
        ContactViewHolder holder=new ContactViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, final int position) {
        // data is binded to holder
        ContactListInfo current=data.get(position);

        String phone=current.getPhoneno();

        if(phone.length()==10){
            phone="+91"+phone;
        }

        mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("user_id").child(phone);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String uId = dataSnapshot.child("uid").getValue().toString();

                    mStorageRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
                    mStorageRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            holder.status.setText(dataSnapshot.child("status").getValue().toString());

                            if(dataSnapshot.child("thumb_image").getValue().toString().equals("default"))
                            {
                             holder.icon.setImageResource(R.mipmap.defaultcontact);
                            }
                            else {
                                new DownloadImageTask((ImageView) holder.icon)
                                        .execute(dataSnapshot.child("thumb_image").getValue().toString());
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    // Toast.makeText(getActivity(),uId,Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    Log.e("my application",e.getMessage());
                    //Toast.makeText(getActivity(),"Sorry, but since this person is not using Ravens you can not start conversation",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.name.setText(current.getContactName());

        //String profilePhotoURL=current.getProfilePhoto();


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView icon;
        TextView status;

        public ContactViewHolder(View itemView) {
            super(itemView);

            name=(TextView) itemView.findViewById(R.id.contactname);
            status=(TextView)itemView.findViewById(R.id.contact_status);
            icon=(ImageView)itemView.findViewById(R.id.contactphoto);
        }
    }
}


class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}