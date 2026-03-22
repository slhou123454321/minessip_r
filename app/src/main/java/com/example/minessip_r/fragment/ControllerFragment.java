package com.example.minessip_r.fragment;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.minessip_r.Constants;
import com.example.minessip_r.ParamSaveClass;
import com.example.minessip_r.R;
import com.example.minessip_r.ui.MainActivity;

import java.util.Date;

public class ControllerFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "ControllerFragment";

    // ==================== 静态变量（供其他类访问） ====================

    /** ADC采样率，用于控制数据采集频率 */
    public static int getSPS = 250;

    /** 工区名称，用于保存数据的文件夹名 */
    public static String workSpacefileNameE = "默认工区";

    /** 时间戳，用于区分不同时间采集的数据 */
    public static String fileTimeE = "时间戳";

    /** 结果文件名，保存的数据文件名称 */
    public static String resultFileNameE = "defaultData";

    /** 点数量 */
    public static int dotNumber = getSPS*4;

    /** 信号频率 */
    public static double signalFrequency = 1;

    /** DAC输出幅值 */
    public static double dacOutputValue = 5.0;

    /** DAC输出频率 */
    public static double dacOutputFrequency = 250.0;

    /** DAC输出开关状态 */
    public static boolean isDacOutputEnabled = true;

    /** 低功耗模式状态 (0=不使用, 1=使用) */
    public static int lowPowerModeState = 0;

    /** 选中的DAC通道 */
    public static String selectedDacChannels = "1";

    // ==================== 实例变量 ====================

    // ADC 相关
    private Spinner adcSamplingSpinner;
    private int[] samplingArr = {250, 500, 1000, 2000};

    // DAC 相关
    private CheckBox dacChannel1, dacChannel2, dacChannel3, dacChannel4;
    private Spinner dacValueSpinner;
    private Spinner dacFrequencySpinner;
    private RadioGroup dacOutputSwitch;
    private RadioButton dacOutputOpen, dacOutputClose;

    // DAC 配置值（实例变量）
    private boolean[] selectedChannels = {true, false, false, false};
    private double selectedDacValue = 5.0;
    private double selectedDacFrequency = 250.0;
    private boolean isDacOutputOpen = true;

    // 低功耗模式
    private CheckBox lowPowerMode;
    private Button lowPowerApplyButton;
    private boolean isLowPowerModeChecked = false;  // UI上的勾选状态

    // 文件保存相关
    private EditText fileNameEdit, fileTimeEdit, resultFileNameEdit;
    private Button saveAllConfigButton;

    // 映射数组
    private final double[] dacValueMap = {5.0, 2.5, 1.25, 0.625};
    private final double[] dacFrequencyMap = {250, 125, 100, 62.5, 60, 55, 50, 31.25};
    private final String[] dacValueStr = {"5V", "2.5V", "1.25V", "0.625V"};
    private final String[] dacFrequencyStr = {"250Hz", "125Hz", "100Hz", "62.5Hz", "60Hz", "55Hz", "50Hz", "31.25Hz"};

    private MainActivity mainActivity;

    public ControllerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View logLayout = inflater.inflate(R.layout.fragment_controller, container, false);
        return logLayout;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity.fragmentState = 1;

        Log.d(TAG, "onActivityCreated");
        mainActivity = (MainActivity) getActivity();

        if (mainActivity == null) return;

        // 初始化所有控件
        initViews();

        // 设置监听器
        setListeners();

        // 从静态变量恢复配置
        restoreFromStaticVariables();

        // 初始化默认显示
        updateFileNameDisplay();
    }

    /**
     * 从静态变量恢复配置到UI
     */
    private void restoreFromStaticVariables() {
        // 恢复ADC采样率
        if (adcSamplingSpinner != null) {
            for (int i = 0; i < samplingArr.length; i++) {
                if (samplingArr[i] == getSPS) {
                    adcSamplingSpinner.setSelection(i);
                    break;
                }
            }
        }

        // 恢复DAC通道选择
        if (selectedDacChannels != null && dacChannel1 != null) {
            dacChannel1.setChecked(selectedDacChannels.contains("1"));
            dacChannel2.setChecked(selectedDacChannels.contains("2"));
            dacChannel3.setChecked(selectedDacChannels.contains("3"));
            dacChannel4.setChecked(selectedDacChannels.contains("4"));
        }

        // 恢复DAC输出值
        if (dacValueSpinner != null) {
            for (int i = 0; i < dacValueMap.length; i++) {
                if (dacValueMap[i] == dacOutputValue) {
                    dacValueSpinner.setSelection(i);
                    selectedDacValue = dacOutputValue;
                    break;
                }
            }
        }

        // 恢复DAC频率
        if (dacFrequencySpinner != null) {
            for (int i = 0; i < dacFrequencyMap.length; i++) {
                if (dacFrequencyMap[i] == dacOutputFrequency) {
                    dacFrequencySpinner.setSelection(i);
                    selectedDacFrequency = dacOutputFrequency;
                    break;
                }
            }
        }

        // 恢复DAC输出开关
        if (dacOutputOpen != null && dacOutputClose != null) {
            if (isDacOutputEnabled) {
                dacOutputOpen.setChecked(true);
            } else {
                dacOutputClose.setChecked(true);
            }
            isDacOutputOpen = isDacOutputEnabled;
        }

        // 恢复低功耗模式状态 (0=不使用, 1=使用)
        if (lowPowerMode != null) {
            boolean isChecked = (lowPowerModeState == 1);
            lowPowerMode.setChecked(isChecked);
            isLowPowerModeChecked = isChecked;
        }

        // 恢复文件名
        if (fileNameEdit != null) {
            fileNameEdit.setText(workSpacefileNameE);
        }
        if (fileTimeEdit != null) {
            fileTimeEdit.setText(fileTimeE);
        }
        if (resultFileNameEdit != null) {
            resultFileNameEdit.setText(resultFileNameE);
        }
    }

    /**
     * 初始化所有控件
     */
    private void initViews() {

        // ADC 采样率 Spinner
        adcSamplingSpinner = mainActivity.findViewById(R.id.adc_sampling_spinner);

        // DAC 通道选择 CheckBox
        dacChannel1 = mainActivity.findViewById(R.id.dac_channel_1);
        dacChannel2 = mainActivity.findViewById(R.id.dac_channel_2);
        dacChannel3 = mainActivity.findViewById(R.id.dac_channel_3);
        dacChannel4 = mainActivity.findViewById(R.id.dac_channel_4);

        // DAC 幅值和频率 Spinner
        dacValueSpinner = mainActivity.findViewById(R.id.dac_value_spinner);
        dacFrequencySpinner = mainActivity.findViewById(R.id.dac_frequency_spinner);

        // DAC 输出开关
        dacOutputSwitch = mainActivity.findViewById(R.id.dac_output_switch);
        dacOutputOpen = mainActivity.findViewById(R.id.dac_output_open);
        dacOutputClose = mainActivity.findViewById(R.id.dac_output_close);

        // 低功耗模式
        lowPowerMode = mainActivity.findViewById(R.id.low_power_mode);
        lowPowerApplyButton = mainActivity.findViewById(R.id.low_power_apply_button);

        // 保存参数输入框
        fileNameEdit = mainActivity.findViewById(R.id.file_name);
        fileTimeEdit = mainActivity.findViewById(R.id.file_time);
        resultFileNameEdit = mainActivity.findViewById(R.id.result_file_name);

        // 底部保存按钮
        saveAllConfigButton = mainActivity.findViewById(R.id.save_all_config_button);

        // 设置默认值
        if (dacChannel1 != null) dacChannel1.setChecked(true);
        if (dacOutputOpen != null) dacOutputOpen.setChecked(true);

        // 设置 Spinner 适配器
        setupSpinnerAdapters();
    }

    /**
     * 设置 Spinner 适配器
     */
    private void setupSpinnerAdapters() {
        // ADC 采样率适配器
        if (adcSamplingSpinner != null && mainActivity != null) {
            ArrayAdapter<CharSequence> adcAdapter = ArrayAdapter.createFromResource(mainActivity,
                    R.array.sampling_rate, android.R.layout.simple_spinner_item);
            adcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adcSamplingSpinner.setAdapter(adcAdapter);
        }

        // DAC 输出幅值适配器
        if (dacValueSpinner != null && mainActivity != null) {
            ArrayAdapter<CharSequence> dacValueAdapter = ArrayAdapter.createFromResource(mainActivity,
                    R.array.dac_output_values, android.R.layout.simple_spinner_item);
            dacValueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dacValueSpinner.setAdapter(dacValueAdapter);
        }

        // DAC 输出频率适配器
        if (dacFrequencySpinner != null && mainActivity != null) {
            ArrayAdapter<CharSequence> dacFreqAdapter = ArrayAdapter.createFromResource(mainActivity,
                    R.array.dac_frequencies, android.R.layout.simple_spinner_item);
            dacFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dacFrequencySpinner.setAdapter(dacFreqAdapter);
        }
    }

    /**
     * 设置所有监听器
     */
    private void setListeners() {
        // ADC Spinner 监听
        if (adcSamplingSpinner != null) {
            adcSamplingSpinner.setOnItemSelectedListener(this);
        }

        // DAC 通道选择监听
        if (dacChannel1 != null) dacChannel1.setOnClickListener(this);
        if (dacChannel2 != null) dacChannel2.setOnClickListener(this);
        if (dacChannel3 != null) dacChannel3.setOnClickListener(this);
        if (dacChannel4 != null) dacChannel4.setOnClickListener(this);

        // DAC Spinner 监听
        if (dacValueSpinner != null) {
            dacValueSpinner.setOnItemSelectedListener(this);
        }
        if (dacFrequencySpinner != null) {
            dacFrequencySpinner.setOnItemSelectedListener(this);
        }

        // DAC 输出开关监听
        if (dacOutputSwitch != null) {
            dacOutputSwitch.setOnCheckedChangeListener((group, checkedId) -> {
                isDacOutputOpen = (checkedId == R.id.dac_output_open);
                Log.d(TAG, "DAC output: " + (isDacOutputOpen ? "打开" : "关闭"));
            });
        }

        // 低功耗模式应用按钮监听
        if (lowPowerApplyButton != null) {
            lowPowerApplyButton.setOnClickListener(this);
        }

        // 底部保存按钮监听
        if (saveAllConfigButton != null) {
            saveAllConfigButton.setOnClickListener(this);
        }
    }

    /**
     * 更新文件名显示
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateFileNameDisplay() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(System.currentTimeMillis());
        String currentDate = formatter.format(date);

        if (fileTimeEdit != null && fileTimeEdit.getText().toString().equals("时间戳")) {
            fileTimeEdit.setText(currentDate);
        }
        if (fileNameEdit != null && fileNameEdit.getText().toString().isEmpty()) {
            fileNameEdit.setText(workSpacefileNameE);
        }
        if (resultFileNameEdit != null && resultFileNameEdit.getText().toString().isEmpty()) {
            resultFileNameEdit.setText(resultFileNameE);
        }
    }

    /**
     * 获取选中的DAC通道字符串
     */
    private String getSelectedChannelsString() {
        StringBuilder sb = new StringBuilder();
        if (dacChannel1 != null && dacChannel1.isChecked()){
            sb.append("1/");
        } else {
            sb.append("0/");
        }
        if (dacChannel2 != null && dacChannel2.isChecked()){
            sb.append("1/");
        } else {
            sb.append("0/");
        }
        if (dacChannel3 != null && dacChannel3.isChecked()) {
            sb.append("1/");
        } else {
            sb.append("0/");
        }
        if (dacChannel4 != null && dacChannel4.isChecked()){
            sb.append("1/");
        } else {
            sb.append("0/");
        }
        String result = sb.toString();
        return result;
    }

    /**
     * 获取DAC幅值字符串
     */
    private String getDacValueString() {
        int position = dacValueSpinner != null ? dacValueSpinner.getSelectedItemPosition() : 0;
        return position >= 0 && position < dacValueStr.length ? dacValueStr[position] : "5V";
    }

    /**
     * 获取DAC频率字符串
     */
    private String getDacFrequencyString() {
        int position = dacFrequencySpinner != null ? dacFrequencySpinner.getSelectedItemPosition() : 0;
        return position >= 0 && position < dacFrequencyStr.length ? dacFrequencyStr[position] : "250Hz";
    }

    /**
     * 发送低功耗模式配置
     * @param useLowPower 0=不使用, 1=使用
     */
    private void sendLowPowerConfig(int useLowPower) {
        String powerState = (useLowPower == 1) ? "开启" : "关闭";
        Log.d(TAG, "发送低功耗配置: " + powerState + " (value=" + useLowPower + ")");

        // TODO: 根据实际通信协议修改命令格式
        String cmd = "lowpower/"+useLowPower+"\r\n";

        if (mainActivity != null) {
            // TODO: 实际发送命令
            mainActivity.sendCommand(cmd);
            mainActivity.logAppend("->低功耗配置: " + powerState + " (命令: " + cmd + ")");
            Toast.makeText(mainActivity, "低功耗模式已" + powerState, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新静态变量并发送低功耗配置
     */
    private void applyLowPowerMode() {
        // 获取当前CheckBox的勾选状态
        isLowPowerModeChecked = lowPowerMode != null && lowPowerMode.isChecked();

        // 转换为 0 或 1
        int powerValue = isLowPowerModeChecked ? 1 : 0;

        // 更新静态变量
        lowPowerModeState = powerValue;

        // 发送配置
        sendLowPowerConfig(powerValue);
    }

    /**
     * 保存所有配置
     */
    private void saveAllConfig() {
        // 更新实例变量
        updateInstanceVariablesFromUI();

        // 更新静态变量
        updateStaticVariables();

        // 保存到 ParamSaveClass
        ParamSaveClass.workSpaceName = workSpacefileNameE;
        ParamSaveClass.workSpacePath = Constants.DATA_DIRECTORY + "/" + workSpacefileNameE + "-" + fileTimeE;
        ParamSaveClass.resultDir = ParamSaveClass.workSpaceName + "/" + resultFileNameE;

        // 构建保存信息日志
        String saveInfo = String.format(
                "配置保存成功:\n" +
                        "ADC采样率: %d SPS\n" +
                        "DAC通道: %s\n" +
                        "DAC幅值: %s\n" +
                        "DAC频率: %s\n" +
                        "DAC输出: %s\n" +
                        "低功耗模式: %s\n" +
                        "工区名: %s\n" +
                        "文件名: %s",
                getSPS,
                getSelectedChannelsString(),
                getDacValueString(),
                getDacFrequencyString(),
                isDacOutputOpen ? "打开" : "关闭",
                lowPowerModeState == 1 ? "开启" : "关闭",
                workSpacefileNameE,
                resultFileNameE
        );

        Log.d(TAG, saveInfo);

        // TODO: 发送完整配置到设备
        sendAllConfigToDevice();

        // 显示保存成功提示
        if (mainActivity != null) {
            mainActivity.logAppend("->" + saveInfo);
            Toast.makeText(mainActivity, "配置保存成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发送完整配置到设备
     */
    private void sendAllConfigToDevice() {
        String channels = getSelectedChannelsString();
        String dacValueStr = getDacValueString();
        String dacFreqStr = getDacFrequencyString();
        String dacOutput = isDacOutputOpen ? "1" : "0";
        int lowPower = lowPowerModeState;

        // TODO: 根据实际协议修改命令格式
        Date d = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        //获取当前日期，在配置命令中发送到下面去，让嵌入式建立文件
        String dateNowStr1 = sdf1.format(d);
        String dateNowStr2 = sdf2.format(d);
        String cmd = "Config/"+getSPS+"/"+channels+dacValueStr+"/"+dacFreqStr+"/"+dacOutput+"/"+dateNowStr1+"/"+dateNowStr2+"\r\n";
                // getSPS, channels, dacValueStr, dacFreqStr, dacOutput, lowPower);
        Log.d(TAG, "发送完整配置命令: " + cmd);

        if (mainActivity != null) {
            // TODO: 实际发送命令
            mainActivity.sendCommand(cmd);
            mainActivity.logAppend("->完整配置命令: " + cmd);
        }
    }

    /**
     * 从UI更新实例变量
     */
    private void updateInstanceVariablesFromUI() {
        // ADC采样率已在 onItemSelected 中更新

        // DAC通道选择
        selectedChannels[0] = dacChannel1 != null && dacChannel1.isChecked();
        selectedChannels[1] = dacChannel2 != null && dacChannel2.isChecked();
        selectedChannels[2] = dacChannel3 != null && dacChannel3.isChecked();
        selectedChannels[3] = dacChannel4 != null && dacChannel4.isChecked();

        // DAC幅值和频率已在 onItemSelected 中更新
        // DAC开关已在监听器中更新
        // 低功耗模式在 applyLowPowerMode 中更新
    }

    /**
     * 更新静态变量
     */
    private void updateStaticVariables() {
        // 更新ADC采样率
        getSPS = this.getSPS;

        // 更新DAC配置
        dacOutputValue = selectedDacValue;
        dacOutputFrequency = selectedDacFrequency;
        isDacOutputEnabled = isDacOutputOpen;
        selectedDacChannels = getSelectedChannelsString();

        // 低功耗模式状态已在 applyLowPowerMode 中更新

        // 更新文件名
        if (fileNameEdit != null) {
            workSpacefileNameE = fileNameEdit.getText().toString();
        }
        if (fileTimeEdit != null) {
            fileTimeE = fileTimeEdit.getText().toString();
        }
        if (resultFileNameEdit != null) {
            resultFileNameE = resultFileNameEdit.getText().toString();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // 处理通道选择变化
        if (id == R.id.dac_channel_1 || id == R.id.dac_channel_2 ||
                id == R.id.dac_channel_3 || id == R.id.dac_channel_4) {
            Log.d(TAG, "DAC通道选择: " + getSelectedChannelsString());
        }
        // 处理低功耗模式应用按钮
        else if (id == R.id.low_power_apply_button) {
            applyLowPowerMode();
        }
        // 处理保存所有配置按钮
        else if (id == R.id.save_all_config_button) {
            saveAllConfig();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int viewId = parent.getId();

        // 处理ADC采样率选择
        if (viewId == R.id.adc_sampling_spinner) {
            if (position >= 0 && position < samplingArr.length) {
                getSPS = samplingArr[position];
                dotNumber = 4*getSPS;
                Log.d(TAG, "ADC采样率: " + getSPS + " SPS");
            }
        }
        // 处理DAC输出值选择
        else if (viewId == R.id.dac_value_spinner) {
            if (position >= 0 && position < dacValueMap.length) {
                selectedDacValue = dacValueMap[position];
                Log.d(TAG, "DAC输出幅值: " + selectedDacValue + "V");
            }
        }
        // 处理DAC频率选择
        else if (viewId == R.id.dac_frequency_spinner) {
            if (position >= 0 && position < dacFrequencyMap.length) {
                selectedDacFrequency = dacFrequencyMap[position];
                Log.d(TAG, "DAC输出频率: " + selectedDacFrequency + "Hz");
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 不需要处理
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        updateFileNameDisplay();
    }
}