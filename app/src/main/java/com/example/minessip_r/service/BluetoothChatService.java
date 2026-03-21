package com.example.minessip_r.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothChatService {
    private static final String TAG = "BluetoothChatService";
    //状态指示常量
    public static final int STATE_NOT_CONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    //消息类型常量
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_TOAST = 3;
    public static final int MESSAGE_BT_READ = 4;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static  final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private BluetoothAdapter mAdapter;
    private Handler mHandler;
    private int mState;
    private int mNewState;

    public BluetoothChatService(Context context, Handler handler){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mState = STATE_NOT_CONNECTED;//状态指示常量
    }

    public synchronized int getState()//返回状态指示常量，当前为几分别代表几种状态
    {
        return mState;
    }
    public synchronized void updateSubTitle() {
        mState = getState();
        Log.d(TAG, "updateSubTitle: " + mNewState + "-->" + mState);
        mNewState = mState;

        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }
    public synchronized void stop(){
        //先关闭所有线程
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mState = STATE_NOT_CONNECTED;
        updateSubTitle();
    }
    public synchronized void connect(BluetoothDevice device){
        if(mState == STATE_CONNECTING && mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        updateSubTitle();
    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        updateSubTitle();
    }

    /**
     * 连接蓝牙设备的线程
     */

    private class ConnectThread extends Thread{
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        public ConnectThread(BluetoothDevice device){
            mDevice = device;
            BluetoothSocket tmp = null;

            Log.e(TAG, "ConnectThread: MY_UUID: "+MY_UUID);
            try{
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (Exception e){
                Log.e(TAG, "ConnectThread: socket create failed!", e);
            }
            mSocket = tmp;
            mState = STATE_CONNECTING;//状态常量改为连接中

        }

        public void run(){
            Log.e(TAG, "ConnectThread begin...");
            try {
                mSocket.connect();
            }
            catch(IOException e){
                Log.e(TAG, "run: socket connect failed!", e);
                try{
                    mSocket.close();
                }
                catch (IOException ex){
                    Log.e(TAG, "run: socket close failed!", ex);
                }
                connectionFailed(mDevice.getName());
                return;
            }
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;//mConnect线程任务完成 清空
            }

            connected(mSocket, mDevice);//连接完成 开启connected线程管理连接
        }
        public void cancel() {
            try{
                mSocket.close();
            }
            catch (IOException e){
                Log.e(TAG, "run: socket close failed!", e);
            }
        }
    }

    /**
     * 已连接的相关处理线程
     */

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;
        public ConnectedThread(BluetoothSocket socket){
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            }
            catch(IOException e){
                Log.e(TAG, "ConnectedThread: get stream failed!", e);
            }
            mInStream = tmpIn;
            mOutStream = tmpOut;
            mState = STATE_CONNECTED;//状态常量改为已连接
        }

        /*public void run(){
            Log.e(TAG, "ConnectedThread begin...");
            byte[] buffer = new byte[1024*2];
            int count;//单次接收字符数
            int sum = 0;//字符总数
            while (mState == STATE_CONNECTED){
                try{
                    count = mInStream.read(buffer, sum, 100);
                    sum += count;
                    Log.e(TAG, "run: count: "+count);
                    if(sum<1024&&(buffer[sum-1] != '\n'))
                        continue;
                    mHandler.obtainMessage(MESSAGE_BT_READ, sum, -1, buffer).sendToTarget();
                    Log.e(TAG, "run: sum: "+sum);
                    sum = 0;
                }
                catch (IOException e){
                    Log.e(TAG, "ConnectedThread: InStream read failed!", e);
                    connectionLost();
                }
            }
        }*/
        public void run(){
            Log.e(TAG, "ConnectedThread begin...");
            Log.e(TAG, "ConnectedThread begin...");
            byte[] buffer = new byte[1024*2];
            int count;//单次接收字符数
            int sum = 0;//字符总数
            String readMessage;
            while (mState == STATE_CONNECTED){
                Log.e(TAG, "mState == STATE_CONNECTED"+(mState == STATE_CONNECTED));
                try{
                    //Log.e(TAG, "try----------------");
                    count = mInStream.read(buffer, sum, 100);
                    sum += count;
                    //Log.e(TAG, "run: count: "+count);
                    if(sum<1024&&(buffer[sum-1] != '\n'))
                        continue;
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    readMessage = new String(buffer,0,sum,"GB2312");
                    data.putString("BTdata",readMessage);
                    msg.what =MESSAGE_BT_READ;
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                    sum = 0;
                    sum = 0;
                }
                catch (IOException e){
                    Log.e(TAG, "ConnectedThread: InStream read failed!", e);
                    connectionLost();
                }
            }
        }

        public void write(byte[] buffer){
            try {
                mOutStream.write(buffer);
                Log.e(TAG, "ConnectedThread: write buffer ok!");
            }
            catch (IOException e){
                Log.e(TAG, "ConnectedThread: write buffer failed!", e);
            }
        }
        public void write(byte b){
            try{
                Log.e(TAG, "write: b "+b);
                mOutStream.write(b);
            }
            catch (IOException e){
                Log.e(TAG, "ConnectedThread: write single byte failed!");
            }
        }
        public void cancel() {
            try{
                mSocket.close();
            }
            catch (IOException e){
                Log.e(TAG, "cancel: socket close failed!", e);
            }
        }
    }

    public void write(byte[] send){
        ConnectedThread connectedThread;
        synchronized (BluetoothChatService.this){
            if(mState != STATE_CONNECTED)
                return;
            connectedThread = mConnectedThread;
        }
        connectedThread.write(send);//调用上面的connectesThread类中的write方法
    }
    private void connectionLost() {
        displayToast("连接丢失");
        mState = STATE_NOT_CONNECTED;
        updateSubTitle();
        this.stop();
    }

    private void connectionFailed(String deviceName) {
        displayToast("无法连接到 "+deviceName);
        mState = STATE_NOT_CONNECTED;
        updateSubTitle();
        this.stop();
    }
    private void displayToast(String string){
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, string);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
}

