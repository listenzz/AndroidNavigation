package com.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WebFragment extends BaseFragment {

    WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_web, container, false);

        webView = root.findViewById(R.id.web);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (webView != null) {
                webView.removeAllViews();
                webView.destroy();
                webView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView.loadUrl("file:///android_asset/input_webview.html");
        setTitle("WebView");
    }

    @Override
    public boolean preferredEdgeToEdge() {
        // 适配在 WebView 上使用键盘
        return false;
    }

}
