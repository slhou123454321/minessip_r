package com.example.minessip_r;

public class ParamSaveClass {
    public static boolean getip=true;//用来判断是否获得当前连接热点的ip并避免重复显示无用信息
    public static boolean isGetCommendReceive=true;

    public static int wifiClientThreadState=0;//用来判断线程状态避免数据传输线程重复开启
    public static int getSPS=250;//获得采样率信息用来控制接收的数据文件大小
    public static int fragmentState=0;//用来判断当前页面是否为logframent，防止更新文件接收进度信息时程序崩溃
    public static int dotNumber=1000;
    public static int minTimes=2;
    public static int maxTimes=3;
    public static int offsetState=1;
    public static int gainState=1;
    public static int groupStart=0;


    public static String configCommand="config/250/1/1/1/1/1/1/FrequencyEle250HzData/FrequencyEle-Data\r\n";

    public static String sendConfigDir=Constants.DATA_DIRECTORY+"/sendConfig";//发送设置文件路径
    public static String receiveConfigDir_1=Constants.DATA_DIRECTORY+"/receiveConfig_1";//接收排列一文件路径
    public static String receiveConfigDir_2=Constants.DATA_DIRECTORY+"/receiveConfig_2";//接收排列二文件路径
    public static String sendConfigFileDir=Constants.DATA_DIRECTORY+"/sendConfig/def.txt";//发送设置文件路径
    public static String receiveConfigFileDir_1=Constants.DATA_DIRECTORY+"/receiveConfig_1/def.txt";//接收排列一文件路径
    public static String receiveConfigFileDir_2=Constants.DATA_DIRECTORY+"/receiveConfig_2/def.txt";//接收排列二文件路径
    public static String logText=Constants.DATA_DIRECTORY+"/logMessage.txt";
    public static String workSpaceName= "默认工区";//工区文件夹名
    public static String workSpacePath=Constants.DATA_DIRECTORY+"/默认工区";
    public static String resultDir="result";//计算结果文件夹名，放在工区文件夹里面
    public static String rawDataDir="rawData";//原始数据文件夹名，放在工区文件夹里面
    public static String rawFileName=null;
    public static String resultAllFileName=ParamSaveClass.workSpacePath+"/汇总数据.txt";
    public static String resultFileName=ParamSaveClass.workSpacePath+"/"+ParamSaveClass.resultDir+"/默认数据.txt";

    public static double limit=50;

    public static double[] max={0,0,0,0,0,0};
    public static double[] signalFrequency={1,2,4};
    public static double[] ele={1.0,1.0,1.0};
    public static double[] voltages={400.0,400.0,400.0};
    public static boolean[] checkBox=new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true};
    //k值前24个数据为发送一与接收点击计算产生，中间24个为发送二，最后24个为发送三。
    public static double[] k={1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
    public static String resistivity_group=null;
    public static String groupID=null;

    public static String butCheckText="开始自检";
    public static String butAquiText="开始采集";
    public static boolean configState=true;
    public static boolean carliState=true;
    public static boolean checkState=true;
    public static boolean aquisitionState=true;
    public static boolean chartState=true;
    public static boolean iotSaveState=true;
    public static boolean iotAquisitionState=true;
    public static boolean iotDeviceState=true;

}
