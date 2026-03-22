package com.example.minessip_r.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.minessip_r.R;
import com.example.minessip_r.ui.ResultActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DataFragment extends Fragment {
    private static String TAG = "DataFragment";
    // 文件目录路径
    private static final String BASE_DIRECTORY =
            Environment.getExternalStorageDirectory().getPath() + "/FEHelper";

    private ListView listView;
    private List<String> fileList = new ArrayList<>();
    private File currentDir;
    private ArrayAdapter<String> adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 加载布局
        View root = inflater.inflate(R.layout.fragment_data, container, false);

        // 初始化ListView
        listView = root.findViewById(R.id.listView);

        // 设置初始目录
        currentDir = new File(BASE_DIRECTORY);
        if (!currentDir.exists()) {
            boolean created = currentDir.mkdirs(); // 如果目录不存在则创建
            Log.d(TAG, "创建目录: " + currentDir.getPath() + " 结果: " + created);
        }

        // 加载文件列表
        loadFileList();

        // 设置适配器
        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                fileList
        );
        listView.setAdapter(adapter);

        // 设置列表项点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = fileList.get(position);

            if (selected.equals("..")) {
                // 返回上级目录
                currentDir = currentDir.getParentFile();
                loadFileList();
                adapter.notifyDataSetChanged();
            } else {
                File clickedFile = new File(currentDir, selected);

                if (clickedFile.isDirectory()) {
                    // 进入子目录
                    currentDir = clickedFile;
                    loadFileList();
                    adapter.notifyDataSetChanged();
                } else {
                    // 根据文件类型处理
                    handleFileClick(clickedFile);
                }
            }
        });

        return root;
    }

    // 读取目录文件
    private void loadFileList() {
        fileList.clear();

        // 添加上级目录选项（如果不是根目录）
        if (!currentDir.getPath().equals(BASE_DIRECTORY)) {
            fileList.add("..");
        }

        if (currentDir.exists() && currentDir.isDirectory()) {
            File[] files = currentDir.listFiles();
            if (files != null && files.length > 0) {
                // 【修改点】创建包含所有文件和文件夹的列表
                List<File> allFiles = new ArrayList<>(Arrays.asList(files));

                // 【修改点】按最后修改时间降序排序（最新在前）
                allFiles.sort((f1, f2) -> {
                    long diff = f2.lastModified() - f1.lastModified();
                    if (diff > 0) return 1;
                    if (diff < 0) return -1;
                    return 0;
                });

                // 【修改点】统一添加排序后的文件/文件夹
                for (File file : allFiles) {
                    fileList.add(file.getName());
                }
            } else {
                fileList.add("空目录");
            }
        } else {
            fileList.add("目录不存在或无法访问");
        }

        Log.d(TAG, "目录: " + currentDir.getPath() + " 中文件数量: " + fileList.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        // 刷新文件列表
        if (adapter != null) {
            loadFileList();
            adapter.notifyDataSetChanged();
        }
    }

    // 处理文件点击事件
    private void handleFileClick(File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".dat")) {
            // .dat文件 - 启动ResultActivity并传递文件路径
            openDatFileInResultActivity(file);
        } else {
            // 其他文件类型 - 用外部应用打开
            openFileWithExternalApp(file);
        }
    }

    // 用外部应用打开文件
    private void openFileWithExternalApp(File file) {
        try {
            // 获取文件URI（使用FileProvider安全共享文件）
            Uri fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider", // 使用fileprovider后缀
                    file
            );

            Log.d(TAG, "文件URI: " + fileUri);

            // 确定文件MIME类型
            String mimeType = getMimeType(file.getName());

            // 创建打开文件的Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 验证是否有应用可以处理此Intent
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(
                        requireContext(),
                        "没有找到可以打开此文件的应用程序",
                        Toast.LENGTH_SHORT
                ).show();
            }
        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    "无法打开文件: " + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
            Log.e(TAG, "打开文件失败: " + file.getPath(), e);
        }
    }

    // 打开.dat文件在ResultActivity
    private void openDatFileInResultActivity(File file) {
        try {
            Log.d(TAG, "打开.dat文件: " + file.getPath());

            Intent intent = new Intent(getActivity(), ResultActivity.class);
            intent.putExtra("DAT_FILE_PATH", file.getAbsolutePath());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    "无法打开数据文件: " + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
            Log.e(TAG, "启动ResultActivity失败", e);
        }
    }

    // 根据文件名获取MIME类型
    private String getMimeType(String fileName) {
        // 默认类型
        String type = "*/*";

        // 获取文件扩展名
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.US);

            // 常见文件类型的MIME类型映射
            switch (extension) {
                case "txt": type = "text/plain"; break;
                case "pdf": type = "application/pdf"; break;
                case "doc": case "docx": type = "application/msword"; break;
                case "xls": case "xlsx": type = "application/vnd.ms-excel"; break;
                case "ppt": case "pptx": type = "application/vnd.ms-powerpoint"; break;
                case "jpg": case "jpeg": type = "image/jpeg"; break;
                case "png": type = "image/png"; break;
                case "gif": type = "image/gif"; break;
                case "mp3": type = "audio/mpeg"; break;
                case "wav": type = "audio/wav"; break;
                case "mp4": type = "video/mp4"; break;
                case "avi": type = "video/x-msvideo"; break;
                case "html": case "htm": type = "text/html"; break;
                case "xml": type = "text/xml"; break;
                case "json": type = "application/json"; break;
                case "zip": type = "application/zip"; break;
                case "rar": type = "application/x-rar-compressed"; break;
                case "apk": type = "application/vnd.android.package-archive"; break;
                case "csv": type = "text/csv"; break;
                // .dat文件的MIME类型可以设置为application/octet-stream
                case "dat": type = "application/octet-stream"; break;
            }
        }

        return type;
    }
}