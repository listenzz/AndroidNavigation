package com.navigation.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.androidx.BarStyle;
import com.navigation.androidx.StackFragment;
import com.navigation.androidx.Style;
import com.navigation.statusbar.TestStatusBarFragment;


/**
 * Created by listen on 2018/2/3.
 */

public class DialogEntryFragment extends BaseFragment {

    private static final int REQUEST_CODE_DATETIME = 1;
    private static final int REQUEST_CODE_AREA = 2;

    TextView resultText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialog_entry, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.alert).setOnClickListener(view -> {
            AlertDialogFragment alert = new AlertDialogFragment();
            showAsDialog(alert, 0);
        });

        root.findViewById(R.id.dialog).setOnClickListener(v -> {
            CustomAnimationDialogFragment dialog = new CustomAnimationDialogFragment();
            showAsDialog(dialog, 0);
        });

        root.findViewById(R.id.datetime_picker).setOnClickListener(v -> {
            DatetimePickerDialogFragment dialog = DatetimePickerDialogFragment.newInstance(null);
            showAsDialog(dialog, REQUEST_CODE_DATETIME);
        });

        root.findViewById(R.id.area_picker).setOnClickListener(v -> {
            AreaPickerDialogFragment dialog = new AreaPickerDialogFragment();
            showAsDialog(dialog, REQUEST_CODE_AREA);
        });

        root.findViewById(R.id.data_binding).setOnClickListener(view -> {
            DataBindingDialogFragment fragment = new DataBindingDialogFragment();
            showAsDialog(fragment, 0);
        });

        root.findViewById(R.id.nested_fragment).setOnClickListener(view -> {
            StackFragment stackFragment = new StackFragment();
            stackFragment.setRootFragment(new TestStatusBarFragment());
            NestedFragmentDialogFragment dialog = new NestedFragmentDialogFragment();
            dialog.setContentFragment(stackFragment);
            // dialog.setCancelable(false);
            showAsDialog(dialog, 0);
        });


        resultText = root.findViewById(R.id.result_text);

        return root;
    }


    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        Log.i(TAG, "onFragmentResult");
        if (requestCode == REQUEST_CODE_DATETIME) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String time = data.getString(DatetimePickerDialogFragment.KEY_TIME);
                resultText.setText("选中的时间是：" + time);
            } else {
                resultText.setText("");
            }
        } else if (requestCode == REQUEST_CODE_AREA) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String area = data.getString(AreaPickerDialogFragment.KEY_SELECTED_AREA);
                resultText.setText("选中的区域是：" + area);
            } else {
                resultText.setText("");
            }
        } else {
            if (data != null) {
                String words = data.getString("text", "");
                resultText.setText(words);
            } else {
                resultText.setText("");
            }
        }
    }


    @Override
    protected void onCustomStyle(@NonNull Style style) {
        style.setStatusBarStyle(BarStyle.DarkContent);
    }
}
