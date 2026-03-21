package com.example.minessip_r.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.minessip_r.R;
import com.example.minessip_r.ui.MainActivity;


public class NoneFileFragment extends Fragment {
    @Nullable
    private MainActivity mainActivity;
    private Button butopenchart;
    private LogFragment logFragment;
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View noneFileFragment = inflater.inflate(R.layout.fragment_none_file, container, false);
        noneFileFragment.findViewById(R.id.none_file_fragment_text_view).setOnClickListener(((MainActivity) getActivity()));

        return noneFileFragment;
    }
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity=(MainActivity) getActivity();
        //logFragment= (LogFragment) mainActivity.getSupportFragmentManager().findFragmentById(R.id.log_layout);
        // butopenchart = (Button) mainActivity.findViewById(R.id.open_chart);
        //butopenchart.setOnClickListener(this);
    }

    public void onClick(View view) {
        /*
        switch (view.getId()) {
            case R.id.open_chart:

                break;
            default:
                break;
        }*/
    }
}
