package com.example.minessip_r.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.minessip_r.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChooseDeviceActivity extends AppCompatActivity {

    private static final String TAG = "ChooseDeviceActivity";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DEVICE_NAME = "device_name";
    private static final int REQUEST_ENABLE_BT =1001;
    private BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> pairedDeviceArrayAdapter;
    ArrayAdapter<String> scanDeviceArrayAdapter;
    List<BluetoothDevice> deviceList = new ArrayList<>();
    MyBtReceiver btReceiver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);
        setResult(RESULT_CANCELED);//防止用户中途退出 先将结果设置为取消
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        MainActivity.fragmentState=2;

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "--------------- 不支持蓝牙");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOnBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnBtIntent, REQUEST_ENABLE_BT);
        }
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置为0一直开启
            i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);//
            startActivity(i);
        }
        intentFilter = new IntentFilter();
        btReceiver = new MyBtReceiver();
        //监听 搜索开始，搜索结束，发现新设备 3条广播
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始搜索
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索完成
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//找到设备
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        this.registerReceiver(btReceiver, intentFilter);

        Button butScan = (Button) findViewById(R.id.button_scan);
        butScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();
            }
        });

        pairedDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        final ListView pairedListView = (ListView) findViewById(R.id.paired_list_view);
        pairedListView.setAdapter(pairedDeviceArrayAdapter);
        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String deviceInfo = ((TextView) view).getText().toString();
                String deviceAddress = deviceInfo.substring(deviceInfo.length()-17);

                // Log.e(TAG, "onItemClick: Address:"+deviceAddress+" position: "+i);

                Intent intent = new Intent();
                intent.putExtra(DEVICE_ADDRESS, deviceAddress);
                intent.putExtra(DEVICE_NAME, deviceInfo.substring(0, deviceInfo.length()-17));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        showBondDevice();//显示已绑定设备


        scanDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        final ListView scanListView = (ListView) findViewById(R.id.list_view_scan);
        scanListView.setAdapter(scanDeviceArrayAdapter);
        scanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                try {
                    Method method=BluetoothDevice.class.getMethod("createBond");
                    method.invoke(deviceList.get(i));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    /**
     * 广播接收器
     */
    private class MyBtReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                toast("开始搜索 ...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                toast("搜索结束");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (isNewDevice(device)) {
                    deviceList.add(device);
                    addnewdevice(device);
                    Log.e(TAG, "---------------- " + device.getName());
                }
            }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        Log.d(TAG, "取消配对");

                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "配对中");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(TAG, "配对成功");
                        showBondDevice();
                        pairedDeviceArrayAdapter.notifyDataSetChanged();
                        break;
                }
            }

        }
    }
    private void showBondDevice(){
        pairedDeviceArrayAdapter.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
            for(BluetoothDevice device : pairedDevices){
                pairedDeviceArrayAdapter.add(device.getName()+"\n"+device.getAddress());}
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                //用户打开蓝牙
                if (resultCode == Activity.RESULT_OK) {
                    //显示已绑定蓝牙设备
                    showBondDevice();
                }
                break;
            }

        }
    }
    private void addnewdevice(BluetoothDevice device){
        scanDeviceArrayAdapter.add(device.getName()+"\n"+device.getAddress());
    }

    /**
     * 判断搜索的设备是新蓝牙设备，且不重复
     * @param device
     * @return
     */
    private boolean isNewDevice(BluetoothDevice device){
        boolean repeatFlag = false;
        for (BluetoothDevice d :
                deviceList) {
            if (d.getAddress().equals(device.getAddress())){
                repeatFlag=true;
            }
        }
        //不是已绑定状态，且列表中不重复
        return device.getBondState() != BluetoothDevice.BOND_BONDED && !repeatFlag;
    }
    public void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

}

