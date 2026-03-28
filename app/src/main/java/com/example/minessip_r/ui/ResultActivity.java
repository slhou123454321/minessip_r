package com.example.minessip_r.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.minessip_r.ChartDataAnalysis;
import com.example.minessip_r.R;
import com.example.minessip_r.chart.ChartPlay;
import com.example.minessip_r.fragment.ControllerFragment;
import com.example.minessip_r.math.Complex;
import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "ResultActivity";

    // 图表视图
    private LineChart chartRes;

    // 表格视图
    private TextView ch1, che1, ch_p1, ch_pe1;
    private TextView ch2, che2, ch_p2, ch_pe2;
    private TextView ch3, che3, ch_p3, ch_pe3;
    private TextView ch4, che4, ch_p4, ch_pe4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 初始化视图
        initViews();

        // 获取传递的文件路径
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DAT_FILE_PATH")) {
            String filePath = intent.getStringExtra("DAT_FILE_PATH");
            Log.d(TAG, "接收到的.dat文件路径: " + filePath);

            // 处理.dat文件
            processDatFile(filePath);
        } else {
            Toast.makeText(this, "未接收到数据文件路径", Toast.LENGTH_SHORT).show();
            finish(); // 没有文件路径则关闭活动
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 调用返回方法
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 重写返回方法
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // 结束当前 Activity
    }

    // 初始化视图
    private void initViews() {
        // 图表视图
        chartRes = findViewById(R.id.chart_res);

        // 表格视图
        ch1 = findViewById(R.id.ch1);
        che1 = findViewById(R.id.che1);
        ch_p1 = findViewById(R.id.ch_p1);
        ch_pe1 = findViewById(R.id.ch_pe1);

        ch2 = findViewById(R.id.ch2);
        che2 = findViewById(R.id.che2);
        ch_p2 = findViewById(R.id.ch_p2);
        ch_pe2 = findViewById(R.id.ch_pe2);

        ch3 = findViewById(R.id.ch3);
        che3 = findViewById(R.id.che3);
        ch_p3 = findViewById(R.id.ch_p3);
        ch_pe3 = findViewById(R.id.ch_pe3);

        ch4 = findViewById(R.id.ch4);
        che4 = findViewById(R.id.che4);
        ch_p4 = findViewById(R.id.ch_p4);
        ch_pe4 = findViewById(R.id.ch_pe4);
        // 配置图表
        configureChart();
    }

    // 配置图表
    private void configureChart() {
        // ChartPlay.initChartView(chartRes,5,"","","电流(mA)/点号(号)");//初始化图表
    }

    // 处理.dat文件
    private void processDatFile(String filePath) {
        File datFile = new File(filePath);

        if (datFile.exists() && datFile.isFile()) {
            ArrayList<ArrayList<Float>> ave = new ArrayList<>();
            for(int i=0;i<2;i++) {
                ave.add(new ArrayList<>());
            }
            ArrayList<ArrayList<Float>> lists1 = ChartDataAnalysis.binaryToDecimal(filePath);//电压
            ArrayList<Complex> temp=ChartDataAnalysis.getSingleFFTarray(lists1, ControllerFragment.signalFrequency,ControllerFragment.getSPS,ControllerFragment.dotNumber);
            for(int i=0;i<temp.size();i++){
                Complex avetemp = temp.get(i).div(new Complex(1,0));//用最后一组电压值计算电流
                double result_Im = avetemp.getImage();
                double result_Re = avetemp.getReal();//
                double pharse_r = Math.atan2(result_Im,result_Re) * 1000;
                double amplitude_r = Math.sqrt(result_Re * result_Re + result_Im * result_Im);
                ave.get(0).add((float)amplitude_r);//振幅
                ave.get(1).add((float)pharse_r);//相位
            }
            ChartPlay.showLineChart(chartRes, lists1.get(0),"电压1", Color.RED,0);
            ChartPlay.addLine(chartRes, lists1.get(1),"电压2", Color.GREEN ,0);
            ChartPlay.addLine(chartRes, lists1.get(2),"电压3", Color.BLUE  ,0);
            ChartPlay.addLine(chartRes,lists1.get(3),"电压4", Color.parseColor("#FF6200EE") ,10);
            fillTableData(ave);
        } else {
            Toast.makeText(this, "数据文件不存在: " + filePath, Toast.LENGTH_SHORT).show();
        }
    }

    // 填充表格数据
    private void fillTableData(ArrayList<ArrayList<Float>> ave) {
        // I1数据
        ch1.setText(String.format("%.4f", ave.get(0).get(0)));
        che1.setText("-");
        ch_p1.setText(String.format("%.4f", ave.get(1).get(0)));
        ch_pe1.setText("-");

        // I2数据
        ch2.setText(String.format("%.4f", ave.get(0).get(1)));
        che2.setText("-");
        ch_p2.setText(String.format("%.4f", ave.get(1).get(1)));
        ch_pe2.setText("-");

        // I3数据
        ch3.setText(String.format("%.4f", ave.get(0).get(2)));
        che3.setText("-");
        ch_p3.setText(String.format("%.4f", ave.get(1).get(2)));
        ch_pe3.setText("-");

        // I4数据
        ch4.setText(String.format("%.4f", ave.get(0).get(3)));
        che4.setText("-");
        ch_p4.setText(String.format("%.4f", ave.get(1).get(3)));
        ch_pe4.setText("-");
    }
}