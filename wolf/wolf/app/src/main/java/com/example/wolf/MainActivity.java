package com.example.wolf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    UserDataInfo userDataInfo = null;
    boolean first = true;
    String mUserID = null;
    Button mButton = null ;
    RadioButton boy = null;
    RadioButton girl = null;
    private static AlertDialog mAlertDialog;
    TextView amount =null;
    int i = 0 ;//Group
    int UserCode;//User
    int GroupCode;
    private DatabaseReference mDatabase;// ...


    Boolean Trigger = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button)findViewById(R.id.mbutton);
        boy = (RadioButton)findViewById(R.id.boy);
        girl = (RadioButton)findViewById(R.id.girl);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"匿名登入成功 uid:\n" + mAuth.getCurrentUser().getUid(),Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this,"匿名登入失敗",Toast.LENGTH_LONG).show();        }
            }
        });

        mButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Trigger = false;
                int sex = 0;
                if(boy.isChecked() == true || girl.isChecked() == true)
                {
                    if(boy.isChecked())
                        sex = 0;
                    else if(girl.isChecked())
                        sex = 1;
                    final int finalSex = sex;
                    mAlertDialog = new AlertDialog.Builder(MainActivity.this,R.style.CustomProgressDialog).create();
                    View loadView = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_progress_dialog_view, null);
                    mAlertDialog.setView(loadView, 0, 0, 0, 0);
                    mAlertDialog.setCanceledOnTouchOutside(false);
                    TextView tvTip = loadView.findViewById(R.id.tvTip);
                    amount = loadView.findViewById(R.id.tvAmount);
                    tvTip.setText("正在引入村民...");

                    mAlertDialog.show();
                    mUserID = mAuth.getCurrentUser().getUid();//放置FireBase給予的ID
                    userDataInfo = new UserDataInfo(mUserID,finalSex,0,true,false,"");
                    mDatabase.child("Group0").child(mUserID).setValue(mUserID);

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            int j = 0;//GroupCount
                            for(DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                j++;
                                if(snapshot.getKey().equals("Group0")&&dataSnapshot.getChildrenCount() == 1)
                                {
                                    mDatabase.child("Group1").child("User"+(snapshot.getChildrenCount())).setValue(userDataInfo);
                                    mDatabase.child("Group0").child(mUserID).removeValue();
                                    Log.e("DEMO","AAA");
                                    GroupCode = 1;
                                    UserCode = 1;
                                    mDatabase.child("Group"+GroupCode).addValueEventListener(valueEventListener);
                                }
                                else if(!snapshot.getKey().equals("Group0") && snapshot.getChildrenCount() < 6)
                                {
                                    mDatabase.child(snapshot.getKey()).child("User"+(snapshot.getChildrenCount()+1)).setValue(userDataInfo);
                                    GroupCode = Math.toIntExact(dataSnapshot.getChildrenCount() - 1);
                                    mDatabase.child("Group0").child(mUserID).removeValue();
                                    UserCode = (int)(snapshot.getChildrenCount()+1);
                                    mDatabase.child("Group"+GroupCode).addValueEventListener(valueEventListener);
                                    Log.e("DEMO","BBB");
                                }
                                else if(!snapshot.getKey().equals("Group0") && snapshot.getChildrenCount() >= 6)
                                {
                                    if(!dataSnapshot.hasChild("Group"+j))
                                    {
                                        mDatabase.child("Group"+j).child("User1").setValue(userDataInfo);
                                        GroupCode = j;
                                        mDatabase.child("Group0").child(mUserID).removeValue();
                                        UserCode = 1;
                                        mDatabase.child("Group"+GroupCode).addValueEventListener(valueEventListener);
                                        Log.e("DEMO","CCC");
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {}
                    });
                }
                else
                    Toast.makeText(MainActivity.this,"未選擇性別",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(first == true && dataSnapshot.getChildrenCount() == 6)
            {
                first = false;
                Intent intent = new Intent(MainActivity.this,MessageActivity.class);
                intent.putExtra("GroupCode",GroupCode);
                intent.putExtra("UserID",mUserID);
                //使用者位置//1.2...
                intent.putExtra("UserCode",UserCode);
                if(Trigger == false)
                    startActivity(intent);
                if (mAlertDialog != null && mAlertDialog.isShowing()) {
                    mAlertDialog.dismiss();
                }
            }
            else
            {
                if(mAlertDialog.isShowing())
                {
                    amount.setText(dataSnapshot.getChildrenCount()+"/6");
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    public void returnToTitle(View view)
    {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        mDatabase.child("Group"+GroupCode).child("User"+UserCode).removeValue();
        mDatabase.removeEventListener(valueEventListener);
        Trigger = true;
    }
    //攔截返回鍵使其無效
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
