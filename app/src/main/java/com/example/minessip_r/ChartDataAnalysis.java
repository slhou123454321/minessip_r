package com.example.minessip_r;

import android.util.Log;


import com.example.minessip_r.math.Complex;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class ChartDataAnalysis {

    private static final String TAG ="chartDataAnalysis" ;
    public static ArrayList<ArrayList<Float>> lists1 = null;//多电压曲线
    public static ArrayList<ArrayList<Float>> Curlists = new ArrayList<>();//多电流曲线
    public static ArrayList<Float> lists2 = null;//三点线
    public static ArrayList<ArrayList<Float>> ave=new ArrayList<>(2);//39电流振幅，相位
    public static ArrayList<Float> errorLists=null;//缓存误差数据
    public static ArrayList<Float> errorLists1=null;//缓存相位误差数据
    /**
     *
     * @param x 偏移值
     * @param y 幅值放大倍数
     * @return  返回正弦曲线数据的六个集合数组
     */
    public static ArrayList<ArrayList<Float>> getSinData(float x,float y,int dot){
        ArrayList<ArrayList<Float>> lists=new ArrayList<>(4);
        for(int i=0;i<4;i++){
            lists.add(new ArrayList<Float>());
        }
        for(double i=0;i<dot;i++){
            lists.get(0).add((float)(y*Math.sin(0.4*i)));
            lists.get(1).add((float)(y*Math.sin(0.4*i-10)+x));
            lists.get(2).add((float)(y*Math.sin(0.4*i-20)+2*x));
            lists.get(3).add((float)(y*Math.sin(0.4*i)+3*x));
//            lists.get(4).add((float)(y*Math.sin(0.4*i)+4*x));
//            lists.get(5).add((float)(y*Math.sin(0.4*i)));
        }
        return lists;
    }







    public static ArrayList<ArrayList<Float>> binaryToDecimal(String dataFilePathname) {
        if (dataFilePathname != null) {
            File file = new File(dataFilePathname);
            if (!file.exists() || file.length() < 1) {
                Log.e(TAG, "文件不存在或为空: " + dataFilePathname);
                return ChartDataAnalysis.getSinData(0.2f, 1.0f, 100);
            }

            long fileSize = file.length();
            Log.d(TAG, "========================================");
            Log.d(TAG, "文件路径: " + dataFilePathname);
            Log.d(TAG, "文件大小: " + fileSize + " 字节");
            Log.d(TAG, "========================================");

            ArrayList<ArrayList<Float>> lists = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                lists.add(new ArrayList<Float>());
            }

            RandomAccessFile readFile = null;
            byte[] buffer = new byte[5];
            int packetIndex = 0;

            try {
                readFile = new RandomAccessFile(file, "r");

                while (readFile.read(buffer) != -1) {
                    // 打印当前数据包的原始十六进制
                    String hexStr = String.format("%02X %02X %02X %02X %02X",
                            buffer[0], buffer[1], buffer[2], buffer[3], buffer[4]);

                    // 通道号
                    int channelNumber = buffer[0] & 0xFF;

                    // 4字节数据（大端序）
                    int data = ((buffer[1] & 0xFF) << 24)
                            | ((buffer[2] & 0xFF) << 16)
                            | ((buffer[3] & 0xFF) << 8)
                            | (buffer[4] & 0xFF);

                    // 无符号转换（用于打印）
                    long unsignedData = data & 0xFFFFFFFFL;

                    // 打印详细信息
                    Log.d(TAG, String.format("包[%d]: %s | 通道号=0x%02X (%d) | 数据=0x%08X (%d) | 无符号=%d",
                            packetIndex, hexStr, channelNumber, channelNumber, data, data, unsignedData));

                    // 验证通道号是否在有效范围内
                    if (channelNumber >= 0 && channelNumber < 4) {
                        float value = convertRawDataToValue(data);
                        lists.get(channelNumber).add(value);
                        Log.d(TAG, String.format("  → 通道%d 转换值: %.6f", channelNumber + 1, value));
                    } else {
                        Log.w(TAG, String.format("  → 无效通道号: %d (0x%02X)，跳过", channelNumber, channelNumber));
                    }

                    packetIndex++;
                }

                Log.d(TAG, "========================================");
                Log.d(TAG, "解析完成，共处理 " + packetIndex + " 个数据包");
                Log.d(TAG, "各通道数据量: ");
                for (int i = 0; i < 4; i++) {
                    Log.d(TAG, "  通道" + (i + 1) + ": " + lists.get(i).size() + " 个数据");
                }
                Log.d(TAG, "========================================");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "文件未找到: " + dataFilePathname);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "读取文件错误: " + e.getMessage());
            } finally {
                if (readFile != null) {
                    try {
                        readFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return lists;

        } else {
            Log.e(TAG, "文件路径为null");
            return ChartDataAnalysis.getSinData(0.2f, 1.0f, 100);
        }
    }
    /**
     *
     * @param dataFilePathname 文件名
     * @return           返回原始数据集合数组，解析二进制数据
     */
    public static ArrayList<ArrayList<Float>> binaryToDecimal1(String dataFilePathname) {
        if (dataFilePathname != null) {
            File file = new File(dataFilePathname);
            if (!file.exists() || file.length() < 1) {
                return ChartDataAnalysis.getSinData(0.2f, 1.0f, 100);
            }

            long fileSize = file.length();
            int packetCount = (int) fileSize / 5;  // 每个包5字节

            // 创建4个通道的列表（通道1-4）
            ArrayList<ArrayList<Float>> lists = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                lists.add(new ArrayList<Float>());
            }

            RandomAccessFile readFile = null;
            byte[] buffer = new byte[5];

            try {
                readFile = new RandomAccessFile(file, "r");
                int validCount = 0;

                while (readFile.read(buffer) != -1) {
                    // 第1字节：通道号
                    int channelNumber = buffer[0] & 0xFF;

                    // 后4字节：32位数据
                    int data = (buffer[1] & 0xFF) << 24
                            | (buffer[2] & 0xFF) << 16
                            | (buffer[3] & 0xFF) << 8
                            | (buffer[4] & 0xFF);

                    // 转换数据（与原协议相同）
                    float value = convertRawDataToValue(data);

                    // 通道号范围：00, 01, 02, 03 对应通道1-4
                    if (channelNumber >= 0 && channelNumber < lists.size()) {
                        lists.get(channelNumber).add(value);
                        validCount++;
                        Log.d(TAG, "通道" + (channelNumber + 1) + " 数据: " + value);
                    } else {
                        Log.w(TAG, "无效通道号: " + channelNumber);
                    }
                }

                Log.d(TAG, "有效数据点数: " + validCount);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (readFile != null) {
                    try {
                        readFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return lists;

        } else {
            return ChartDataAnalysis.getSinData(0.2f, 1.0f, 100);
        }
    }

    /**
     * 将32位原始数据转换为实际物理量（与原协议相同）
     *
     * @param rawData 32位原始数据（0 ~ 4294967295）
     * @return 转换后的电流值
     */
    private static float convertRawDataToValue(int rawData) {
        // 转换为电压（mV）
        float voltage = ((float) rawData) / 4294967296L * 5000.0f;
        // 霍尔传感器转换：电压(mV) → 电流
        //float current = voltage * 1000.0f / 62.5f;
        return voltage;
    }

    /**
     * 计算得到三个不同发送频率的电位值数据，主频处
     * @param dataLists         进行fft以及后续计算的数据
     * @param signalFrequency   信号频率
     * @param sampleRate        采样率
     * @param sampleDots        采样点数
     * @return                  三个不同频率的六通道计算得到的电位值数据
     */
    public static ArrayList<Complex> getSingleFFTarray(ArrayList<ArrayList<Float>> dataLists, double signalFrequency, int sampleRate, int sampleDots){
        ArrayList<Complex> res=new ArrayList<>();
        for (int i=0;i<dataLists.size();i++){
            ArrayList<Float> a=dataLists.get(i);
            double[] arr=new double[a.size()];
            for(int j=0;j<a.size();j++){
                arr[j]=a.get(j);
            }
            if(a.size()==0){
                res.add(new Complex(0,0));
                continue;
            }
            // Complex ui=executeSingleFFT(arr,a.size(),signalFrequency,sampleRate);
            Complex ui=executeFFT(arr,a.size(),signalFrequency,sampleRate);
            res.add(ui);
        }
        return res;
    }

    /**
     * 方法2：获取目标频率处的FFT结果
     * 调用executeSingleFFT方法，该方法返回指定频率处的复数
     */
    public static ArrayList<Complex> getTargetFrequencyFFT(ArrayList<ArrayList<Float>> dataLists,
                                                           double targetFrequency,
                                                           int sampleRate) {
        ArrayList<Complex> res = new ArrayList<>();
        for (int i = 0; i < dataLists.size(); i++) {
            ArrayList<Float> a = dataLists.get(i);
            double[] arr = new double[a.size()];
            for (int j = 0; j < a.size(); j++) {
                arr[j] = a.get(j);
            }
            if (a.size() == 0) {
                res.add(new Complex(0, 0));
                continue;
            }
            // 调用executeSingleFFT获取目标频率处的结果
            Complex ui = executeSingleFFT(arr, a.size(), targetFrequency, sampleRate);
            res.add(ui);
        }
        return res;
    }

    /**
     * 进行傅里叶变换，并获得傅里叶变换之后的复数值
     * @param firstChannelFFT  进行fft的数据
     * @param sampleDots       采样点数
     * @param signalFrequency  信号频率数组
     * @param sampleRate       采样率
     * @return                 相应信号频率下对应的正弦波的幅度值
     */
    public static Complex executeSingleFFT(double[] firstChannelFFT, int sampleDots, double signalFrequency, int sampleRate) {
        Log.e(TAG,"sampleDots"+sampleDots+"  "+firstChannelFFT.length);

        DoubleFFT_1D DoubleFFT_1D = new DoubleFFT_1D(sampleDots);
        DoubleFFT_1D.realForward(firstChannelFFT);
        //信号分辨率:采样率除以采样点数
        double signalResolution = (double) sampleRate / sampleDots;
        //用信号频率除以信号分辨率就可以得到角标，当信号频率存在小数的时候要四舍五入
        int count = (int) Math.round(signalFrequency/ signalResolution);
        double im = firstChannelFFT[count * 2 + 1];
        double re = firstChannelFFT[count * 2];
        // Complex Ui = new Complex(re,im);
        Complex res = new Complex(re,im);
        Complex temp = new Complex(firstChannelFFT.length/2,0);
        Complex Ui = res.div(temp);
        Log.d("GETrun", "实部虚部: "+"im:"+im+"re:"+re);
        Log.d("GETrun", "幅度值: "+(float) Math.sqrt(re * re + im * im)/(firstChannelFFT.length / 2));
        Log.d("GETrun", "相位: "+Math.atan2(im,re)/Math.PI*180);
        return Ui;
    }

    public static Complex executeFFT(double[] firstChannelFFT, int sampleDots, double signalFrequency, int sampleRate) {
        double signalResolution = (double) sampleRate / sampleDots;
        int count = (int) Math.round(signalFrequency/ signalResolution);
        int numFFT = 2*count;
        if(firstChannelFFT.length==0){
            Complex re = new Complex(0,0);
            return re;
        }
        DoubleFFT_1D DoubleFFT_1D = new DoubleFFT_1D(firstChannelFFT.length);
        DoubleFFT_1D.realForward(firstChannelFFT);
        double im,re ;
        double maxMagnitude = Double.MIN_VALUE;

            for(int i=0;i+1<firstChannelFFT.length;i=i+2){
                double d = Math.sqrt(firstChannelFFT[i] * firstChannelFFT[i] +
                        firstChannelFFT[i + 1] * firstChannelFFT[i + 1]);
                if (d > maxMagnitude) {
                    maxMagnitude = d;
                    numFFT = i;
                }
            }
        //FileRecordUtil.saveString("最大值索引:"+numFFT);
        im = firstChannelFFT[numFFT+1];
        re = firstChannelFFT[numFFT];

        Complex res = new Complex(re,im);
        Complex temp = new Complex(firstChannelFFT.length/2,0);
        Complex Ui = res.div(temp);

        Log.d("实部虚部", "GETrun: "+"im:"+im+"re:"+re);
        Log.d("幅度值", "GETrun: "+(float) Math.sqrt(re * re + im * im)/(firstChannelFFT.length / 2));
        Log.d("相位", "GETrun: "+Math.atan2(im,re)/Math.PI*180);
        return Ui;
    }


    /**
     *
     * @param per
     * @param cur
     * @return     返回两个图表数据的相对误差值
     */
    public static ArrayList<Float> getErrorLists(ArrayList<Float> per,ArrayList<Float> cur){
        ArrayList<Float> res=new ArrayList<Float>();
        for (int j=0;j<per.size();j++){
            float data=Math.abs(per.get(j)-cur.get(j))*200/(per.get(j)+cur.get(j));//相对误差百分比
            res.add((float)((Math.round(data*10))/10.0));
        }
        return res;
    }

    public static boolean isLimit(ArrayList<Float> list,Double limit){
        for(int i=1;i<list.size();i=i+2){
            if(list.get(i)>limit){
                return true;
            }
        }
        return false;
    }

}
