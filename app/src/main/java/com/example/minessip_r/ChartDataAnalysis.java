package com.example.minessip_r;

import android.util.Log;


import com.example.minessip_r.math.Complex;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
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


    /**
     *
     * @param dataFilePathname 文件名
     * @return           返回原始数据集合数组，解析二进制数据
     */
    public static ArrayList<ArrayList<Float>> binaryToDecimal(String dataFilePathname){
        if (dataFilePathname!=null){
            //todo
            File file = new File(dataFilePathname);
            if(!file.exists() || file.length()<1){
                return ChartDataAnalysis.getSinData(0.2f,1.0f,100);
            }
            long fileSize = file.length();//返回文件长度，字节为单位
            int DotNumber = (int) fileSize/5/6;//每个数据5字节，所以每道数据点数为式子所求。   250*32
            ArrayList<ArrayList<Float>> lists=new ArrayList<>();
            for(int i = 0;i < 3;i++){
                lists.add(new ArrayList<Float>());
            }
            RandomAccessFile readFile = null;
            byte[] buffer = new byte[300];
            int len = 0;
            Log.e(TAG, "fileSize =" + fileSize);
            Log.e(TAG, "DotNumber =" + DotNumber);
            try {
                readFile=new RandomAccessFile(file, "r");
                int sum=0;
                int count=DotNumber/10;
                while(count--!=0) {
                    len = readFile.read(buffer);
                    if (len < 300) {
                        break;
                    }
                    for(int i = 0; i < 10; i++) {
                        for (int j = 0; j < 6; j++) {
                            int begin = i * 30 + j * 5;
                            int mark = (buffer[begin] & 0xff);
                            int data = (buffer[begin + 1] & 0xff) << 24
                                    | (buffer[begin + 2] & 0xff) << 16
                                    | (buffer[begin + 3] & 0xff) << 8
                                    | buffer[begin + 4] & 0xff;
                            float data_f = ((float) data) / 4294967296L * 5000 ;//得到的是电位值，单位为mV；

                            // data_f = data_f / 62.5f;// /62.5直接转换为电流A

                            int a=mark;
                            Log.e(TAG, "runtext2 data_f=" + data_f + "  a =" +a);
                            Log.e(TAG, "runtext2 data_f=" + data_f + "  a-1 =" +(a-1));

                            //int a = mark>195?6:(mark%5==0?5:mark%5);
                            //Log.e(TAG, "runtext2  a=" + a + "  mark=" + mark);
                            // 霍尔传感器转换
                            data_f = data_f*1000/62.5f;
                            if(a-1<lists.size() && a-1>=0)lists.get(a-1).add(data_f);
                                /*if (mark >0 && mark <= 234) {
                                    temp.get(mark-1).add(data_f);
                                }*/
                        }
                    }
                    sum+=10;
                }
                //templist=new ArrayList<>(temp);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lists;
        }else{
            return ChartDataAnalysis.getSinData(0.2f,1.0f,100);
        }
    }

    /**
     * 计算得到三个不同发送频率的电位值数据
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

       /* Log.e("FFT","count:"+count);
        FFTlists.clear();
        for(int i=0;i * 2 + 1<firstChannelFFT.length;i++){
            double im1 = firstChannelFFT[i * 2 + 1];
            double re1 = firstChannelFFT[i * 2];
            Float max= (float)Math.sqrt(re1 * re1 + im1 * im1) / (firstChannelFFT.length / 2);
            FFTlists.add(max);
            SetFFTFile(max+"");
            Log.e("FFT","i:"+i+"   max:"+max);
        }*/

        Complex Ui = new Complex(re,im);
        System.out.println(signalFrequency+"Hz信号幅值为："+Math.sqrt(re * re + im * im) / (firstChannelFFT.length / 2));
        System.out.println();
        // Log.d("实部虚部", "GETrun: "+"im:"+im+"re:"+re);
        //Log.d("幅度值", "GETrun: "+(float) Math.sqrt(re * re + im * im)/(firstChannelFFT.length / 2));
        //Log.d("相位", "GETrun: "+Math.atan2(im,re)/Math.PI*180);
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
