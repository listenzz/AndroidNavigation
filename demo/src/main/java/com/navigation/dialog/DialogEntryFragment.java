package com.navigation.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.statusbar.TestStatusBarFragment;

import me.listenzz.navigation.NavigationFragment;


/**
 * Created by listen on 2018/2/3.
 */

public class DialogEntryFragment extends BaseFragment {

    TextView resultText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialog_entry, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.alert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFragment alert = new AlertDialogFragment();
                showDialog(alert, 0);
            }
        });

        root.findViewById(R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAnimationDialogFragment dialog = new CustomAnimationDialogFragment();
                showDialog(dialog, 0);
            }
        });

        root.findViewById(R.id.bottom_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlideAnimationDialogFragment dialog = new SlideAnimationDialogFragment();
                showDialog(dialog, 0);
            }
        });

        root.findViewById(R.id.data_binding).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataBindingDialogFragment fragment = new DataBindingDialogFragment();
                showDialog(fragment, 0);
            }
        });

        root.findViewById(R.id.nested_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationFragment navigationFragment = new NavigationFragment();
                navigationFragment.setRootFragment(new TestStatusBarFragment());
                NestedFragmentDialogFragment dialog = new NestedFragmentDialogFragment();
                dialog.setContentFragment(navigationFragment);
                // dialog.setCancelable(false);
                showDialog(dialog, 0);
            }
        });


        resultText = root.findViewById(R.id.result_text);

        return root;
    }


    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        Log.i(TAG, "onFragmentResult");
        if (data != null) {
            String words = data.getString("text", "");
            resultText.setText(words);
        }
    }
}
