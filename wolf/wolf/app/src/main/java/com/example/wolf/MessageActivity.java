package com.example.wolf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.wolf.UserDataInfo;
import com.google.firebase.database.ChildEventListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Random;

public class MessageActivity extends AppCompatActivity
{
    //0=狼人 1=預言家 2=守衛 3=平民

    AlertDialog Identitydialog = null;
    AlertDialog wolfDialog = null;
    AlertDialog proDialog = null;
    AlertDialog GuardianDialog = null;
    AlertDialog VoteDialog = null;
    RelativeLayout rl = null;
    private CountDownTimer countDownTimer;
    public boolean timerStopped;
    boolean firsttime = true;
    boolean Ready = false;
    //存取玩家ID(目前為預設)
    private ArrayList<UserDataInfo> userDataInfos = new ArrayList<UserDataInfo>();
    private UserDataInfo userDataInfo = null;
    String mUserID = null;
    String killedID;
    String prophetID;
    String GuardianID;
    String VoteID;
    MessageAdapter messageAdapter;
    List<Chating> mchat;
    int GroupCode;
    ImageButton btn_send;
    EditText text_send;
    DatabaseReference reference;
    RecyclerView recyclerView;
    //本機使用者在每個Array的位置
    int mUserPos = 12;
    //給賽後聊天用//使用者是數字好馬
    int UserCode;
    String showTitle;
    boolean hasShowEndMenu = false;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        rl = (RelativeLayout)findViewById(R.id.bottom) ;
        Intent intent = getIntent();
        mUserID = intent.getStringExtra("UserID");
        GroupCode = intent.getIntExtra("GroupCode",GroupCode);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ArrayList<String> Data = new ArrayList<String>();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        UserCode = intent.getIntExtra("UserCode",UserCode);
        text_send= (EditText)findViewById(R.id.textSend);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        btn_send =(ImageButton)findViewById(R.id.btn_send);
        rl.setVisibility(View.GONE);
        mDatabase.child("Group"+GroupCode).child("User"+UserCode).child("Ready").setValue(true);
        vSetIdentity();
        //傳訊收訊
        mchat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Group"+GroupCode).child("Chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                    Chating chat = new Chating();
                    chat.setMessageUser(String.valueOf(snapshot.child("sender").getValue()));
                    chat.setMessageText(String.valueOf(snapshot.child("message").getValue()));
                    chat.setMessageIdentity(Math.toIntExact((long) snapshot.child("Identity").getValue()));
                    chat.setUsercode(Math.toIntExact((long) snapshot.child("usercode").getValue()));
                    mchat.add(chat);
                }
                //region 判斷訊息為左或右
                messageAdapter = new MessageAdapter(MessageActivity.this,mchat,0);
                //endregion
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mag =text_send.getText().toString();
                //是否為空
                if(!mag.equals("")){
                    sendMessage(mUserID,mag,userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);
                }
                else{
                    Toast.makeText(MessageActivity.this,"不可為空!",Toast.LENGTH_SHORT).show();
                }
                // Clear the input
                text_send.setText("");
            }
        });
        //整理六位玩家資料
        mDatabase.child("Group"+GroupCode).addValueEventListener(FirstListener);

    }

    private ValueEventListener FirstListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userDataInfos.clear();
            Ready = true;
            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                if(!snapshot.getKey().equals("Chat")&&!snapshot.hasChild("Ready"))
                    Ready = false;
                else if(!snapshot.getKey().equals("Chat")&&snapshot.hasChild("Ready"))
                {
                    if((Boolean)snapshot.child("Ready").getValue()==false)
                        Ready = false;
                }

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
            if(!dataSnapshot.child("Chat").exists()&&Ready)
                vShowYourIdentity();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private ValueEventListener ChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

    public static int randomNum() { //隨機產生0 ~ 3的數字
        int num = (int)(Math.random() * 4);  // 0 ~ 3
        return num;
    }

    private void vSetIdentity()
    {
        int[] player_identity;//六位玩家身份別
        player_identity = new int[6];
        int[] identityArray;//建立職業陣列，記錄該身份目前有幾個人
        identityArray = new int[4];
        int randNum;
        firsttime = false;


        for (int i = 0; i < 4; i++) { //初始化職業陣列
            identityArray[i] = 0;
        }

        for (int i = 0; i < 6; i++) { //初始化玩家身份陣列
            player_identity[i] = -1;
        }

        for (int i = 0; i < 6; i++){
            randNum = randomNum();
            if (randNum == 0 && identityArray[randNum] ==2){
                i--;
                continue;
            }
            else if (randNum == 1 && identityArray[randNum] == 1){
                i--;
                continue;
            }
            else if (randNum == 2 && identityArray[randNum] == 1){
                i--;
                continue;
            }
            else if (randNum == 3 && identityArray[randNum] == 2){
                i--;
                continue;
            }

            identityArray[randNum] += 1;
            player_identity[i] = randNum;

        }

//        mDatabase.child("Group"+GroupCode).child("User1").child("identity").setValue(player_identity[0]);
//        mDatabase.child("Group"+GroupCode).child("User2").child("identity").setValue(player_identity[1]);
//        mDatabase.child("Group"+GroupCode).child("User3").child("identity").setValue(player_identity[2]);
//        mDatabase.child("Group"+GroupCode).child("User4").child("identity").setValue(player_identity[3]);
//        mDatabase.child("Group"+GroupCode).child("User5").child("identity").setValue(player_identity[4]);
//        mDatabase.child("Group"+GroupCode).child("User6").child("identity").setValue(player_identity[5]);
        mDatabase.child("Group"+GroupCode).child("User1").child("identity").setValue(1);
        mDatabase.child("Group"+GroupCode).child("User2").child("identity").setValue(2);
        mDatabase.child("Group"+GroupCode).child("User3").child("identity").setValue(3);
        mDatabase.child("Group"+GroupCode).child("User4").child("identity").setValue(0);
        mDatabase.child("Group"+GroupCode).child("User5").child("identity").setValue(0);
        mDatabase.child("Group"+GroupCode).child("User6").child("identity").setValue(3);
    }

    private String returnYourName(int i)
    {
        String name = null;
        switch (i)
        {
            case 0:
                name = "A";
                break;
            case 1:
                name = "B";
                break;
            case 2:
                name = "C";
                break;
            case 3:
                name = "D";
                break;
            case 4:
                name = "E";
                break;
            case 5:
                name = "F";
                break;
        }
        return name;
    }
    private void vShowYourIdentity()
    {
        mDatabase.child("Group"+GroupCode).removeEventListener(FirstListener);
        mDatabase.child("Group"+GroupCode).addValueEventListener(ChangeListener);
        mUserPos = returnYourPos();
        if(mUserPos == 10)
            Toast.makeText(MessageActivity.this,"使用者不存在",Toast.LENGTH_SHORT).show();
        else
        {
            LayoutInflater inflater = this.getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_design,null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomProgressDialog);//builder是名字
            TextView text = (TextView)view.findViewById(R.id.tvIdentity);
            String name = null;
            switch (userDataInfos.get(mUserPos).getIdentity())
            {
                case 0:
                    if(userDataInfos.get(mUserPos).getSex() == 0) {
                        for (int i = 0; i < 6; i++) {
                            if (userDataInfos.get(i).getIdentity() == 0) {
                                //顯示狼人的隊友
                                if(i!=mUserPos)//略過自己
                                    text.setText("你是:\n("+returnYourName(mUserPos)+")狼人\n另一位狼人為:\n" + returnYourName(i));
                            }
                        }
                    }
                    else{
                        for (int i = 0; i < 6; i++) {
                            if (userDataInfos.get(i).getIdentity() == 0) {
                                //顯示狼人的隊友
                                if(i!=mUserPos)
                                    text.setText("你是:\n("+returnYourName(mUserPos)+")狼人\n另一位狼人為:\n" + returnYourName(i));
                            }
                        }
                    }

                    break;
                case 1:
                    if(userDataInfos.get(mUserPos).getSex() == 0)
                        text.setText("你是:\n("+returnYourName(mUserPos)+")預言家");

                    else
                        text.setText("你是:\n("+returnYourName(mUserPos)+")預言家");
                    break;
                case 2:
                    if(userDataInfos.get(mUserPos).getSex() == 0)
                        text.setText("你是:\n("+returnYourName(mUserPos)+")守衛");

                    else
                        text.setText("你是:\n("+returnYourName(mUserPos)+")守衛");
                    break;
                case 3:
                    if(userDataInfos.get(mUserPos).getSex() == 0)
                        text.setText("你是:\n("+returnYourName(mUserPos)+")村民");
                    else
                        text.setText("你是:\n("+returnYourName(mUserPos)+")村民");
                    break;

            }
            builder.setView(view);

            Identitydialog = builder.create();
            Identitydialog.setCanceledOnTouchOutside(false);
            Identitydialog.show();
            countDownTimer = new CountDownTimer(5000,100) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if(Identitydialog != null && Identitydialog.isShowing())
                    {
                        Identitydialog.dismiss();
                        if(mUserPos == 5)
                            sendMessage("notice","天黑了...",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);
                        vNightWolf();
                    }
                }
            }.start();


        }

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int returnYourPos()
    {
        return (UserCode - 1);
    }

    private void vNightWolf()
    {
        //狼人殺人 跳出視窗 1
        killedID = null;
        //
        if(userDataInfos.get(mUserPos).getIdentity() == 0 && userDataInfos.get(mUserPos).getLive() == true)
        {
            //timer 15s
            wolfDialog = new AlertDialog.Builder(MessageActivity.this,R.style.CustomProgressDialog).create();
            LayoutInflater inflater = this.getLayoutInflater();
            final View loadView = inflater.inflate(R.layout.wolf_kill_dialog,null);
            wolfDialog.setView(loadView, 0, 0, 0, 0);
            wolfDialog.setCanceledOnTouchOutside(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomProgressDialog);
            RadioGroup wolfrg = loadView.findViewById(R.id.wolfRadioGroup);

            final RadioButton wolfrb1 = loadView.findViewById(R.id.wolfrb1);
            final TextView Timer = loadView.findViewById(R.id.wolfCountDown);
            final RadioButton wolfrb2 = loadView.findViewById(R.id.wolfrb2);
            final RadioButton wolfrb3 = loadView.findViewById(R.id.wolfrb3);
            final RadioButton wolfrb4 = loadView.findViewById(R.id.wolfrb4);
            final RadioButton wolfrb5 = loadView.findViewById(R.id.wolfrb5);
            final RadioButton wolfrb6 = loadView.findViewById(R.id.wolfrb6);
            setDieDrawable(wolfrb1,wolfrb2,wolfrb3,wolfrb4,wolfrb5,wolfrb6);
            builder.setView(loadView);
            wolfDialog = builder.create();
            wolfDialog.setCanceledOnTouchOutside(false);
            wolfDialog.show();
            //換選取時的顏色

            wolfrg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId)
                {

                    Drawable red = getResources().getDrawable(R.drawable.people_red);
                    red.setBounds(0,0,25,25);
                    Drawable blue = getResources().getDrawable(R.drawable.people_blue);
                    blue.setBounds(0,0,25,25);
                    Drawable green = getResources().getDrawable(R.drawable.people_green);
                    green.setBounds(0,0,25,25);
                    Drawable pink = getResources().getDrawable(R.drawable.people_pink);
                    pink.setBounds(0,0,25,25);
                    Drawable purple = getResources().getDrawable(R.drawable.people_purple);
                    purple.setBounds(0,0,25,25);
                    Drawable white = getResources().getDrawable(R.drawable.people);
                    white.setBounds(0,0,25,25);
                    Drawable selected = getResources().getDrawable(R.drawable.people_selected);
                    selected.setBounds(0,0,25,25);
                    switch(checkedId)
                    {
                        case R.id.wolfrb1:
                            killedID = userDataInfos.get(0).getUserID();
                            wolfrb1.setBackground(selected);
                            wolfrb2.setBackground(red);
                            wolfrb3.setBackground(blue);
                            wolfrb4.setBackground(green);
                            wolfrb5.setBackground(pink);
                            wolfrb6.setBackground(purple);
                            setDieDrawable(wolfrb1,wolfrb2,wolfrb3,wolfrb4,wolfrb5,wolfrb6);
                            break;
                        case R.id.wolfrb2:
                            killedID = userDataInfos.get(1).getUserID();
                            wolfrb1.setBackground(white);
                            wolfrb2.setBackground(selected);
                            wolfrb3.setBackground(blue);
                            wolfrb4.setBackground(green);
                            wolfrb5.setBackground(pink);
                            wolfrb6.setBackground(purple);
                            setDieDrawable(wolfrb1,wolfrb2,wolfrb3,wolfrb4,wolfrb5,wolfrb6);
                            break;
                        case R.id.wolfrb3:
                            killedID = userDataInfos.get(2).getUserID();
                            wolfrb1.setBackground(white);
                            wolfrb2.setBackground(red);
                            wolfrb3.setBackground(selected);
                            wolfrb4.setBackground(green);
                            wolfrb5.setBackground(pink);
                            wolfrb6.setBackground(purple);
                            setDieDrawable(wolfrb1,wolfrb2,wolfrb3,wolfrb4,wolfrb5,wolfrb6);
                            break;
                        case R.id.wolfrb4:
                            killedID = userDataInfos.get(3).getUserID();
                            wolfrb1.setBackground(white);
                            wolfrb2.setBackground(red);
                            wolfrb3.setBackground(blue);
                            wolfrb4.setBackground(selected);
                            wolfrb5.setBackground(pink);
                            wolfrb6.setBackground(purple);
                            setDieDrawable(wolfrb1,wolfrb2,wolfrb3,wolfrb4,wolfrb5,wolfrb6);
                            break;
                        case R.id.wolfrb5:
                            killedID = userDataInfos.get(4).getUserID();
                            wolfrb1.setBackground(white);
                            wolfrb2.setBackground(red);
                            wolfrb3.setBackground(blue);
                            wolfrb4.setBackground(green);
                            wolfrb5.setBackground(selected);
                            wolfrb6.setBackground(purple);
                            setDieDrawable(wolfrb1,wolfrb2,wolfrb3,wolfrb4,wolfrb5,wolfrb6);
                            break;
                        case R.id.wolfrb6:
                            killedID = userDataInfos.get(5).getUserID();
                            wolfrb1.setBackground(white);
                            wolfrb2.setBackground(red);
                            wolfrb3.setBackground(blue);
                            wolfrb4.setBackground(green);
                            wolfrb5.setBackground(pink);
                            wolfrb6.setBackground(selected);
                            setDieDrawable(wolfrb1,wolfrb2,wolfrb3,wolfrb4,wolfrb5,wolfrb6);
                            break;
                    }
                }
            });
            countDownTimer = new CountDownTimer(15500,5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Timer.setText(String.valueOf(millisUntilFinished / 1000));
                    if(mUserPos == 5)
                        sendMessage("notice","等待狼人投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);

                }

                @Override
                public void onFinish() {
                    Toast.makeText(MessageActivity.this,"你選擇的ID是: "+String.valueOf(killedID),Toast.LENGTH_SHORT).show();
                    if(killedID == null)
                        killedID = "";
                    mDatabase.child("Group"+GroupCode).child("User"+(mUserPos+1)).child("vote").setValue(killedID);
                    Log.e("DEMO","killID="+killedID);
                    wolfDialog.dismiss();
                    vNightProphet();
                }
            }.start();
        }
        else
        {
            //等待狼人殺完 timer 15S
            countDownTimer = new CountDownTimer(15500,5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(mUserPos == 5)
                        sendMessage("notice","等待狼人投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);

                }

                @Override
                public void onFinish() {
                    vNightProphet();
                }
            }.start();
        }

    }
    private void vNightProphet()
    {
        //預言家預人 跳出視窗 2
        prophetID = null;
        if(userDataInfos.get(mUserPos).getIdentity() == 1&& userDataInfos.get(mUserPos).getLive() == true)
        {
            //timer 15s
            proDialog = new AlertDialog.Builder(MessageActivity.this,R.style.CustomProgressDialog).create();
            LayoutInflater inflater = this.getLayoutInflater();
            final View loadView = inflater.inflate(R.layout.prophet_dialog,null);
            proDialog.setView(loadView, 0, 0, 0, 0);
            proDialog.setCanceledOnTouchOutside(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomProgressDialog);
            RadioGroup prorg = loadView.findViewById(R.id.proRadioGroup);
            final RadioButton prorb1 = loadView.findViewById(R.id.prorb1);
            final TextView Timer = loadView.findViewById(R.id.proCountDown);
            final  RadioButton prorb2 = loadView.findViewById(R.id.prorb2);
            final  RadioButton prorb3 = loadView.findViewById(R.id.prorb3);
            final RadioButton prorb4 = loadView.findViewById(R.id.prorb4);
            final  RadioButton prorb5 = loadView.findViewById(R.id.prorb5);
            final   RadioButton prorb6 = loadView.findViewById(R.id.prorb6);
            //把自己的頭像設定成不能選取
            switch(mUserPos)
            {
                case 0:
                    prorb1.setEnabled(false);
                    break;
                case 1:
                    prorb2.setEnabled(false);
                    break;
                case 2:
                    prorb3.setEnabled(false);
                    break;
                case 3:
                    prorb4.setEnabled(false);
                    break;
                case 4:
                    prorb5.setEnabled(false);
                    break;
                case 5:
                    prorb6.setEnabled(false);
                    break;
            }
            setDieDrawable(prorb1,prorb2,prorb3,prorb4,prorb5,prorb6);

            builder.setView(loadView);
            proDialog = builder.create();
            proDialog.setCanceledOnTouchOutside(false);
            proDialog.show();
            //換選取時的顏色
            prorg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId)
                {

                    Drawable red = getResources().getDrawable(R.drawable.people_red);
                    red.setBounds(0,0,25,25);
                    Drawable blue = getResources().getDrawable(R.drawable.people_blue);
                    blue.setBounds(0,0,25,25);
                    Drawable green = getResources().getDrawable(R.drawable.people_green);
                    green.setBounds(0,0,25,25);
                    Drawable pink = getResources().getDrawable(R.drawable.people_pink);
                    pink.setBounds(0,0,25,25);
                    Drawable purple = getResources().getDrawable(R.drawable.people_purple);
                    purple.setBounds(0,0,25,25);
                    Drawable white = getResources().getDrawable(R.drawable.people);
                    white.setBounds(0,0,25,25);
                    Drawable selected = getResources().getDrawable(R.drawable.people_selected);
                    selected.setBounds(0,0,25,25);
                    switch(checkedId)
                    {
                        case R.id.prorb1:
                            prophetID = userDataInfos.get(0).getUserID();
                            prorb1.setBackground(selected);
                            prorb2.setBackground(red);
                            prorb3.setBackground(blue);
                            prorb4.setBackground(green);
                            prorb5.setBackground(pink);
                            prorb6.setBackground(purple);
                            setDieDrawable(prorb1,prorb2,prorb3,prorb4,prorb5,prorb6);
                            break;
                        case R.id.prorb2:
                            prophetID = userDataInfos.get(1).getUserID();
                            prorb1.setBackground(white);
                            prorb2.setBackground(selected);
                            prorb3.setBackground(blue);
                            prorb4.setBackground(green);
                            prorb5.setBackground(pink);
                            prorb6.setBackground(purple);
                            setDieDrawable(prorb1,prorb2,prorb3,prorb4,prorb5,prorb6);
                            break;
                        case R.id.prorb3:
                            prophetID = userDataInfos.get(2).getUserID();
                            prorb1.setBackground(white);
                            prorb2.setBackground(red);
                            prorb3.setBackground(selected);
                            prorb4.setBackground(green);
                            prorb5.setBackground(pink);
                            prorb6.setBackground(purple);
                            setDieDrawable(prorb1,prorb2,prorb3,prorb4,prorb5,prorb6);
                            break;
                        case R.id.prorb4:
                            prophetID = userDataInfos.get(3).getUserID();
                            prorb1.setBackground(white);
                            prorb2.setBackground(red);
                            prorb3.setBackground(blue);
                            prorb4.setBackground(selected);
                            prorb5.setBackground(pink);
                            prorb6.setBackground(purple);
                            setDieDrawable(prorb1,prorb2,prorb3,prorb4,prorb5,prorb6);
                            break;
                        case R.id.prorb5:
                            prophetID = userDataInfos.get(4).getUserID();
                            prorb1.setBackground(white);
                            prorb2.setBackground(red);
                            prorb3.setBackground(blue);
                            prorb4.setBackground(green);
                            prorb5.setBackground(selected);
                            prorb6.setBackground(purple);
                            setDieDrawable(prorb1,prorb2,prorb3,prorb4,prorb5,prorb6);
                            break;
                        case R.id.prorb6:
                            prophetID = userDataInfos.get(5).getUserID();
                            prorb1.setBackground(white);
                            prorb2.setBackground(red);
                            prorb3.setBackground(blue);
                            prorb4.setBackground(green);
                            prorb5.setBackground(pink);
                            prorb6.setBackground(selected);
                            setDieDrawable(prorb1,prorb2,prorb3,prorb4,prorb5,prorb6);
                            break;
                    }
                }
            });

            countDownTimer = new CountDownTimer(15500,5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Timer.setText(String.valueOf(millisUntilFinished / 1000));
                    if(mUserPos == 5)
                        sendMessage("notice","等待預言家投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);

                }

                @Override
                public void onFinish() {
                    Toast.makeText(MessageActivity.this,"你選擇的ID是: "+String.valueOf(prophetID),Toast.LENGTH_SHORT).show();
                    if(prophetID == null)
                        prophetID = "";
                   for(int i = 0;i < 6;i++)
                   {
                       if(userDataInfos.get(i).getUserID().equals(prophetID))
                       {
                            switch (userDataInfos.get(i).getIdentity())
                            {
                                case 0:
                                    Toast.makeText(MessageActivity.this,"你查的人是狼人",Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Toast.makeText(MessageActivity.this,"你查的人是守衛",Toast.LENGTH_SHORT).show();
                                    break;
                                case 3:
                                    Toast.makeText(MessageActivity.this,"你查的人是平民",Toast.LENGTH_SHORT).show();
                                    break;
                            }
                       }
                   }
                    proDialog.dismiss();
                    vNightGuardian();
                }
            }.start();
        }
        else
        {
            //等待預言家選完 timer 15S
            countDownTimer = new CountDownTimer(15500,5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(mUserPos == 5)
                        sendMessage("notice","等待預言家投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);

                }

                @Override
                public void onFinish() {
                    vNightGuardian();
                }
            }.start();
        }

    }
    private void vNightGuardian()
    {
        //守衛守人 跳出視窗 3
        GuardianID = null;
        //set timer 15s
        if(userDataInfos.get(mUserPos).getIdentity() == 2&& userDataInfos.get(mUserPos).getLive() == true) {
            GuardianDialog = new AlertDialog.Builder(MessageActivity.this, R.style.CustomProgressDialog).create();
            LayoutInflater inflater = this.getLayoutInflater();
            final View loadView = inflater.inflate(R.layout.guardian_dialog, null);
            GuardianDialog.setView(loadView, 0, 0, 0, 0);
            GuardianDialog.setCanceledOnTouchOutside(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomProgressDialog);
            RadioGroup guardianrg = loadView.findViewById(R.id.GuardianRadioGroup);
            final RadioButton Guardianrb1 = loadView.findViewById(R.id.Guardianrb1);
            final TextView Timer = loadView.findViewById(R.id.GuardianCountDown);
            final RadioButton Guardianrb2 = loadView.findViewById(R.id.Guardianrb2);
            final RadioButton Guardianrb3 = loadView.findViewById(R.id.Guardianrb3);
            final RadioButton Guardianrb4 = loadView.findViewById(R.id.Guardianrb4);
            final RadioButton Guardianrb5 = loadView.findViewById(R.id.Guardianrb5);
            final RadioButton Guardianrb6 = loadView.findViewById(R.id.Guardianrb6);

            setDieDrawable(Guardianrb1,Guardianrb2,Guardianrb3,Guardianrb4,Guardianrb5,Guardianrb6);
            builder.setView(loadView);
            GuardianDialog = builder.create();
            GuardianDialog.setCanceledOnTouchOutside(false);
            GuardianDialog.show();
            //換選取時的顏色

            guardianrg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    Drawable red = getResources().getDrawable(R.drawable.people_red);
                    red.setBounds(0, 0, 25, 25);
                    Drawable blue = getResources().getDrawable(R.drawable.people_blue);
                    blue.setBounds(0, 0, 25, 25);
                    Drawable green = getResources().getDrawable(R.drawable.people_green);
                    green.setBounds(0, 0, 25, 25);
                    Drawable pink = getResources().getDrawable(R.drawable.people_pink);
                    pink.setBounds(0, 0, 25, 25);
                    Drawable purple = getResources().getDrawable(R.drawable.people_purple);
                    purple.setBounds(0, 0, 25, 25);
                    Drawable white = getResources().getDrawable(R.drawable.people);
                    white.setBounds(0, 0, 25, 25);
                    Drawable selected = getResources().getDrawable(R.drawable.people_selected);
                    selected.setBounds(0, 0, 25, 25);
                    switch (checkedId) {
                        case R.id.Guardianrb1:
                            GuardianID = userDataInfos.get(0).getUserID();
                            Guardianrb1.setBackground(selected);
                            Guardianrb2.setBackground(red);
                            Guardianrb3.setBackground(blue);
                            Guardianrb4.setBackground(green);
                            Guardianrb5.setBackground(pink);
                            Guardianrb6.setBackground(purple);
                            setDieDrawable(Guardianrb1,Guardianrb2,Guardianrb3,Guardianrb4,Guardianrb5,Guardianrb6);
                            break;
                        case R.id.Guardianrb2:
                            GuardianID = userDataInfos.get(1).getUserID();
                            Guardianrb1.setBackground(white);
                            Guardianrb2.setBackground(selected);
                            Guardianrb3.setBackground(blue);
                            Guardianrb4.setBackground(green);
                            Guardianrb5.setBackground(pink);
                            Guardianrb6.setBackground(purple);
                            setDieDrawable(Guardianrb1,Guardianrb2,Guardianrb3,Guardianrb4,Guardianrb5,Guardianrb6);
                            break;
                        case R.id.Guardianrb3:
                            GuardianID = userDataInfos.get(2).getUserID();
                            Guardianrb1.setBackground(white);
                            Guardianrb2.setBackground(red);
                            Guardianrb3.setBackground(selected);
                            Guardianrb4.setBackground(green);
                            Guardianrb5.setBackground(pink);
                            Guardianrb6.setBackground(purple);
                            setDieDrawable(Guardianrb1,Guardianrb2,Guardianrb3,Guardianrb4,Guardianrb5,Guardianrb6);
                            break;
                        case R.id.Guardianrb4:
                            GuardianID = userDataInfos.get(3).getUserID();
                            Guardianrb1.setBackground(white);
                            Guardianrb2.setBackground(red);
                            Guardianrb3.setBackground(blue);
                            Guardianrb4.setBackground(selected);
                            Guardianrb5.setBackground(pink);
                            Guardianrb6.setBackground(purple);
                            setDieDrawable(Guardianrb1,Guardianrb2,Guardianrb3,Guardianrb4,Guardianrb5,Guardianrb6);
                            break;
                        case R.id.Guardianrb5:
                            GuardianID = userDataInfos.get(4).getUserID();
                            Guardianrb1.setBackground(white);
                            Guardianrb2.setBackground(red);
                            Guardianrb3.setBackground(blue);
                            Guardianrb4.setBackground(green);
                            Guardianrb5.setBackground(selected);
                            Guardianrb6.setBackground(purple);
                            setDieDrawable(Guardianrb1,Guardianrb2,Guardianrb3,Guardianrb4,Guardianrb5,Guardianrb6);
                            break;
                        case R.id.Guardianrb6:
                            GuardianID = userDataInfos.get(5).getUserID();
                            Guardianrb1.setBackground(white);
                            Guardianrb2.setBackground(red);
                            Guardianrb3.setBackground(blue);
                            Guardianrb4.setBackground(green);
                            Guardianrb5.setBackground(pink);
                            Guardianrb6.setBackground(selected);
                            setDieDrawable(Guardianrb1,Guardianrb2,Guardianrb3,Guardianrb4,Guardianrb5,Guardianrb6);
                            break;
                    }
                }
            });
            countDownTimer = new CountDownTimer(15500,5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Timer.setText(String.valueOf(millisUntilFinished / 1000));
                    if(mUserPos == 5)
                        sendMessage("notice","等待守衛投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);
                }

                @Override
                public void onFinish() {
                    Toast.makeText(MessageActivity.this,"你選擇的ID是: "+String.valueOf(GuardianID),Toast.LENGTH_SHORT).show();

                    if(GuardianID == null)
                        GuardianID = "";
                    mDatabase.child("Group"+GroupCode).child("User"+(mUserPos+1)).child("vote").setValue(GuardianID);
                    GuardianDialog.dismiss();
                    vDawn();
                }
            }.start();
        }
        else
        {
            //等待狼人殺完 timer 15S
            countDownTimer = new CountDownTimer(15500,5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(mUserPos == 5)
                        sendMessage("notice","等待守衛投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);
                }

                @Override
                public void onFinish() {
                    vDawn();
                }
            }.start();
        }
    }
    private  void  setDieDrawable(RadioButton rb1,RadioButton rb2,RadioButton rb3,RadioButton rb4,RadioButton rb5,RadioButton rb6)
    {
        Drawable die = getResources().getDrawable(R.drawable.people_die);
        for(int i = 0;i < 6;i++)
        {
            if(userDataInfos.get(i).getLive() == false)
            {
                switch (i)
                {
                    case 0:
                        rb1.setEnabled(false);
                        rb1.setBackground(die);
                        break;
                    case 1:
                        rb2.setEnabled(false);
                        rb2.setBackground(die);
                        break;
                    case 2:
                        rb3.setEnabled(false);
                        rb3.setBackground(die);
                        break;
                    case 3:
                        rb4.setEnabled(false);
                        rb4.setBackground(die);
                        break;
                    case 4:
                        rb5.setEnabled(false);
                        rb5.setBackground(die);
                        break;
                    case 5:
                        rb6.setEnabled(false);
                        rb6.setBackground(die);
                        break;
                }
            }
        }
    }
    private void vDawn()
    {
        //公布夜晚被殺的人
       // Toast.makeText(MessageActivity.this,"今晚被殺掉的人是: "+String.valueOf(killedID),Toast.LENGTH_SHORT).show();
        boolean bBreak = false;
        for(int i = 0 ; i < 6 ; i ++)
        {
            if(userDataInfos.get(i).getIdentity() == 0)
            {
                killedID = userDataInfos.get(i).getVote();
                for(int j = 0 ; j < 6 ; j ++)
                {

                    if(!bBreak && i < j&&userDataInfos.get(j).getIdentity() == 0&&!userDataInfos.get(j).getVote().equals(userDataInfos.get(i).getVote()))
                    {
                        killedID = userDataInfos.get(i).getVote();
                        bBreak = true;
                    }
                    else if(!bBreak && i > j&&userDataInfos.get(j).getIdentity() == 0&&!userDataInfos.get(j).getVote().equals(userDataInfos.get(i).getVote()))
                    {
                        killedID = userDataInfos.get(j).getVote();
                        bBreak = true;
                    }
                }
            }
            if(userDataInfos.get(i).getIdentity() == 2)
            {
                GuardianID = userDataInfos.get(i).getVote();
            }
        }

        Toast.makeText(MessageActivity.this,"今晚被殺掉的人是: "+String.valueOf(killedID),Toast.LENGTH_SHORT).show();
        mDatabase.child("Group"+GroupCode).child("User"+(mUserPos+1)).child("vote").setValue("");
        String name = null;

        //輪流發話
        for(int i = 0 ; i < 6 ; i++)
        {
            //若為這回合被殺掉的人 即更改生死狀態為死
            if(userDataInfos.get(i).getUserID().equals(killedID)&& !killedID.equals(GuardianID))
            {
                mDatabase.child("Group"+GroupCode).child("User"+(i+1)).child("live").setValue(false);

                sendMessage("notice","天亮了",7,"Group"+GroupCode);
                sendMessage("notice","今晚被殺的人是"+returnYourName(i),7,"Group"+GroupCode);


            }
            else if(userDataInfos.get(i).getUserID().equals(killedID)&& killedID.equals(GuardianID))
            {
                if(mUserPos == 5)
                {
                    sendMessage("notice","天亮了",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);
                    sendMessage("notice","今晚守衛守到了!",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);
                }
            }
        }
        //若為本機且本機存活，即可擁有發話權
        if(userDataInfos.get(mUserPos).getLive())
            rl.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(40500,10000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(mUserPos == 5)
                    sendMessage("notice","對話時間還剩下"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);
            }

            @Override
            public void onFinish()
            {
                //投票表決 4
                vDawnVoting();
            }
        }.start();

    }
    private void vDawnVoting()
    {
        //timer
        VoteID = null;
        VoteDialog = new AlertDialog.Builder(MessageActivity.this,R.style.CustomProgressDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        final View loadView = inflater.inflate(R.layout.vote_dialog,null);
        if(userDataInfos.get(mUserPos).getLive() == true) {
            VoteDialog.setView(loadView, 0, 0, 0, 0);
            VoteDialog.setCanceledOnTouchOutside(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomProgressDialog);
            RadioGroup Voterg = loadView.findViewById(R.id.VoteRadioGroup);
            final TextView Timer = loadView.findViewById(R.id.CountDown);
            final RadioButton Voterb1 = loadView.findViewById(R.id.Voterb1);
            final RadioButton Voterb2 = loadView.findViewById(R.id.Voterb2);
            final RadioButton Voterb3 = loadView.findViewById(R.id.Voterb3);
            final RadioButton Voterb4 = loadView.findViewById(R.id.Voterb4);
            final RadioButton Voterb5 = loadView.findViewById(R.id.Voterb5);
            final RadioButton Voterb6 = loadView.findViewById(R.id.Voterb6);

            setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);

            builder.setView(loadView);
            VoteDialog = builder.create();
            VoteDialog.setCanceledOnTouchOutside(false);
            VoteDialog.show();
            countDownTimer = new CountDownTimer(15000, 5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Timer.setText(String.valueOf(millisUntilFinished / 1000));
                    if(mUserPos == 5)
                        sendMessage("notice","等待投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);

                }

                @Override
                public void onFinish() {//計時器結束後判斷遊戲是否結束
                    if(VoteID == null){
                        Toast.makeText(MessageActivity.this, "你選擇不投票!: ", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MessageActivity.this, "你選擇的ID是: " + String.valueOf(VoteID), Toast.LENGTH_SHORT).show();
                        mDatabase.child("Group"+GroupCode).child("User"+(mUserPos+1)).child("vote").setValue(VoteID);
                    }
                    VoteDialog.dismiss();
                    after_vote();
                }
            }.start();
            //換選取時的顏色

            for(int i = 0 ; i < 6 ; i++){
                if(userDataInfos.get(i).getLive() == false) {
                    switch (i) {
                        case 0:
                            Voterb1.setEnabled(false);
                            break;
                        case 1:
                            Voterb2.setEnabled(false);
                            break;
                        case 2:
                            Voterb3.setEnabled(false);
                            break;
                        case 3:
                            Voterb4.setEnabled(false);
                            break;
                        case 4:
                            Voterb5.setEnabled(false);
                            break;
                        case 5:
                            Voterb6.setEnabled(false);
                            break;
                        default:
                            break;
                    }
                }
            }
            switch (mUserPos) {
                case 0:
                    Voterb1.setEnabled(false);
                    break;
                case 1:
                    Voterb2.setEnabled(false);
                    break;
                case 2:
                    Voterb3.setEnabled(false);
                    break;
                case 3:
                    Voterb4.setEnabled(false);
                    break;
                case 4:
                    Voterb5.setEnabled(false);
                    break;
                case 5:
                    Voterb6.setEnabled(false);
                    break;
                default:
                    break;
            }

            Voterg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    Drawable red = getResources().getDrawable(R.drawable.people_red);
                    red.setBounds(0, 0, 25, 25);
                    Drawable blue = getResources().getDrawable(R.drawable.people_blue);
                    blue.setBounds(0, 0, 25, 25);
                    Drawable green = getResources().getDrawable(R.drawable.people_green);
                    green.setBounds(0, 0, 25, 25);
                    Drawable pink = getResources().getDrawable(R.drawable.people_pink);
                    pink.setBounds(0, 0, 25, 25);
                    Drawable purple = getResources().getDrawable(R.drawable.people_purple);
                    purple.setBounds(0, 0, 25, 25);
                    Drawable white = getResources().getDrawable(R.drawable.people);
                    white.setBounds(0, 0, 25, 25);
                    Drawable selected = getResources().getDrawable(R.drawable.people_selected);
                    selected.setBounds(0, 0, 25, 25);
                    switch (checkedId) {
                        case R.id.Voterb1:
                            VoteID = userDataInfos.get(0).getUserID();
                            Toast.makeText(MessageActivity.this, "你選擇的ID是: " + String.valueOf(VoteID), Toast.LENGTH_SHORT).show();
                            Voterb1.setBackground(selected);
                            Voterb2.setBackground(red);
                            Voterb3.setBackground(blue);
                            Voterb4.setBackground(green);
                            Voterb5.setBackground(pink);
                            Voterb6.setBackground(purple);
                            setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                            break;
                        case R.id.Voterb2:
                            VoteID = userDataInfos.get(1).getUserID();
                            Toast.makeText(MessageActivity.this, "你選擇的ID是: " + String.valueOf(VoteID), Toast.LENGTH_SHORT).show();
                            Voterb1.setBackground(white);
                            Voterb2.setBackground(selected);
                            Voterb3.setBackground(blue);
                            Voterb4.setBackground(green);
                            Voterb5.setBackground(pink);
                            Voterb6.setBackground(purple);
                            setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                            break;
                        case R.id.Voterb3:
                            VoteID = userDataInfos.get(2).getUserID();
                            Toast.makeText(MessageActivity.this, "你選擇的ID是: " + String.valueOf(VoteID), Toast.LENGTH_SHORT).show();
                            Voterb1.setBackground(white);
                            Voterb2.setBackground(red);
                            Voterb3.setBackground(selected);
                            Voterb4.setBackground(green);
                            Voterb5.setBackground(pink);
                            Voterb6.setBackground(purple);
                            setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                            break;
                        case R.id.Voterb4:
                            VoteID = userDataInfos.get(3).getUserID();
                            Toast.makeText(MessageActivity.this, "你選擇的ID是: " + String.valueOf(VoteID), Toast.LENGTH_SHORT).show();
                            Voterb1.setBackground(white);
                            Voterb2.setBackground(red);
                            Voterb3.setBackground(blue);
                            Voterb4.setBackground(selected);
                            Voterb5.setBackground(pink);
                            Voterb6.setBackground(purple);
                            setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                            break;
                        case R.id.Voterb5:
                            VoteID = userDataInfos.get(4).getUserID();
                            Toast.makeText(MessageActivity.this, "你選擇的ID是: " + String.valueOf(VoteID), Toast.LENGTH_SHORT).show();
                            Voterb1.setBackground(white);
                            Voterb2.setBackground(red);
                            Voterb3.setBackground(blue);
                            Voterb4.setBackground(green);
                            Voterb5.setBackground(selected);
                            Voterb6.setBackground(purple);
                            setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                            break;
                        case R.id.Voterb6:
                            VoteID = userDataInfos.get(5).getUserID();
                            Toast.makeText(MessageActivity.this, "你選擇的ID是: " + String.valueOf(VoteID), Toast.LENGTH_SHORT).show();
                            Voterb1.setBackground(white);
                            Voterb2.setBackground(red);
                            Voterb3.setBackground(blue);
                            Voterb4.setBackground(green);
                            Voterb5.setBackground(pink);
                            Voterb6.setBackground(selected);
                            setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                            break;
                    }
                }
            });
            final Button btnUnvote = loadView.findViewById(R.id.UNvotebutton);
            //不投票的選項

            btnUnvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Drawable red = getResources().getDrawable(R.drawable.people_red);
                    red.setBounds(0, 0, 25, 25);
                    Drawable blue = getResources().getDrawable(R.drawable.people_blue);
                    blue.setBounds(0, 0, 25, 25);
                    Drawable green = getResources().getDrawable(R.drawable.people_green);
                    green.setBounds(0, 0, 25, 25);
                    Drawable pink = getResources().getDrawable(R.drawable.people_pink);
                    pink.setBounds(0, 0, 25, 25);
                    Drawable purple = getResources().getDrawable(R.drawable.people_purple);
                    purple.setBounds(0, 0, 25, 25);
                    Drawable white = getResources().getDrawable(R.drawable.people);
                    white.setBounds(0, 0, 25, 25);
                    Drawable selected = getResources().getDrawable(R.drawable.people_selected);
                    selected.setBounds(0, 0, 25, 25);
                    setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                    Toast.makeText(MessageActivity.this, "你選擇不投票!: ", Toast.LENGTH_SHORT).show();
                    VoteID = null;
                    Voterb1.setBackground(white);
                    Voterb2.setBackground(red);
                    Voterb3.setBackground(blue);
                    Voterb4.setBackground(green);
                    Voterb5.setBackground(pink);
                    Voterb6.setBackground(purple);
                    setDieDrawable(Voterb1, Voterb2, Voterb3, Voterb4, Voterb5, Voterb6);
                    mDatabase.child("Group"+GroupCode).child("User"+(mUserPos+1)).child("vote").setValue("");
                }
            });
        }
        else{//死掉的人會到這邊
            countDownTimer = new CountDownTimer(15600,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(mUserPos == 5)
                        sendMessage("notice","等待投完票,還剩"+String.valueOf(millisUntilFinished / 1000)+"秒",userDataInfos.get(mUserPos).getIdentity(),"Group"+GroupCode);

                }

                @Override
                public void onFinish() {
                    after_vote();
                    /*if(bIsEnd()==true)
                        vShowEndMenu();

                    else
                        vNightWolf();*/
                }
            }.start();
        }
    }
    private void after_vote()
    {
        countDownTimer = new CountDownTimer(3000,100)
        {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish()
            {
                int[] if_same_vote;
                if_same_vote = new int[6];      //存同票的人
                int size_of_vote = 0;               //紀錄有多少人同票
                int[] vote_count;
                vote_count = new int[6];
                for(int i = 0;i < 6;i++)        //歸零投票數
                    vote_count[i] = 0;
                for(int i = 0;i < 6;i++){           //vote
                    for(int j = 0;j < 6;j++){       //userID
                        if(userDataInfos.get(i).getVote().equals(userDataInfos.get(j).getUserID())){
                            vote_count[j] += 1;
                        }
                    }
                }
                int max = -1;
                for(int i = 0; i < 6; i++){
                    if(vote_count[i] > max)
                        max = vote_count[i];
                }
                if(max <= 0){
                    Toast.makeText(MessageActivity.this, "今天沒人投票" , Toast.LENGTH_SHORT).show();
                }
                else {
                    int vote_final = 0;
                    for (int i = 0; i < 6; i++) {
                        if (vote_count[i] == max) {
                            if_same_vote[size_of_vote] = i;
                            size_of_vote++;
                        }
                    }
                    if(size_of_vote > 1){
                        vote_final = if_same_vote[0];
                    }
                    else{
                        vote_final = if_same_vote[0];       //沒有重複票數的情況
                    }
                    mDatabase.child("Group"+GroupCode).child("User"+(vote_final+1)).child("live").setValue(false);       //殺掉被投死的人
                    Toast.makeText(MessageActivity.this, "今天"  + returnYourName(vote_final)+"被投票出局",Toast.LENGTH_SHORT).show();
                }

            }
        }.start();

        countDownTimer = new CountDownTimer(5000,100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (bIsEnd())
                    vShowEndMenu();
                else
                    vNightWolf();
            }
        }.start();

    }
    private boolean bIsEnd()
    {
        //若狼人全死or好人全死 就回傳true 否則回傳false
        //自己是狼且活著
        if(userDataInfos.get(mUserPos).getIdentity() == 0 && userDataInfos.get(mUserPos).getLive())
        {
            for(int i = 0 ; i < 6 ; i ++)
            {
                if(i != mUserPos && userDataInfos.get(i).getIdentity() != 0 && userDataInfos.get(i).getLive())
                    return false;
            }
            showTitle = "你贏了！";
            return true;
        }
        //自己是狼但死了
        else if(userDataInfos.get(mUserPos).getIdentity() == 0 && !userDataInfos.get(mUserPos).getLive())
        {
            //確認狼隊友是否生存
            for(int i = 0 ; i < 6 ; i++)
            {
                //若狼隊友也死亡 遊戲結束
                if(i != mUserPos && userDataInfos.get(i).getIdentity() == 0 && !userDataInfos.get(i).getLive()) {
                    showTitle = "你輸了！";
                    return true;
                }
                //若狼隊友生存，好人有一個活著 夜晚在開
                else if(i != mUserPos && userDataInfos.get(i).getIdentity() == 0 && userDataInfos.get(i).getLive())
                {
                    for(int j = 0 ; j < 6 ; j ++)
                    {
                        if(j != mUserPos && userDataInfos.get(j).getIdentity() != 0 && userDataInfos.get(j).getLive())
                            return false;
                    }
                    return true;
                }
            }
        }

        //自己是好人且活著
        else if(userDataInfos.get(mUserPos).getIdentity() != 0 && userDataInfos.get(mUserPos).getLive())
        {
            int wolf = 0;
            for(int i = 0 ; i < 6 ; i ++)
            {
                if(i != mUserPos && userDataInfos.get(i).getIdentity() == 0 && userDataInfos.get(i).getLive())
                    return false;
                else if(i != mUserPos && userDataInfos.get(i).getIdentity() == 0 && !userDataInfos.get(i).getLive())
                {
                    wolf++;
                    if(wolf == 2) {
                        showTitle = "你贏了！";
                        return true;
                    }
                }
            }
        }
        //自己是好人但死了
        else if(userDataInfos.get(mUserPos).getIdentity() != 0 && !userDataInfos.get(mUserPos).getLive())
        {
            int wolf = 0;
            boolean goodLive = false;
            for(int i = 0 ; i < 6 ; i ++)
            {
                if(i != mUserPos && userDataInfos.get(i).getIdentity() != 0 && userDataInfos.get(i).getLive())
                    goodLive = true;
            }
            if(goodLive == true)
            {
                for(int i = 0 ; i < 6 ; i ++)
                {
                    if(i != mUserPos && userDataInfos.get(i).getIdentity() == 0 && userDataInfos.get(i).getLive())
                        return false;
                    else if(i != mUserPos && userDataInfos.get(i).getIdentity() == 0 && !userDataInfos.get(i).getLive())
                    {
                        wolf++;
                        if(wolf == 2) {
                            showTitle = "你贏了！";
                            return true;
                        }
                    }
                }
            }
        }
        showTitle = "你輸了！";
        return true;
    }

    private void setEndingImage(ImageView imgView,int i)
    {
        switch(i)
        {
            case 0:
                imgView.setImageResource(R.drawable.people);
                break;
            case 1:
                imgView.setImageResource(R.drawable.people_red);
                break;
            case 2:
                imgView.setImageResource(R.drawable.people_blue);
                break;
            case 3:
                imgView.setImageResource(R.drawable.people_green);
                break;
            case 4:
                imgView.setImageResource(R.drawable.people_pink);
                break;
            case 5:
                imgView.setImageResource(R.drawable.people_purple);
                break;

        }
    }
    private void vShowEndMenu()
    {
        hasShowEndMenu = true;
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.ending_dialog_design,null);
        ImageView img = null;
        int personCount = 0;
        int wolfCount = 0;
        for(int i = 0 ; i < 6 ; i ++)
        {
            switch (userDataInfos.get(i).getIdentity())
            {
                case 0:
                    if(wolfCount == 0)
                        img = (ImageView)view.findViewById(R.id.imageView5);
                    else if (wolfCount == 1)
                        img = (ImageView)view.findViewById(R.id.imageView6);
                    setEndingImage(img,i);
                    wolfCount++;
                    break;
                case 1:
                    img = (ImageView)view.findViewById(R.id.imageView3);
                    setEndingImage(img,i);
                    break;
                case 2:
                    img = (ImageView)view.findViewById(R.id.imageView4);
                    setEndingImage(img,i);
                    break;
                case 3:
                    if(personCount == 0)
                        img = (ImageView)view.findViewById(R.id.imageView1);
                    else if (personCount == 1)
                        img = (ImageView)view.findViewById(R.id.imageView2);
                    setEndingImage(img,i);
                    personCount++;
                    break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//builder是名字
        builder.setTitle(showTitle)
                .setView(view)
                .setPositiveButton("繼續聊天", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        //傳至下一個聊天室
                        Intent intent = new Intent(MessageActivity.this,EndingChatActivity.class);
                        intent.putExtra("UserID",mUserID);
                        intent.putExtra("GroupCode",GroupCode);
                        //使用者位置//1.2...
                        intent.putExtra("UserCode",UserCode);
                        //Group數字//0.1.2...
                        intent.putExtra("GroupCode",GroupCode);
                        finish();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("返回標題", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id)
                    {
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

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

}
