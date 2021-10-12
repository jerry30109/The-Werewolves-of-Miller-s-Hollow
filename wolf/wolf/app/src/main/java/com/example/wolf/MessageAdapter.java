package com.example.wolf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_NOTICE = 2;
    String side = "";
    private Context mContext;
    private List<Chating>mChat;
    private int userIdentity;
    private int visible;

    FirebaseUser fuser;
    public MessageAdapter(Context mContext, List<Chating>mChat,int visible) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.visible = visible;
    }
    @NonNull

    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_right_item, parent, false);//選擇要放在哪個畫面上

            return new MessageAdapter.ViewHolder(view);
        }
        else if(viewType == MSG_TYPE_NOTICE)
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_notice_item, parent, false);//選擇要放在哪個畫面上
            return new MessageAdapter.ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_left_item, parent, false);//選擇要放在哪個畫面上
            if(visible ==1){
                view.findViewById(R.id.left_job).setVisibility(visible);
            }
            return new MessageAdapter.ViewHolder(view);
        }
    }

    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position){
        Chating chat = mChat.get(position);
        holder.show_message.setText(chat.getMessageText());
        if(side.equals("_left"))
        {
            switch (chat.getUsercode())
            {
                case 1:
                    holder.profile_image.setImageResource(R.drawable.people);
                    break;
                case 2:
                    holder.profile_image.setImageResource(R.drawable.people_red);
                    break;
                case 3:
                    holder.profile_image.setImageResource(R.drawable.people_blue);
                    break;
                case 4:
                    holder.profile_image.setImageResource(R.drawable.people_green);
                    break;
                case 5:
                    holder.profile_image.setImageResource(R.drawable.people_pink);
                    break;
                case 6:
                    holder.profile_image.setImageResource(R.drawable.people_purple);
                    break;
            }
            switch (chat.getMessageIdentity())
            {
                case 0:
                    holder.job.setText("狼人");
                    break;
                case 1:
                    holder.job.setText("預言家");
                    break;
                case 2:
                    holder.job.setText("守衛");
                    break;
                case 3:
                    holder.job.setText("村民");
                    break;
            }

        }
        //if(imageurl.equals("default")){
        //    holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        //}
        //else{
        //Glide.vith(mContext).load(imageurl).into(holder.profile_image);
        //}
    }

    @Override
    public int getItemCount(){
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView profile_image;
        public TextView job;

        public ViewHolder(View itemView){
            super(itemView);
           if(side.equals("_right"))
           {
               show_message = itemView.findViewById(R.id.textMessage_right);
           }
           else if(side.equals("_left"))
           {
               show_message = itemView.findViewById(R.id.textMessage_left);
               job = itemView.findViewById(R.id.left_job);
               //先隨便給
               profile_image = itemView.findViewById(R.id.profile_image_chat_left);
           }
           else if(side.equals("_notice"))
           {
               show_message = itemView.findViewById(R.id.textMessage_notice);
           }
        }
    }

    //判斷是自己還是他人
    public int getItemViewType(int position){
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getMessageUser().equals(fuser.getUid())){
            side = "_right";
            return MSG_TYPE_RIGHT;
        }
        else if(mChat.get(position).getMessageUser().equals("notice"))
        {
            side = "_notice";
            return MSG_TYPE_NOTICE;
        }
        else
        {
            side = "_left";
            return  MSG_TYPE_LEFT;
        }

    }

}