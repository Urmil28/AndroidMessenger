/*
Adapter class for messages
oncreateviewholder inflates the layout of vie to be shown as item of recycler view
onbindviewHolder binds data to viewholder object
getItemcount returns total no of item in recycler view
getItemType returns type of item to be shown our_msg other_msg

class holder extended from recyclerview.viewholder creates prototype of view
 */

package com.ravensltd.ravens.ChatUI;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ravensltd.ravens.R;

import java.util.List;

/**
 * Created by jatin on 6/10/17.
 */
//package com.ravensltd.ravens.ChatUI;
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<MessageItemInfo> mMessageList;

    private FirebaseAuth mAuth;

    public MessageAdapter(List<MessageItemInfo> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        switch(viewType){
            case 0:
                view= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_message_other_item,parent,false);
                break;
            case 1:
                view= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_message_our_item,parent,false);
        }

        return new MessageViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {


        MessageItemInfo itemInfo=mMessageList.get(position);
        holder.messageView.setText(itemInfo.getMessage());
        holder.timeView.setText(itemInfo.getTime().toString());

        switch(getItemViewType(position)){
            case 0:
                holder.messageView.setBackgroundResource(R.drawable.message_other_background);
                holder.messageView.setTextColor(Color.BLACK);
                break;
            case 1:
                holder.messageView.setBackgroundResource(R.drawable.message_our_background);
                holder.messageView.setTextColor(Color.BLACK);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        mAuth=FirebaseAuth.getInstance();
        String currentUserId=mAuth.getCurrentUser().getUid().toString();

        MessageItemInfo itemInfo=mMessageList.get(position);

        String fromUser=itemInfo.getFrom();

        if(fromUser.equals(currentUserId)){
            return 1;
        }
        else{
            return 0;
        }
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageView;
        public TextView timeView;

        public MessageViewHolder(View itemView,int viewType) {
            super(itemView);

            if(viewType==0) {
                this.messageView = (TextView) itemView.findViewById(R.id.msg_other_item_message);
                this.timeView = (TextView) itemView.findViewById(R.id.msg_other_item_time);
            }
            if(viewType==1){
                this.messageView = (TextView) itemView.findViewById(R.id.msg_our_item_message);
                this.timeView = (TextView) itemView.findViewById(R.id.msg_our_item_time);
            }
        }
    }
}
