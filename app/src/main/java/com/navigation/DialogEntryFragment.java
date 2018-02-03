package com.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navigation.library.AwesomeFragment;

/**
 * Created by listen on 2018/2/3.
 */

public class DialogEntryFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialog_entry, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopDialogFragment dialog = new TopDialogFragment();
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getSceneId());
            }
        });

        root.findViewById(R.id.bottom_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment dialog = new BottomSheetDialogFragment();
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getSceneId());
            }
        });

        return root;
    }
}
