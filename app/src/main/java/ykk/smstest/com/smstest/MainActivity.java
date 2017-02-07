package ykk.smstest.com.smstest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView sender;
    private  TextView content;
    private IntentFilter receiveFilter;
    private IntentFilter sendFilter;
    private SendStatusReceiver sendStatusReceiver;
    private  MessageReceiver messageReceiver;
    private EditText msgInput;
    private Button send;
    private  EditText to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sender= (TextView) findViewById(R.id.sender);
        content= (TextView) findViewById(R.id.content);
        send= (Button) findViewById(R.id.send);
        to= (EditText) findViewById(R.id.to);
        msgInput= (EditText) findViewById(R.id.msg_input);
        //接受短信广播注册
        receiveFilter=new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        messageReceiver=new MessageReceiver();
        registerReceiver(messageReceiver,receiveFilter);
        //发送短信广播注册
        sendFilter=new IntentFilter();
        sendFilter.addAction("SENT_SMS_ACTION");
        sendStatusReceiver=new SendStatusReceiver();
        registerReceiver(sendStatusReceiver,sendFilter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsManager smsManager=SmsManager.getDefault();
                Intent sentIntent=new Intent("SENT_SMS_ACTION");
                PendingIntent pi=PendingIntent.getBroadcast(MainActivity.this,0,sentIntent,0);
                /*
                 第一个参数是收信人号码，第二个参数是发信人号码，第三个参数是发信内容，
                 第四个参数是监听短信是否发送成功，第五个参数是监听对法是否接收成功。
                 */
                smsManager.sendTextMessage(to.getText().toString(),null,msgInput.getText().toString(),pi,null);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
    }
    //发送广播
    class SendStatusReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getResultCode()==RESULT_OK)
            {
                Toast.makeText(context,"Send Succeeded",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context,"Send failed",Toast.LENGTH_SHORT).show();
            }
        }
    }
    //接收广播
    class MessageReceiver extends BroadcastReceiver
    {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle=intent.getExtras();
            Object[] pdus= (Object[]) bundle.get("pdus");//提取短信消息
            String format=intent.getStringExtra("format");
            SmsMessage[] message=new SmsMessage[pdus.length];
            for(int i=0;i<message.length;i++)
            {
                //下面方法已被谷歌废弃.应使用第二行的方法。
                //message[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
                message[i]=SmsMessage.createFromPdu((byte[]) pdus[i],format);
            }
            //获取发送方的号码
            String address=message[0].getOriginatingAddress();
            String fullMessage="";
            for(SmsMessage messagel:message)
            {
                //获取短信内容
                fullMessage+=messagel.getMessageBody();
            }
            sender.setText(address);
            content.setText(fullMessage);
        }
    }
}
