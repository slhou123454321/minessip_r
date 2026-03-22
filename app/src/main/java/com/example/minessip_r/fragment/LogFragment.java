package com.example.minessip_r.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.minessip_r.ChartDataAnalysis;
import com.example.minessip_r.Constants;
import com.example.minessip_r.R;
import com.example.minessip_r.WifiClientThread;
import com.example.minessip_r.chart.ChartPlay;
import com.example.minessip_r.ui.MainActivity;
import com.github.mikephil.charting.charts.LineChart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;


public class LogFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private LineChart lineChart_1;
    private LineChart lineChart_2;
    private TextView logTextView;
    private Button butCarlibration;
    private Button butAquisition;
    private Button butSelfCheck;
    private Button butGroundResistance;
    private Button butOpenChart;
    private Button butDelectSD;

    private TextView fileSize;
    private TextView[] TV_I=new TextView[3];
    private TextView[] TV_IE=new TextView[3];
    private TextView[] TV_P=new TextView[3];
    private TextView[] TV_PE=new TextView[3];

    private boolean buttonUpdateConfigenableState = true;
    private WifiManager wifiManager;
    private String ip="";
    private WifiClientThread wifiClientThread;
    private static final String TAG = "LogFragment";
    private ArrayList<String> connectIP;


    private Handler handler=new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.FINISH_THREAD:
                    mainActivity.logAppend("->"+"采集结束！！！"+"\n");
                    butAquisition.setText(R.string.button_start_aquisition);
                    butAquisition.setText(R.string.button_start_aquisition);
                    mainActivity.controllerLayout.setEnabled(true);
                    mainActivity.dataLayout.setEnabled(true);
                    mainActivity.logLayout.setEnabled(true);
                    buttonUpdateConfigenableState = true;
                    butCarlibration.setEnabled(true);
                    butSelfCheck.setEnabled(true);
                    butGroundResistance.setEnabled(true);
                    lineChart_1.setTouchEnabled(true);
                    lineChart_2.setTouchEnabled(true);
                    break;
                case Constants.SET_LOG_MESSAGE:
                    mainActivity.logAppend("->"+msg.obj+"\n");
                    break;
                case Constants.GET_FILE_SIZE:
                    String s=(String)msg.obj;
                    fileSize.setText(s);
                    break;
                case Constants.CHANGE_TABLE:
                    Log.e(TAG, "List1="+ChartDataAnalysis.ave.get(0).size()+"  "+ChartDataAnalysis.ave.get(1).size());
                    Log.e(TAG, "List1="+TV_I.length);
                    Log.e(TAG,"ChartDataAnalysis.lists1"+ChartDataAnalysis.Curlists.size());
                    ChartPlay.showLineChart(lineChart_1,ChartDataAnalysis.Curlists.get(0),"电压1", Color.RED,0);
                    ChartPlay.addLine(lineChart_1,ChartDataAnalysis.Curlists.get(1),"电压2", Color.GREEN ,0);
                    ChartPlay.addLine(lineChart_1,ChartDataAnalysis.Curlists.get(2),"电压3", Color.BLUE  ,0);
                    ChartPlay.addLine(lineChart_1,ChartDataAnalysis.Curlists.get(3),"电压4", Color.YELLOW  ,0);
                    lineChart_1.invalidate();
                    ChartPlay.initChartView(lineChart_2,3,"","","电压(mV)/通道号");//初始化图表
                    ChartPlay.showLineChart1(lineChart_2,ChartDataAnalysis.ave.get(0),"电压曲线", Color.CYAN,0);
                    lineChart_2.invalidate();
//                    for(int i=0;i<3;i++){
//                        Log.e(TAG, "List1=TV_I[i]"+TV_I[i]);
//                        Log.e(TAG, "List1=hartDataAnalysis.ave.get(0).get(i)"+ChartDataAnalysis.ave.get(0).get(i));
//                        Log.e(TAG, "List1=TV_P[i]"+TV_P[i]);
//                        TV_I[i].setText(String.format("%.1f",ChartDataAnalysis.ave.get(0).get(i)));
//                        TV_P[i].setText(String.format("%.1f",ChartDataAnalysis.ave.get(1).get(i)));
//                        TV_IE[i].setText(String.format("%.1f",ChartDataAnalysis.errorLists.get(i)));
//                        TV_PE[i].setText(String.format("%.1f",ChartDataAnalysis.errorLists1.get(i)));
//                    }
                    WifiClientThread.chartFinsh1=true;
                    break;
                case Constants.CHANGE_CALIBRA:
                    mainActivity.logAppend("->"+"校准完成"+"\n");
                    mainActivity.controllerLayout.setEnabled(true);
                    mainActivity.dataLayout.setEnabled(true);
                    mainActivity.logLayout.setEnabled(true);
                    butAquisition.setEnabled(true);
                    butSelfCheck.setEnabled(true);
                    butGroundResistance.setEnabled(true);
                    butCarlibration.setEnabled(true);
                    buttonUpdateConfigenableState=true;
                    break;
                case Constants.GET_SERVER_IP:
                    if(MainActivity.getip)
                        mainActivity.logAppend("->"+"仪器连接失败，请检查是否连接了仪器热点"+"\n");
                    butAquisition.setEnabled(true);
                    butSelfCheck.setEnabled(true);
                    butGroundResistance.setEnabled(true);
                    butGroundResistance.setEnabled(true);
                    butCarlibration.setEnabled(true);
                    buttonUpdateConfigenableState=true;
                    break;
                default:
                    break;
            }
        }
    };


    public LogFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View logLayout = inflater.inflate(R.layout.fragment_log, container, false);
        return logLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity.fragmentState=0;
        mainActivity = (MainActivity) getActivity();
        Log.e(TAG, "开始onActivityCreated");
        wifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initView();
        initChart();
        connectIP=getConnectedIP();
        setHotsspotReceiver();//wifi广播

    }
    public void initView(){
        lineChart_1=mainActivity.findViewById(R.id.chart_1);
        lineChart_2=mainActivity.findViewById(R.id.chart_2);
        logTextView = (TextView) mainActivity.findViewById(R.id.log_text_view);
        wifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        butCarlibration = (Button) mainActivity.findViewById(R.id.controller_button_calibration);
        butSelfCheck = (Button) mainActivity.findViewById(R.id.controller_button_selfcheck);
        butGroundResistance = (Button) mainActivity.findViewById(R.id.controller_button_ground_resistance);
        butAquisition = (Button) mainActivity.findViewById(R.id.controller_button_aquisition);
        butOpenChart = (Button) mainActivity.findViewById(R.id.log_button_chart);
        butDelectSD = (Button) mainActivity.findViewById(R.id.button_delectsd);
        fileSize=mainActivity.findViewById(R.id.progress_text);

        butAquisition.setOnClickListener(this);
        butSelfCheck.setOnClickListener(this);
        butGroundResistance.setOnClickListener(this);
        butCarlibration.setOnClickListener(this);
        butOpenChart.setOnClickListener(this);
        butDelectSD.setOnClickListener(this);
    }
    public void initChart(){
        SharedPreferences pre = mainActivity.getSharedPreferences("butState", 0);//打开文件
        String dataFilePathname = pre.getString("DataFilePathName", null);//得到文件绝对路径与文件名
        SharedPreferences.Editor pre1 = ((MainActivity) getActivity()).getSharedPreferences("butState", 0).edit();
        pre1.putString("DataFilePathName", null);
        pre1.apply();
        if (dataFilePathname != null && (new File(dataFilePathname)).length() < 1024) {
            mainActivity.displayToast("文件没有数据");
            dataFilePathname = null;
        }else if (dataFilePathname!=null){
            //ChartDataAnalysis.lists2= ChartDataAnalysis.binaryToDecimal(dataFilePathname);//将数据解析处理后缓存
        }
//        ChartDataAnalysis.Curlists = ChartDataAnalysis.binaryToDecimal(dataFilePathname);
        if(ChartDataAnalysis.Curlists == null || ChartDataAnalysis.Curlists.size() == 0){
            ChartDataAnalysis.Curlists = ChartDataAnalysis.getSinData(0.2f,3f,100);
        }
        ChartPlay.initChartView(lineChart_1,5,"","","电压(mV)/点号(号)");//初始化图表
        ChartPlay.showLineChart(lineChart_1,ChartDataAnalysis.Curlists.get(0),"电压1", Color.RED,0);
        ChartPlay.addLine(lineChart_1,ChartDataAnalysis.Curlists.get(1),"电压2", Color.GREEN ,0);
        ChartPlay.addLine(lineChart_1,ChartDataAnalysis.Curlists.get(2),"电压3", Color.BLUE  ,0);       //ChartPlay.addLine(chartone,lists_1.get(3),"测道4", Color.TRANSPARENT ,10);
        ChartPlay.addLine(lineChart_1,ChartDataAnalysis.Curlists.get(3),"电压4", Color.YELLOW  ,0);       //ChartPlay.addLine(chartone,lists_1.get(3),"测道4", Color.TRANSPARENT ,10);

        ArrayList<Float> List2 = null;
        if(ChartDataAnalysis.lists2 == null || ChartDataAnalysis.lists2.size()==0){
            List2 = ChartDataAnalysis.getSinData(1,1,3).get(0);
            ChartPlay.initChartView(lineChart_2,3,"","","电压(mV)/通道号");//初始化图表
            ChartPlay.showLineChart1(lineChart_2,List2,"电压曲线", Color.CYAN,0);
        }else{
            List2= ChartDataAnalysis.lists2;
            Log.e(TAG, "lists_2:" + List2.size());
            ChartPlay.initChartView(lineChart_2,3,"","","电压(mV)/通道号");//初始化图表
            ChartPlay.showLineChart1(lineChart_2,List2,"电压曲线", Color.CYAN,0);
            /*ChartPlay.showLineChart(charttwo,PositiveList2,"电压衰减曲线", Color.CYAN,1);
            ChartPlay.addLine(charttwo,NegativeList2,"电压衰减曲线(负)", Color.LTGRAY,1);*/
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.controller_button_calibration) {
            butStartCarliClicked();
        }
        else if (id == R.id.controller_button_selfcheck) {
            if (butSelfCheck.getText().toString().equals(mainActivity.getString(R.string.button_start_selfcheck)))
                butStartSelfCheckClicked();
            else
                butStopSelfCheckClicked();
        }
        else if (id == R.id.controller_button_aquisition) {
            if (butAquisition.getText().toString()
                    .equals(mainActivity.getString(R.string.button_start_aquisition)))
                butStartAquisitionClicked();
            else
                butStopAquisitionClicked();
        }
        else if (id == R.id.log_button_chart) {
            mainActivity.logContent = "";
        }
        else if (id == R.id.controller_button_ground_resistance) {
            // 接地电阻测试
        } else if (id == R.id.button_delectsd) {
            mainActivity.sendCommand(Constants.CMD_REQ_DELECT_SD);
        }
    }

    //开始校准
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void butStartCarliClicked() {
        mainActivity.controllerLayout.setEnabled(false);
        mainActivity.logLayout.setEnabled(false);
        mainActivity.dataLayout.setEnabled(false);
        butAquisition.setEnabled(false);
        butSelfCheck.setEnabled(false);
        butCarlibration.setEnabled(false);
        butGroundResistance.setEnabled(false);
        buttonUpdateConfigenableState=false;

        String log = "->正在校准\n";
        mainActivity.logAppend(log);
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    mainActivity.sendCommand(Constants.CMD_CALIBRA);
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Message mes = new Message();
                    mes.what = Constants.CHANGE_CALIBRA;
                    handler.sendMessage(mes);
                }
            }).start();

    }

    //开始自检
    private void butStartSelfCheckClicked() {

    }

    //停止自检
    private void butStopSelfCheckClicked(){

    }

    //开始采集
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void butStartAquisitionClicked() {
        boolean test ;
        if (WifiClientThread.wifiTestFlag) {
            test = ip.equals("192.168.1.1");
        } else {
            test = ip.equals("192.168.4.1")&&mainActivity.itemConnect.getTitle().equals("断开");
        }
        // if(ip.equals("192.168.4.1")&&mainActivity.itemConnect.getTitle().equals("断开")) {
        // todo WiFi自测
        if(test) {
            wifiClientThread = new WifiClientThread(ip, handler,mainActivity);
            wifiClientThread.start();
            butAquisition.setText(R.string.button_stop_aquisition);
            buttonUpdateConfigenableState = false;
            butCarlibration.setEnabled(false);
            butSelfCheck.setEnabled(false);
            butGroundResistance.setEnabled(false);
            lineChart_1.setTouchEnabled(false);
            lineChart_2.setTouchEnabled(false);
            mainActivity.controllerLayout.setEnabled(false);
            mainActivity.dataLayout.setEnabled(false);
            mainActivity.logLayout.setEnabled(false);
        }else{
            mainActivity.logAppend("->"+"仪器连接失败，请检查是否连接了仪器热点或蓝牙"+"\n");
            butAquisition.setText("开始采集");
            mainActivity.controllerLayout.setEnabled(true);
            mainActivity.dataLayout.setEnabled(true);
            mainActivity.logLayout.setEnabled(true);
            buttonUpdateConfigenableState = true;
            butCarlibration.setEnabled(true);
            butSelfCheck.setEnabled(true);
            butGroundResistance.setEnabled(true);
            lineChart_1.setTouchEnabled(true);
            lineChart_2.setTouchEnabled(true);
        }
    }

    //停止采集
    private void butStopAquisitionClicked(){
        mainActivity.sendCommand(Constants.CMD_STOP);
        handler.obtainMessage(Constants.SET_LOG_MESSAGE,"发送停止命令："+Constants.CMD_STOP).sendToTarget();
        if (wifiClientThread!=null){
            wifiClientThread.stopThreadFlag=true;
        }//终止数据传输线程
        /*DataAnalysisLzt.amplitude.clear();
        DataAnalysisLzt.pharse.clear();*/
        Log.e(TAG, "wifiClientThread==null1111"+(wifiClientThread==null));
            butAquisition.setText(R.string.button_start_aquisition);
            buttonUpdateConfigenableState = true;
            butCarlibration.setEnabled(true);
            butSelfCheck.setEnabled(true);
            butGroundResistance.setEnabled(true);
            lineChart_1.setTouchEnabled(true);
            lineChart_2.setTouchEnabled(true);
            mainActivity.controllerLayout.setEnabled(true);
            mainActivity.dataLayout.setEnabled(true);
            mainActivity.logLayout.setEnabled(true);

    }

    @Override
    public void onStop(){
        super.onStop();

    }
    @Override
    public void onPause(){
        MainActivity.fragmentState=1;
        super.onPause();
        SharedPreferences.Editor pre = mainActivity.getSharedPreferences("butState", 0).edit();

        pre.putString("butCheckText", butSelfCheck.getText().toString());
        pre.putString("butGroundResistance", butGroundResistance.getText().toString());
        pre.putString("butAquiText", butAquisition.getText().toString());
        pre.putBoolean("configState", buttonUpdateConfigenableState);
        pre.putBoolean("carliState", butCarlibration.isEnabled());
        pre.putBoolean("checkState", butSelfCheck.isEnabled());
        pre.putBoolean("checkGroundResistanceState", butGroundResistance.isEnabled());
        pre.putBoolean("aquisitionState", butAquisition.isEnabled());
        //pre.putString("txtFileName",filenameview);
        pre.apply();
    }
    @Override
    public void onDetach() {//当碎片和活动解除关联的时候调用
        SharedPreferences.Editor pre = mainActivity.getSharedPreferences("butState", 0).edit();

        pre.putString("butCheckText", butSelfCheck.getText().toString());
        pre.putString("butGroundResistance", butGroundResistance.getText().toString());
        pre.putString("butAquiText", butAquisition.getText().toString());
        pre.putBoolean("configState", buttonUpdateConfigenableState);
        pre.putBoolean("carliState", butCarlibration.isEnabled());
        pre.putBoolean("checkState", butSelfCheck.isEnabled());
        pre.putBoolean("checkGroundResistanceState", butGroundResistance.isEnabled());
        pre.putBoolean("aquisitionState", butAquisition.isEnabled());
        pre.apply();

        super.onDetach();
    }

    @Override
    public void onResume() {//碎片重新激活时调用
        SharedPreferences pre = mainActivity.getSharedPreferences("butState", 0);
        String butCheckText = pre.getString("butCheckText", "仪器自检");
        String butAquiText = pre.getString("butAquiText", "开始采集");
        String butGroundResistanceText = pre.getString("butGroundResistance", "接地电阻测试");

        //filenameview=pre.getString("txtFileName",Constants.DATA_DIRECTORY+"/"+"wrong.txt");

        butSelfCheck.setText(butCheckText);
        butAquisition.setText(butAquiText);
        butGroundResistance.setText(butGroundResistanceText);
        //controllerFragment.butUpdateConfig.setEnabled(pre.getBoolean("configState", true));
        butCarlibration.setEnabled(pre.getBoolean("carliState", true));
        butSelfCheck.setEnabled(pre.getBoolean("checkState", true));
        butAquisition.setEnabled(pre.getBoolean("aquisitionState", true));
        butGroundResistance.setEnabled(pre.getBoolean("checkGroundResistanceState", true));

        //vbias=DataAnalysisLzt.vbias;

        if (!butAquisition.isEnabled()){
            butAquisition.setEnabled(true);
            butSelfCheck.setEnabled(true);
            butCarlibration.setEnabled(true);
            butGroundResistance.setEnabled(true);
            buttonUpdateConfigenableState=true;
        }
        super.onResume();
    }
    @Override
    public void onStart() {//碎片重新激活时调用
        super.onStart();
//        clientSocket=mainActivity.getClientSocket();
//        if ((clientSocket!=null)&&(wifiConnectThread==null)){
//            Log.e(TAG, "onStart: " );
//            wifiConnectThread= new WifiConnectThread(clientSocket,handler);
//            wifiConnectThread.start();
//        }
        logTextView.setText(mainActivity.logContent);
    }


    /**
     * 获取连接到热点上的手机ip
     *
     * @return
     */
    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }



    private void setHotsspotReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mainActivity.registerReceiver(new HotsspotReceiver(), intentFilter);
    }


    //广播监听WiFi
    class HotsspotReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {//便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
                int state = intent.getIntExtra("wifi_state", 0);
                if (state == 13) {
                    //Log.e(TAG, "onReceive: "+connectIP );
                    //mainActivity.logAppend("->"+"热点已开启"+"\n");
                    //new TCPConnectThread().start();
                }
                else if (state==11){
                    //mainActivity.logAppend("->"+"热点已关闭"+"\n");
                }
            }else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.e("BBB", "WifiManager.NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    mainActivity.logAppend("->"+"wifi连接断开"+"\n");
                    MainActivity.getip=true;
                    MainActivity.wifiClientThreadState=0;
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    Log.w("AAA","wifiInfo.getSSID():"+wifiInfo.getSSID()+"  WIFI_HOTSPOT_SSID:");
                    if (wifiInfo.getSSID()!=null&&MainActivity.wifiClientThreadState==0) {
                        //如果当前连接到的wifi是热点,则开启连接线程
                        if(MainActivity.getip)
                            mainActivity.logAppend("->"+"已连接到网络:" + wifiInfo.getSSID()+"\n");
                        DhcpInfo dhcpInfo=wifiManager.getDhcpInfo();
                        int i=dhcpInfo.serverAddress;
                        ip=(i&0xFF)+"."+((i>>8)&0xFF)+ "."+((i>>16)&0xFF)+ "."+((i>>24)&0xFF);
                        if(MainActivity.getip)
                            mainActivity.logAppend("->"+"获取到服务端IP："+ip+"\n");
                        MainActivity.getip=false;
                        Log.e(TAG, "onClick: socket.......555" +ip);
                        if(!(ip.equals("192.168.4.1"))){
                            mainActivity.logAppend("->"+"仪器连接失败，请检查是否连接了仪器热点"+"\n");
                        }
                    }
                }
                else {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == state.CONNECTING) {
                        Log.e(TAG, "onReceive: 连接中");
                    } else if (state == state.AUTHENTICATING) {
                        Log.e(TAG, "onReceive: 正在验证身份信息..." );
                    } else if (state == state.OBTAINING_IPADDR) {
                        Log.e(TAG, "onReceive: 正在获取IP地址..." );
                    } else if (state == state.FAILED) {
                        Log.e(TAG, "onReceive: 连接失败" );
                    }
                }
            }
        }
    }
}
