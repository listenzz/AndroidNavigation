package com.navigation.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navigation.R;

import me.listenzz.navigation.AwesomeFragment;


/**
 * Created by listen on 2018/2/3.
 */

public class DialogEntryFragment extends AwesomeFragment {

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
                showDialogFragment(alert, 0);
            }
        });

        root.findViewById(R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopDialogFragment dialog = new TopDialogFragment();
                showDialogFragment(dialog, 0);
            }
        });

        root.findViewById(R.id.bottom_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment dialog = new BottomSheetDialogFragment();
                showDialogFragment(dialog, 0);
            }
        });

        root.findViewById(R.id.data_binding).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataBindingDialogFragment fragment = new DataBindingDialogFragment();
                showDialogFragment(fragment, 0);
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
            String words = data.getString("words", "");
            resultText.setText(words);
        }
    }
}
