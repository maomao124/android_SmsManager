package mao.android_smsmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity
{

    private PendingIntent sentPI;
    private BroadcastReceiver broadcastReceiver;
    private PendingIntent deliverPI;
    private BroadcastReceiver broadcastReceiver1;
    private EditText editText1;
    private EditText editText2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = findViewById(R.id.EditText1);
        editText2 = findViewById(R.id.EditText2);

        findViewById(R.id.Button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendSMS(R.id.Button);
            }
        });
    }


    /**
     * 发送短信
     */
    public void sendSMS()
    {
        String phone = editText1.getText().toString();
        String content = editText2.getText().toString();
        if (phone.length() == 0)
        {
            return;
        }
        if (content.length() == 0)
        {
            return;
        }
        sendSMS(phone, content);
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


    /**
     * 发送短信
     *
     * @param requestCode 请求代码
     */
    public void sendSMS(int requestCode)
    {
        if (checkPermission(MainActivity.this, Manifest.permission.SEND_SMS,
                requestCode % 65536))
        {
            sendSMS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == R.id.Button % 65536)
        {
            if (checkGrant(grantResults))
            {
                sendSMS();
            }
            else
            {
                toastShow("没有权限");
            }
        }
    }


    /**
     * 检查权限结果数组，
     *
     * @param grantResults 授予相应权限的结果是PackageManager.PERMISSION_GRANTED
     *                     或PackageManager.PERMISSION_DENIED
     *                     从不为空
     * @return boolean 返回true表示都已经获得授权 返回false表示至少有一个未获得授权
     */
    public static boolean checkGrant(int[] grantResults)
    {
        boolean result = true;
        if (grantResults != null)
        {
            for (int grant : grantResults)
            {
                //遍历权限结果数组中的每条选择结果
                if (grant != PackageManager.PERMISSION_GRANTED)
                {
                    //未获得授权，返回false
                    result = false;
                    break;
                }
            }
        }
        else
        {
            result = false;
        }
        return result;
    }


    /**
     * 检查某个权限
     *
     * @param act         Activity对象
     * @param permission  许可
     * @param requestCode 请求代码
     * @return boolean 返回true表示已启用该权限，返回false表示未启用该权限
     */
    public static boolean checkPermission(Activity act, String permission, int requestCode)
    {
        return checkPermission(act, new String[]{permission}, requestCode);
    }


    /**
     * 检查多个权限
     *
     * @param act         Activity对象
     * @param permissions 权限
     * @param requestCode 请求代码
     * @return boolean 返回true表示已完全启用权限，返回false表示未完全启用权限
     */
    @SuppressWarnings("all")
    public static boolean checkPermission(Activity act, String[] permissions, int requestCode)
    {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int check = PackageManager.PERMISSION_GRANTED;
            //通过权限数组检查是否都开启了这些权限
            for (String permission : permissions)
            {
                check = ContextCompat.checkSelfPermission(act, permission);
                if (check != PackageManager.PERMISSION_GRANTED)
                {
                    //有个权限没有开启，就跳出循环
                    break;
                }
            }
            if (check != PackageManager.PERMISSION_GRANTED)
            {
                //未开启该权限，则请求系统弹窗，好让用户选择是否立即开启权限
                ActivityCompat.requestPermissions(act, permissions, requestCode);
                result = false;
            }
        }
        return result;
    }

    /**
     * 显示消息
     *
     * @param message 消息
     */
    private void toastShow(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}