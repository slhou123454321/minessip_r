package com.example.minessip_r.chart;

import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ChartPlay {
    private static final String TAG = "ChartPlay";

    public static void initChartView(LineChart linechart, int count, final String unitX, final String unitY, String description){
        /*MyMarkerView myMarkerView = new MyMarkerView(getContext());
        myMarkerView.setChartView(chart);
        chart.setMarker(myMarkerView);*/

        linechart.getDescription().setText(description);
        linechart.setDrawBorders(true);//显示边界
        linechart.setDrawGridBackground(false);//不显示图表网格线
        (linechart.getLegend()).setTextSize(7f);//设置图例文字大小
        Legend legend = linechart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setXEntrySpace(20f);
        XAxis xaxis=linechart.getXAxis();//设置x轴
        //xaxis.setGranularity(1f);
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴底端显示
        xaxis.setLabelCount(count,true);//设置轴标签数量
        xaxis.setDrawGridLines(false);//不显示x轴网格线
//        xaxis.setValueFormatter(new IAxisValueFormatter() {//设置x轴轴标签
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return String.valueOf(((Math.round(value*10))/10)).concat(unitX);
//                //return xList.get((int) value);
//            }
//        });
        linechart.getAxisRight().setEnabled(false);//右侧y轴不显示
        YAxis leftYAxis = linechart.getAxisLeft();//设置y轴
        leftYAxis.setLabelCount(4,false);//第一个参数是Y轴坐标的个数，第二个参数是是否不均匀分布，true是不均匀分布
        leftYAxis.setDrawGridLines(false);//y轴网格线不显示
        final List<String> yList = new ArrayList<>();
        yList.add("1");
        yList.add("10");
        yList.add("100");
        yList.add("1000");
//        leftYAxis.setValueFormatter(new IAxisValueFormatter() {//设置y轴轴标签
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                Log.e(TAG, "----->getFormattedValue: "+value);//"value" represents the position of the label on the axis (x or y)
//                //return yList.get((int)value);
//                //return String.valueOf(((Math.round(Math.pow(10,value)/100))*100)).concat(unitY);
//                return String.valueOf(((Math.round(value*10))/10)).concat(unitY);
//            }
//        });
    }

    /**
     * * 展示曲线
     *
     * @param lineChart  图表
     * @param dataList   曲线数据
     * @param name       曲线名
     * @param color      曲线颜色
     */
    public static void showLineChart(LineChart lineChart,List<Float> dataList, String name, int color,int t){
        List<Entry> entries = new ArrayList<>();
        Log.e("showLineChart", "开始"+dataList.size());
        for (int i = t; i < dataList.size(); i++) {
            Float data = dataList.get(i);
           /*  在此可查看 Entry构造方法，可发现 可传入数值 Entry(float x, float y)
            也可传入Drawable， Entry(float x, float y, Drawable icon) 可在XY轴交点 设置Drawable图像展示*/
            Entry entry = new Entry(i+1,data);
            //Entry entry = new Entry(i,((float)Math.log10(data))*100);
            //Entry entry = new Entry(i*1000000/500000, (float) ((float)Math.log(data)/Math.log(10)));
            //Entry entry = new Entry(i*1000000/500000,((float)Math.log10(data)));
            //Log.e("showLineChart", i+"  ((float)Math.log10(data))   "+((float)Math.log10(data))+"   "+data);
            entries.add(entry);
        }
        Log.e("showLineChart", "结束"+dataList.size());
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }

    public static void showLineChart1(LineChart lineChart,List<Float> dataList, String name, int color,int t){
        List<Entry> entries = new ArrayList<>();
        for (int i = t; i < dataList.size(); i++) {
            Float data = dataList.get(i);
            //Entry entry = new Entry(i*1000000/500000,data);
            Entry entry = new Entry(i+1,data);
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }
    //两条线
    public static void showLineChart2(LineChart lineChart,List<Float> dataList, String name, int color,int t){
        List<Entry> entries = new ArrayList<>();
        Log.e("showLineChart", "开始"+dataList.size());
        for (int i = t; i < dataList.size(); i++) {
            Float data = dataList.get(i);
            Entry entry = new Entry(i,((float)Math.log10(data)));
            Log.e("showLineChart", i+"  ((float)Math.log10(data))*100   "+(((float)Math.log10(data))*100)+"   "+data);
            entries.add(entry);
        }
        Log.e("showLineChart", "结束"+dataList.size());
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }
    /**
     *
     * @param lineChart  图表
     * @param dataList   曲线数据
     * @param name       曲线名
     * @param color      曲线颜色
     */
    public static void addLine(LineChart lineChart,List<Float> dataList, String name, int color,int t) {
        List<Entry> entries = new ArrayList<>();
        for (int i = t; i < dataList.size(); i++) {
            Float data = dataList.get(i);
            //Entry entry = new Entry(i*10/5, ((float)Math.log10(data))*100);
            Entry entry = new Entry(i, data);
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);
        lineChart.getLineData().addDataSet(lineDataSet);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }
    public static void addLine1(LineChart lineChart,List<Float> dataList, String name, int color,int t) {
        List<Entry> entries = new ArrayList<>();
        for (int i = t; i < dataList.size(); i++) {
            Float data = dataList.get(i);
            Entry entry = new Entry(i, data);
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);
        lineChart.getLineData().addDataSet(lineDataSet);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    /**
     * 曲线初始化设置 一个LineDataSet 代表一条曲线
     *
     * @param lineDataSet 线条
     * @param color       线条颜色
     * @param mode
     */
    private static void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        //lineDataSet.setCircleColor(color);
        //lineDataSet.setLineWidth(1f);
        //lineDataSet.setCircleRadius(3f);
        //设置曲线值的圆点是实心还是空心
        lineDataSet.setDrawCircles(false);//不显示点
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawValues(false);//不显示值
        //lineDataSet.setValueTextSize(10f);
        //设置折线图填充
        //lineDataSet.setDrawFilled(true);
        // lineDataSet.setFormLineWidth(1f);
        //lineDataSet.setFormSize(15.f);
        if (mode == null) {
            //设置曲线展示为圆滑曲线
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
    }
}
