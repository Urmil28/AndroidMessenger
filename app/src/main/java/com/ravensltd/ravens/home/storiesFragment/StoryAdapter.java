/*
Adapter class for stories
oncreateviewholder inflates the layout of vie to be shown as item of recycler view
onbindviewHolder binds data to viewholder object
getItemcount returns total no of item in recycler view

class holder extended from recyclerview.viewholder creates prototype of view
 */

package com.ravensltd.ravens.home.storiesFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ravensltd.ravens.ChatUI.ChatInterfaceActivity;
import com.ravensltd.ravens.R;
import com.ravensltd.ravens.RelativeTime;
import com.ravensltd.ravens.home.contactFragment.*;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private List<StoryItemInfo> mList= Collections.emptyList();
    private LayoutInflater inflater;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseReference;

    private Context context;


    public StoryAdapter(Context context, List<StoryItemInfo> mList) {
        inflater = LayoutInflater.from(context);
        this.mList = mList;
    }


    @Override
    public StoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_story_item,parent,false);
        context=parent.getContext();
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StoryViewHolder holder, int position) {

        StoryItemInfo storyItemInfo=mList.get(position);


        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("stories");

        RelativeTime relativeTime=new RelativeTime();

        long lastTime=Long.parseLong(String.valueOf(storyItemInfo.getTime()));

        String lastSeenTime=relativeTime.getTimeAgo(lastTime,context);

        holder.time.setText(lastSeenTime);

            mDatabaseReference.child(storyItemInfo.getStorykey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    new DownloadTask((ImageView) holder.mStoryView)
                            .execute(dataSnapshot.child("story_image").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        DatabaseReference db=FirebaseDatabase.getInstance().getReference().child("Users").child(storyItemInfo.getUid());
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String profilePhoto=dataSnapshot.child("profile_photo").getValue().toString();

                //retrieve image here from picassa

                holder.username.setText(dataSnapshot.child("user_name").getValue().toString());
                if(!profilePhoto.equals("default")) {
                    Picasso.with(context).load(profilePhoto)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.defaultcontact).into(holder.profile, new Callback() {
                        @Override
                        public void onSuccess() {
                            //image successfully retrived from offline
                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(profilePhoto).placeholder(R.mipmap.defaultcontact).into(holder.profile);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class StoryViewHolder extends RecyclerView.ViewHolder{

        private ImageButton mLikeBorder;
        private ImageButton mLikeFilled;
        private ImageButton mComment;
        private ImageButton mDelete;
        private ImageButton mMenu;
        private TextView mLikeCount;
        private TextView mCommentCount;
        private ImageView mStoryView;
        private TextView username;
        private TextView time;
        private CircleImageView profile;
        private boolean liked=false;
        public StoryViewHolder(final View view) {
            super(view);

            mLikeBorder=(ImageButton)view.findViewById(R.id.storyitem_like_border);
            mLikeFilled=(ImageButton)view.findViewById(R.id.storyitem_like_filled);
            mComment=(ImageButton)view.findViewById(R.id.storyitem_comment);
            mDelete=(ImageButton)view.findViewById(R.id.storyitem_delete);
            mMenu=(ImageButton)view.findViewById(R.id.story_item_menu);
            mLikeCount=(TextView)view.findViewById(R.id.storyitem_likecount);
            mCommentCount=(TextView)view.findViewById(R.id.storyitem_commentcount);
            mStoryView=(ImageView)view.findViewById(R.id.storyitem_storyphoto);
            username=(TextView)view.findViewById(R.id.storyitem_username);
            time=(TextView)view.findViewById(R.id.sstoryitem_time);
            profile=(CircleImageView)view.findViewById(R.id.storyitem_profilephoto);


            mLikeBorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(liked==false) {
                        mLikeFilled.setVisibility(v.VISIBLE);
                        liked = true;
                        int like = Integer.parseInt(mLikeCount.getText().toString());
                        like++;
                        mLikeCount.setText(like + "");
                        mLikeBorder.setVisibility(v.INVISIBLE);

                        Toast.makeText(view.getContext(),"you liked this story",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mLikeFilled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(liked==true) {
                        mLikeBorder.setVisibility(v.VISIBLE);
                        liked = false;
                        int like = Integer.parseInt(mLikeCount.getText().toString());
                        like--;
                        mLikeCount.setText(like + "");
                        mLikeFilled.setVisibility(v.INVISIBLE);

                        Toast.makeText(view.getContext(),"you unliked this story",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }
}

class DownloadTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadTask(ImageView bmImage) {
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