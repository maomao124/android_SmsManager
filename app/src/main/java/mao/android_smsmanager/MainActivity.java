package mao.android_smsmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity
{

    private PendingIntent sentPI;
    private BroadcastReceiver broadcastReceiver;
    private PendingIntent deliverPI;
    private BroadcastReceiver broadcastReceiver1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.Button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }


    /**
     * 发送短信
     *
     * @param phoneNumber 电话号码
     * @param message     消息
     */
    public void sendSMS(String phoneNumber, String message)
    {
        //获取短信管理器
        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
        //拆分短信内容（手机短信长度限制）,貌似长度限制为140个字符,就是
        //只能发送70个汉字,多了要拆分成多条短信发送
        //第四五个参数,如果没有需要监听发送状态与接收状态的话可以写null
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents)
        {
            smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliverPI);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //处理返回的发送状态
        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, 0);
        //注册发送信息的广播接收者
        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context _context, Intent _intent)
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "短信发送成功", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:    //普通错误
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:         //无线广播被明确地关闭
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:          //没有提供pdu
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:         //服务当前不可用
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(SENT_SMS_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);


        //处理返回的接收状态
        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
        //创建接收返回的接收状态的Intent
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        IntentFilter filter = new IntentFilter(DELIVERED_SMS_ACTION);
        broadcastReceiver1 = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context _context, Intent _intent)
            {
                Toast.makeText(MainActivity.this, "收信人已经成功接收", Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(broadcastReceiver1, filter);

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(broadcastReceiver1);
    }
}