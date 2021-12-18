package com.navigation.toolbar;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.androidx.ToolbarButtonItem;

public class SearchFragment extends BaseFragment {


    EditText searchInput;

    View clearButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ToolbarButtonItem toolbarButtonItem = new ToolbarButtonItem.Builder().title("搜索").build();
        setRightBarButtonItem(toolbarButtonItem);

        View searchBar = LayoutInflater.from(requireContext()).inflate(R.layout.search_bar, getToolbar(), false);
        getToolbar().addView(searchBar, new Toolbar.LayoutParams(-1, -1, Gravity.CENTER));

        searchInput = searchBar.findViewById(R.id.search_input);
        clearButton = searchBar.findViewById(R.id.search_clear);
    }
}
