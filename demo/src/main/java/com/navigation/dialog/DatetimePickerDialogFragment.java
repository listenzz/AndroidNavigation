package com.navigation.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bigkoo.pickerview.view.WheelTime;
import com.navigation.R;
import com.navigation.androidx.AwesomeFragment;
import com.navigation.androidx.FragmentHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatetimePickerDialogFragment extends AwesomeFragment {

    public static final String KEY_TIME = "time";

    public static DatetimePickerDialogFragment newInstance(@Nullable String time) {
        DatetimePickerDialogFragment fragment = new DatetimePickerDialogFragment();
        Bundle args = FragmentHelper.getArguments(fragment);
        args.putString(KEY_TIME, time);
        return fragment;
    }

    WheelTime wheelTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_datetime_picker, container, false);
        wheelTime = new WheelTime(root, new boolean[]{true, true, true, false, false, false}, Gravity.CENTER, 18);
        root.findViewById(R.id.iv_cancel).setOnClickListener(v -> hideAsDialog());
        root.findViewById(R.id.tv_finish).setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putString(KEY_TIME, wheelTime.getTime().replace(" 0:0:0", ""));
            setResult(Activity.RESULT_OK, data);
            hideAsDialog();
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args =  FragmentHelper.getArguments(this);
        String time = args.getString(KEY_TIME);
        Date date = null;
        if (time != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                date = simpleDateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }

        Calendar start = Calendar.getInstance();
        start.setTime(new Date());
        Calendar end = Calendar.getInstance();
        end.set(Calendar.YEAR, 2030);
        end.set(Calendar.MONTH, 11);
        end.set(Calendar.DAY_OF_MONTH, 31);
        // 只能选当天及之后的日期
        wheelTime.setRangDate(start, end);
        wheelTime.setPicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        wheelTime.isCenterLabel(false);
        wheelTime.setCyclic(false);
        wheelTime.setLabels("年", "月", "日", null, null, null);
        wheelTime.setLineSpacingMultiplier(2f);
        wheelTime.setItemsVisible(7);
    }
}
