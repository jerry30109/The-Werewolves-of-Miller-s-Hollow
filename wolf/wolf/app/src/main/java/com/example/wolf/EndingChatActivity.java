package com.example.wolf;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EndingChatActivity extends AppCompatActivity
{
    Button mReturn = null;
    String mUserID = null;
    int GroupCode;
    //user1
    int UserCode;
    private DatabaseReference mDatabase;
    RecyclerView end_recyclerView;
    DatabaseReference reference;
    UserDataInfo userDataInfo;
    ArrayList<UserDataInfo> userDataInfos = new ArrayList<UserDataInfo>();
    //region 傳訊變數
    List<Chating> mchat;
    MessageAdapter messageAdapter;
    ImageButton endbtn_send;
    EditText endtextSend;
    //endregion

    Button btn_endreturn;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ending_chat);
        Intent intent = getIntent();

        //region 共用變數
        mUserID = intent.getStringExtra("UserID");
        mReturn = (Button)findViewById(R.id.btn_endreturn);
        GroupCode = intent.getIntExtra("GroupCode",GroupCode);
        //user所在位置
        UserCode = intent.getIntExtra("UserCode",UserCode);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //endregion

        vSetOnClick();

        //region 傳訊收訊
        //region 傳訊變數
        mchat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        endbtn_send =(ImageButton)findViewById(R.id.endbtn_send);
        end_recyclerView = (RecyclerView)findViewById(R.id.endrecycler_view);
        end_recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        end_recyclerView.setLayoutManager(linearLayoutManager);

        //endregion

        mDatabase.child("Group"+GroupCode).addListenerForSingleValueEvent(listener);
        mDatabase.child("Group"+GroupCode).removeEventListener(listener);
        //region 若資料數紀錄有變化，判別傳訊對象
        mDatabase.child("Group"+GroupCode).child("Chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                    Chating chat = new Chating();
                    chat.setMessageUser(String.valueOf(snapshot.child("sender").getValue()));
                    chat.setMessageIdentity(Math.toIntExact((long)snapshot.child("Identity").getValue()));
                    chat.setMessageText(String.valueOf(snapshot.child("message").getValue()));
                    chat.setUsercode(Math.toIntExact((long) snapshot.child("usercode").getValue()));
                    mchat.add(chat);
                }
                //region 判斷訊息為左或右
                messageAdapter = new MessageAdapter(EndingChatActivity.this,mchat,1);
                //endregion

                end_recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //endregion

        //region 按鈕動作
        endbtn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endtextSend= (EditText)findViewById(R.id.endtextSend);
                String mag =endtextSend.getText().toString();
                //是否為空
                if(!mag.equals("")){
                    sendMessage(mUserID,mag,userDataInfos.get(UserCode-1).getIdentity(),"Group"+GroupCode);
                }
                else{
                    Toast.makeText(EndingChatActivity.this,"不可為空!",Toast.LENGTH_SHORT).show();
                }
                // Clear the input
                endtextSend.setText("");
            }
        });
        //endregion
        //endregion

        //region (按鈕)返回標題
        btn_endreturn=(Button)findViewById(R.id.btn_endreturn);
        btn_endreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameover_returnToTitle();
            }
        });
        //endregion

    }

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
        {
            userDataInfos.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                if(!snapshot.getKey().equals("Chat"))
                {
                    userDataInfos.add(new UserDataInfo(
                            String.valueOf(snapshot.child("userID").getValue()),
                            Math.toIntExact((long) snapshot.child("sex").getValue()),
                            Math.toIntExact((long) snapshot.child("identity").getValue()),
                            (Boolean) snapshot.child("live").getValue(),
                            (Boolean) snapshot.child("speak").getValue(),
                            String.valueOf(snapshot.child("vote").getValue())));
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void vSetOnClick()
    {
        mReturn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //以下為傳訊相關
    //查看最新的訊息，紀錄最新的收件者(職位)
    private void sendMessage(String sender,String message,int identity,String GroupNameNum){
        reference = FirebaseDatabase.getInstance().getReference(GroupNameNum);
        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("sender",sender);
        hashMap.put("message",message);
        hashMap.put("Identity",identity);
        hashMap.put("usercode",UserCode);

        //Toast.makeText(MessageActivity.this,GroupNameNum,Toast.LENGTH_SHORT).show();
        reference.child("Chat").push().setValue(hashMap);
    }

    //region 返回標題
    public void gameover_returnToTitle()
    {
        Intent intent= new Intent();
        intent.setClass(EndingChatActivity.this,MainActivity.class);
        startActivity(intent);
    }
    //endregion

}
