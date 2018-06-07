package com.navigation.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
                alert.showDialog(requireActivity());
            }
        });

        root.findViewById(R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopDialogFragment dialog = new TopDialogFragment();
                dialog.showDialog(requireActivity());
            }
        });

        root.findViewById(R.id.bottom_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment dialog = new BottomSheetDialogFragment();
                dialog.showDialog(requireActivity());
            }
        });

        return root;
    }
}
