package com.example.minessip_r;

import android.os.Environment;

public class Constants {

    public static final String CMD_RESET="reset\r\n";
    public static final String CMD_STOP = "stop\r\n";
    public static final String CMD_SEND_DATA = "senddata\r\n";
    public static final String CMD_START = "start\r\n";
    public static final String CMD_CALIBRA = "calibra\r\n";

    public static final String CMD_STOP_SELFCHECK = "stopcheck\r\n";
    public static final String CMD_REQ_SD_CAPACITY = "viewSDcapacity\r\n";
    public static final String CMD_REQ_DELECT_SD = "sddelect\r\n";

    //wifi
    public static final int PORT = 4321;


    public static final int DEVICE_CONNECTING = 11;//有设备正在连接热点
    public static final int DEVICE_CONNECTED = 12;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE = 13;//发送消息成功
    public static final int SEND_MSG_ERROR = 14;//发送消息失败
    public static final int GET_MSG = 6;//获取新消息

    //消息
    public static final int SET_LOG_MESSAGE=21;
    public static final int GET_FILE_SIZE=22;
    public static final int GET_FILE_NAME=23;
    public static final int GET_CHART1_CHANGE=24;
    public static final int GET_CHART2_CHANGE=25;
    public static final int GET_TEXT_DATA=26;
    public static final int GET_SERVER_IP=27;
    public static final int GET_TEXTVIEW_CHANGE=28;
    public static final int FINISH_THREAD=29;//结束采集
    public static final int GET_TEMP_CHART2_CHANGE=30;
    public static final int STOP_AQUISITION=31;//停止采集
    public static final int CHANGE_TABLE=32;//修改表格信息

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static int fragmentState = 0;//用来判断当前页面是否为logframent，防止更新文件接收进度信息时程序崩溃

    public static final String DATA_DIRECTORY = Environment.getExternalStorageDirectory().getPath() +"/MineSSIP_R";


}
