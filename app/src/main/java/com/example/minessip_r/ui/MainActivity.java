package com.example.minessip_r.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.minessip_r.Constants;
import com.example.minessip_r.R;
import com.example.minessip_r.WifiClientThread;
import com.example.minessip_r.fragment.ControllerFragment;
import com.example.minessip_r.fragment.DataFragment;
import com.example.minessip_r.fragment.LogFragment;
import com.example.minessip_r.fragment.NoneFileFragment;
import com.example.minessip_r.service.BluetoothChatService;
import com.example.minessip_r.test.WifiServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import xcrash.XCrash;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_SET_NETWORK = 2;
    //public String filesize;
    private String currentDeviceName;
    private BluetoothChatService mChatService;
    private BluetoothAdapter mBluetoothAdapter;


    public MenuItem itemConnect;

    public View logLayout;
    private ImageView logImage;
    private TextView logTextView;
    public String logContent="";
    public View controllerLayout;
    private ImageView controllerImage;
    public View dataLayout;
    private ImageView dataImage;

    private LogFragment logFragment;
    private ControllerFragment controllerFragment;
    private DataFragment dataFragment;
    private NoneFileFragment noneFileFragment;

    private FragmentManager fragmentManager;

    public static boolean getip = true;
    public static int wifiClientThreadState=0;
    public static int fragmentState = 0;//用来判断当前页面是否为controllerframent，防止更新文件接收进度信息时程序崩溃

    public static String configCommend="config/I/1uA/250/b9bf1a/123456/MMS-Data\r\n";

    public final Handler mHandler = new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case BluetoothChatService.MESSAGE_BT_READ:
                    Bundle data = msg.getData();
                    String reply = data.getString("BTdata");
                    Log.e("Length",String.valueOf(reply.length()) + "  " + reply);
                    String[] infoToDispaly = reply.split("\r\n");
                    if (reply.contains("filesize")){
                        int firstIndex, lastIndex;
                        firstIndex = reply.indexOf("filesize");
                        lastIndex = reply.indexOf("\r\n", firstIndex);
                        String filesize = reply.substring(firstIndex+8, lastIndex);
                        Log.e(TAG, "handleMessage: bt_read: "+filesize);
                        //networkFragment.setFileSize(filesize);
                        break;
                    }
                    for(int i = 0; i<infoToDispaly.length; i++){
                        logAppend(currentDeviceName+": "+infoToDispaly[i]+"\n");//显示相应信息
                        Log.e(TAG, "handleMessage: bt_read: "+ Arrays.toString(infoToDispaly));
                    }
                    break;
                case BluetoothChatService.MESSAGE_DEVICE_NAME:
                    itemConnect.setEnabled(true);
                    itemConnect.setTitle("断开");
                    break;
                case BluetoothChatService.MESSAGE_STATE_CHANGE:
                    //更新连接状态显示

                    switch (msg.arg1){

                        case BluetoothChatService.STATE_CONNECTING:
                            setSubtitle("正在连接...");
                            break;
                        case BluetoothChatService.STATE_NOT_CONNECTED:
                            itemConnect.setEnabled(true);
                            itemConnect.setTitle("连接");
                            setSubtitle("未连接");
                            /*if (networkFragment != null) {
                                networkFragment.disableAllView();
                                if (networkFragment.getRecieveState())
                                    networkFragment.setViewEnabled(R.id.network_button_get_data, true);
                            }*/
                            break;
                        case BluetoothChatService.STATE_CONNECTED:
                            setSubtitle("已连接："+ currentDeviceName);
                            /*if (networkFragment != null) {
                                networkFragment.setViewEnabled(R.id.network_checkbox, true);
                                //networkFragment.setViewEnabled(R.id.network_button_start_server, true);
                                networkFragment.setViewEnabled(R.id.network_button_get_data, true);
                                networkFragment.setViewEnabled(R.id.network_button_edit, true);
                                if (networkFragment.getCheckBoxState())
                                    networkFragment.setViewEnabled(R.id.network_edit_server_port, true);
                            }*/
                    }
                    break;
                case BluetoothChatService.MESSAGE_TOAST:
                    String string = msg.getData().getString(BluetoothChatService.TOAST);
                    displayToast(string);
                    break;
                case Constants.DEVICE_CONNECTING:
                    setTabDisplay(0);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getpermissions();
        setContentView(R.layout.activity_main);
        //开启IOT数据接收监听服务
        //Intent startIntent=new Intent(this, IotMonitorService.class);
        //startService(startIntent);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//得到蓝牙适配器
        fragmentManager = getSupportFragmentManager();//获取FragmentManager，在活动中而已使用此方法获取
        initView();
        setTabDisplay(0);
        XCrash.init(this);
        if (WifiClientThread.wifiTestFlag) {
            Thread serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    WifiServer.startService();
                }
            });
            serverThread.start();
        }
        Constants.DATA_DIRECTORY = getExternalFilesDir(null).getAbsolutePath() + "/MineSSIP_R";
    }

    private void getpermissions()//运行时权限授权
    {
        String[] permission=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.BLUETOOTH_PRIVILEGED};
        for (int i=0;i<permission.length;i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission[i]) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 6.0) {
                    ActivityCompat.requestPermissions(MainActivity.this, permission, 12);
                }
            }
        }
    }

    private void initView(){
        logLayout = findViewById(R.id.log_layout);
        logImage = findViewById(R.id.log_image);//下面要对图片进行更改，所以声明
        //logTextView = (TextView) findViewById(R.id.log_text_view);
        logFragment = new LogFragment();

        controllerLayout = findViewById(R.id.controller_layout);
        controllerImage = findViewById(R.id.controller_image);

        dataLayout = findViewById(R.id.data_layout);
        dataImage = findViewById(R.id.data_image);

        logLayout.setOnClickListener(v-> setTabDisplay(0));
        controllerLayout.setOnClickListener(v-> setTabDisplay(1));
        dataLayout.setOnClickListener(v-> setTabDisplay(3));
    }

    protected void setTabDisplay(int index){
        clearSelection();
        FragmentTransaction transaction = fragmentManager.beginTransaction();//开启一个事务，通过beginTransaction（）开启
        switch (index){
            case 0:
                logImage.setImageResource(R.drawable.image_log_selected);
                if(logFragment == null)
                    logFragment = new LogFragment();
                transaction.replace(R.id.fragment_layout, logFragment);
                break;
            case 1:
                controllerImage.setImageResource(R.drawable.image_controller_selected);//更改碎片切换按钮颜色
                if(controllerFragment == null)
                    controllerFragment = new ControllerFragment();
                transaction.replace(R.id.fragment_layout, controllerFragment);//向容器内调价或替换碎片，一般使用replace（）方法实现，需要传入容器的id和待添加的碎片实例
                Log.e(TAG, "onClick:controllerfragment ");
                break;
            case 3:
                dataImage.setImageResource(R.drawable.image_data_selected);
                if(dataFragment == null)
                    dataFragment = new DataFragment();
                transaction.replace(R.id.fragment_layout, dataFragment);
                break;
            case 4:
                dataImage.setImageResource(R.drawable.image_data_selected);
                if (noneFileFragment == null)
                    noneFileFragment = new NoneFileFragment();
                Log.e(TAG, "setTabDisplay: 4 -------"+noneFileFragment.toString());
                transaction.replace(R.id.fragment_layout, noneFileFragment);
                break;
            default:
                break;
        }
        transaction.commit();//提交事务，调用commit（）方法来完成
    }

    private void clearSelection(){
        controllerImage.setImageResource(R.drawable.image_controller_unselected);
        logImage.setImageResource(R.drawable.image_log_unselected);
        dataImage.setImageResource(R.drawable.image_data_unselected);
    }

    public void setSubtitle(String subtitle)//显示蓝牙连接状态
    {
        ActionBar actionBar = MainActivity.this.getSupportActionBar();
        //Log.e(TAG, "setSubtitle: actionBar = "+actionBar);
        if(actionBar != null)
            actionBar.setSubtitle(subtitle);
    }

    @Override
    protected void onStart() //活动由不可见变为可见的时候调用
    {
        super.onStart();
        if(mChatService == null)
            mChatService = new BluetoothChatService(this, mHandler);
    }

    @Override
    protected void onDestroy() //活动在被销毁之前调用
    {
        super.onDestroy();
        if(mChatService != null)
            mChatService.stop();
        //networkFragment.stopServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) //创建Menu弹出式菜单
    {
        getMenuInflater().inflate(R.menu.main, menu);
        itemConnect = menu.findItem(R.id.menu_connect_item);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) //判断弹出式菜单，菜单的响应事件，为处理菜单被选中运行后的事件处理。
    {
        if(item.getTitle().toString().equals("连接")) {
            item.setEnabled(false);
            Intent intent = new Intent(this, ChooseDeviceActivity.class);
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
        }
        else {
            mChatService.stop();
            logAppend("->bluetooth disconnected!\n");
            item.setTitle("连接");
        }
        return true;
    }

    public void sendCommand(String cmd)//发送命令
    {
        if(mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
            mChatService.write(cmd.getBytes());//调用BluetoothChatService类中的write方法
        else
            displayToast("蓝牙未连接");//调用Toast提醒
    }

    public int getBluetoothState()//得到状态指示常量
    {
        return mChatService.getState();
    }

    public void displayToast(String string){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)


    public void logAppend(String string){
        logContent += string;
        if(logFragment == null)
            logFragment = new LogFragment();
        logTextView = findViewById(R.id.log_text_view);
        if(logTextView == null){
            Log.e(TAG, "logAppend: null");
        }else{
            if (logContent.length() > 1024){
                logContent = "";
                logTextView.setText("");
            }
            logTextView.append(string);
        }
        ScrollView scrollView = findViewById(R.id.log_scroll_view);
        Log.e(TAG, "scroll" + scrollView);
        if(fragmentState == 0)
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void writeLog(String message, String logTextPath){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String modfiedTime = formatter.format(date);
        File f = new File(logTextPath);
        if (f.length()> 5 * 1024 * 1024){
            f.delete();//删除上次保存的数据
        }
        f = new File(logTextPath);
        FileWriter fw = null;
        try {
            fw = new FileWriter(f, true);
            fw.write(modfiedTime);
            fw.write(Constants.LINE_SEPARATOR);
            fw.write(message);
            fw.write(Constants.LINE_SEPARATOR);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) //在被startActivityForResult（）方法启动的活动销毁后调用
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String deviceAddress = data.getExtras().getString(ChooseDeviceActivity.DEVICE_ADDRESS);
                    currentDeviceName = data.getExtras().getString(ChooseDeviceActivity.DEVICE_NAME);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                    mChatService.connect(device);
                    currentDeviceName = device.getName();
                }
                if (resultCode == Activity.RESULT_CANCELED)
                    itemConnect.setEnabled(true);
                break;
        }
    }

    @Override
    public void onClick(View v) {

    }
}

