package com.example.minessip_r;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.minessip_r.fragment.ControllerFragment;
import com.example.minessip_r.math.Complex;
import com.example.minessip_r.service.BluetoothChatService;
import com.example.minessip_r.ui.MainActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;

public class WifiClientThread extends Thread{

    private static final String TAG ="WifiClientThread" ;
    private Socket socket=null;
    private String ip;
    private Handler handler;
    private InputStream inputStream;
    private MainActivity mainActivity;
    private FileInputStream fis;

    public static boolean chartFinsh1=true;//图表更新完成
    public  boolean stopThreadFlag=false;
    private  boolean checkout=false;//数据校验位出错
    private int times=1;
    public  ArrayList<ArrayList<Float>> Lists=null;//缓存上一次的电阻率数据
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static boolean wifiTestFlag = false;

    public WifiClientThread(String ip, Handler handler, MainActivity mainActivity) {
        Log.e("AAA","ClientThread开启");
        this.ip = ip;
        this.handler = handler;
        this.mainActivity=mainActivity;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        while (!stopThreadFlag) {
            while(!chartFinsh1){
                Log.e(TAG, "!chartFinsh1");
                try {
                    sleep(100);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (wifiTestFlag) {
                    // todo WiFi自测
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    Log.d(TAG, "服务端IP地址: " + inetAddress.getHostAddress());
                    socket = new Socket("127.0.0.1", 4321);
                } else {
                    socket = new Socket(ip, Constants.PORT);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (socket == null) {
                handler.obtainMessage(Constants.SET_LOG_MESSAGE, "WiFi连接失败\r\n").sendToTarget();
                break;
            } else {
                handler.obtainMessage(Constants.SET_LOG_MESSAGE, "仪器连接成功\r\n").sendToTarget();
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date date = new Date(System.currentTimeMillis());
            String modfiedTime = formatter.format(date);
            String filePath = ParamSaveClass.workSpacePath ;
            if (filePath == null || filePath.isEmpty()) {
                // 使用默认路径
                filePath = Environment.getExternalStorageDirectory().getPath()
                        + "/MineSSIP_R/默认工区";
                Log.w(TAG, "workSpacePath 为空，使用默认路径: " + filePath);
            }

            File file = new File(filePath);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    String errorMsg = "无法创建目录: " + filePath;
                    Log.e(TAG, errorMsg);
                    handler.obtainMessage(Constants.SET_LOG_MESSAGE, errorMsg + "\r\n").sendToTarget();
                    // 尝试使用备用目录
                    filePath = Environment.getExternalStorageDirectory().getPath() + "/MineSSIP_R";
                    file = new File(filePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
            }

            String filename = filePath+ "/" + modfiedTime+ ".dat";
            File saveDataFile = new File(filename);
            BufferedOutputStream bs = null;
            int totalLength;
            if (wifiTestFlag) {
                // todo WiFi自测
                totalLength = 5 * 3 * 150;//需要采集的总数据，6道数据，每个数据5个字节
            } else {
                totalLength=5 * ControllerFragment.dotNumber *4;//需要采集的总数据，4道数据，每个数据5个字节
            }
            if(ChartDataAnalysis.ave.size()==0){
                ChartDataAnalysis.ave.add(new ArrayList<Float>());
                ChartDataAnalysis.ave.add(new ArrayList<Float>());
            }else{
                ChartDataAnalysis.ave.get(0).clear();
                ChartDataAnalysis.ave.get(1).clear();
            }
            if (ChartDataAnalysis.lists1 != null && ChartDataAnalysis.lists1.size() != 0){
                ChartDataAnalysis.lists1.clear();
            }
            Log.e("1234",ChartDataAnalysis.Curlists + "");

            if (ChartDataAnalysis.Curlists.size() != 0){
                ChartDataAnalysis.Curlists.clear();
            }
            try {
                socket.setSoTimeout(8000);
                handler.obtainMessage(Constants.SET_LOG_MESSAGE,"发送开始命令：start\r\n").sendToTarget();//嵌入式解析，6/6-1=0
                sendToBle("start\r\n");
                inputStream = socket.getInputStream();
                // todo WiFi自测
                if (wifiTestFlag) {
                    OutputStream outputStream = socket.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    writer.write("trigger\n");
                    writer.flush();
                }

                bs = new BufferedOutputStream(new FileOutputStream(saveDataFile));
                if(bs==null) Log.e(TAG, "run:bs==null ");
                byte[] buffer = new byte[1020];
                int total = 0;//总的数据量
                int singleLength;
                int progress=1024;//进度显示阈值
                while ((singleLength = inputStream.read(buffer)) != -1) {
                    if(stopThreadFlag){
                        break;
                    }
                    if (total+singleLength<totalLength) {
                        bs.write(buffer, 0, singleLength);
                    } else if ((total<totalLength)&&(total+singleLength>=totalLength)){
                        bs.write(buffer, 0, totalLength-total);
                    }
                    if(!checkout) {
                        for (int j = 0; (j<singleLength)&&((total+j)<totalLength); j++) {
                            if ((total+j+1)%5==1) {
                                int seq = buffer[j] & 0xff;
                                //Log.e(TAG, "runtext1:校验位 " + seq);
                                // todo WiFi自测
                                if (((seq <= 0) || (seq > 6))&& false){
                                    checkout = true;
                                    sendToBle(Constants.CMD_STOP);
                                    sendToBle(Constants.CMD_RESET);
                                    handler.obtainMessage(Constants.SET_LOG_MESSAGE, seq + "数据出错,重新采集...\r\n").sendToTarget();
                                    try {
                                        sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    bs.flush();
                    total+=singleLength;
                    if(total>progress){
                        String s = progress/1024+"kb";
                        progress += 1024;
                        handler.obtainMessage(Constants.GET_FILE_SIZE,s).sendToTarget();
                    }
                    Log.e(TAG, "run: " + total +" "+total+"  " + singleLength+"  "+saveDataFile.length());
                    if ((total >= totalLength)&&((total-singleLength)<=totalLength)){
                        Log.e(TAG, "接收数据足够，break!");
                        sendToBle(Constants.CMD_STOP);
                                /*try {
                                    sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }*/
                        //socket.close();
                        //handler.obtainMessage(Constants.SET_LOG_MESSAGE,"发送停止命令："+Constants.CMD_STOP).sendToTarget();
                        //break;
                        // todo WiFi自测
                        if (wifiTestFlag) {
                            break;
                        }
                    }
                }
            }catch (SocketTimeoutException e) {
                handler.obtainMessage(Constants.SET_LOG_MESSAGE, "SocketTimeoutException"+e.getMessage()+"\r\n").sendToTarget();
                SetResetFile("错误：SocketTimeoutException   ");
                e.printStackTrace();
                Log.e(TAG, "run: " + "SocketTimeoutException");
            } catch (IOException e) {
                handler.obtainMessage(Constants.SET_LOG_MESSAGE, "IOException+"+e.getMessage()+"\r\n").sendToTarget();
                Log.e(TAG, "run: " + "IOException e"+e.getMessage());
                e.printStackTrace();
            } finally {
                Log.e(TAG, "finally" );
                //recheck=true;
                if (bs != null) {
                    try {
                        bs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (socket != null || inputStream != null) {
                    try {
                        inputStream.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(checkout){
                checkout=false;
            }else{
                if (saveDataFile.length()<totalLength) {//没有接受到需要的数据量下面的数据就不再上传时，现在会出现这种情况处理方式与校验位出错相同，所以直接将校验位标志位设置为true
                    checkout = true;
                    sendToBle(Constants.CMD_STOP);
                    handler.obtainMessage(Constants.SET_LOG_MESSAGE,"数据接收不足,重新开始采集").sendToTarget();
                }else{
                    ChartDataAnalysis.Curlists = ChartDataAnalysis.binaryToDecimal(filename);//电压
                    ArrayList<Complex> temp=ChartDataAnalysis.getSingleFFTarray(ChartDataAnalysis.Curlists,ControllerFragment.signalFrequency,ControllerFragment.getSPS,ControllerFragment.dotNumber);
                    for(int i=0;i<temp.size();i++){
                        // handler.obtainMessage(Constants.SET_LOG_MESSAGE,"计算前："+temp.get(i).toString()+"\r\n").sendToTarget();
                        Complex avetemp = temp.get(i).div(new Complex(1,0));//用最后一组电压值计算电流
                       //  handler.obtainMessage(Constants.SET_LOG_MESSAGE,"计算后："+avetemp.toString()+"\r\n").sendToTarget();
                        double result_Im = avetemp.getImage();
                        double result_Re = avetemp.getReal();//
                        double pharse_r = Math.atan2(result_Im,result_Re) * 1000;
                        double amplitude_r = Math.sqrt(result_Re * result_Re + result_Im * result_Im);
                        ChartDataAnalysis.ave.get(0).add((float)amplitude_r);//振幅
                        ChartDataAnalysis.ave.get(1).add((float)pharse_r);//相位
                    }
                    saveDataFile(modfiedTime);
                    handler.obtainMessage(Constants.SET_LOG_MESSAGE,"ave.size"+ ChartDataAnalysis.ave.get(0).size()).sendToTarget();
                    handler.obtainMessage(Constants.SET_LOG_MESSAGE,"Curlists.size"+ ChartDataAnalysis.Curlists.get(0).size()).sendToTarget();
                    Log.e(TAG, "ChartDataAnalysis.ave.get(0) " + ChartDataAnalysis.ave.get(0));
                    Log.e(TAG, "ChartDataAnalysis.ave.get(1) " + ChartDataAnalysis.ave.get(1));
                    chartFinsh1 = false;
                    Lists=ChartDataAnalysis.ave;
                    if(Lists!=null){
                        ChartDataAnalysis.errorLists = ChartDataAnalysis.getErrorLists(Lists.get(0),ChartDataAnalysis.ave.get(0));
                        ChartDataAnalysis.errorLists1 = ChartDataAnalysis.getErrorLists(Lists.get(1),ChartDataAnalysis.ave.get(1));
                    }

                    handler.obtainMessage(Constants.CHANGE_TABLE).sendToTarget();
                    handler.obtainMessage(Constants.SET_LOG_MESSAGE,"第"+times+"次采集完成\r\n").sendToTarget();
                    times++;
                }
            }
        }
    }
    //新建文件记录复位情况
    private void SetResetFile(String s){
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date=new Date(System.currentTimeMillis());
        String modfiedTime = formatter.format(date);
        File f = new File(Environment.getExternalStorageDirectory().getPath() +"/SocketTimeoutNumber.txt");
        try {
            OutputStreamWriter writer=new OutputStreamWriter(new FileOutputStream(f, true));
            writer.write(LINE_SEPARATOR);
            writer.append(modfiedTime+":"+"   "+s);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //发送指令
    private void  sendToBle(String command){
        // todo WiFi自测
        if (!wifiTestFlag) {
            if (mainActivity.getBluetoothState() != BluetoothChatService.STATE_CONNECTED) {
                //stopThreadFlag=true;
                int reConnectCnt = 1;
                handler.obtainMessage(Constants.SET_LOG_MESSAGE, "命令发送失败，蓝牙连接断开，重连中...\r\n").sendToTarget();
                while (true){
                    mainActivity.reConnect();
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mainActivity.getBluetoothState() != BluetoothChatService.STATE_CONNECTED){
                        handler.obtainMessage(Constants.SET_LOG_MESSAGE, "第" + reConnectCnt + "次重连失败！\r\n").sendToTarget();
                        reConnectCnt++;
                    }
                    if(reConnectCnt >= 4){
                        stopThreadFlag=true;
                        handler.obtainMessage(Constants.SET_LOG_MESSAGE, "重连次数已达到最大次数，请尝试重启程序和设备.\r\n").sendToTarget();
                        break;
                    }
                }
            } else {
                mainActivity.sendCommand(command);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void saveDataFile(String modfiedTime) {
        String filePathR = ParamSaveClass.workSpacePath;
        File file1 = new File(filePathR);
        if (!file1.exists()) {
            Log.d(TAG, "saveDataFile ：file1.mkdirs()" + file1.getAbsolutePath());
            file1.mkdirs();
        }
        FileWriter resfw = null;
        try {
            resfw = new FileWriter(file1.getPath() + "/"+modfiedTime+".txt", true);
            resfw.write(modfiedTime);
            resfw.write("电流：");
            resfw.write(Constants.LINE_SEPARATOR);
            for (int i = 0; ChartDataAnalysis.ave.size()>0 && i<ChartDataAnalysis.ave.get(0).size();i++) {
                resfw.write(ChartDataAnalysis.ave.get(0).get(i)+"");
                resfw.write(Constants.LINE_SEPARATOR);
            }
            resfw.write("电流相位：");
            resfw.write(Constants.LINE_SEPARATOR);
            for (int i = 0; ChartDataAnalysis.ave.size()>1 && i<ChartDataAnalysis.ave.get(1).size();i++) {
                resfw.write(ChartDataAnalysis.ave.get(1).get(i)+"");
                resfw.write(Constants.LINE_SEPARATOR);
            }
            resfw.close();
        } catch (IOException e) {
            //IOException -如果文件存在但是是一个目录而不是常规文件，则不存在但不能创建，或由于任何其他原因无法打开
            e.printStackTrace();
        }
    }
}
