package com.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


/**
 * Created by Listen on 2018/2/2.
 */

public class WebFragment extends BaseFragment {

    WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.loadUrl("file:///android_asset/input_webview.html");
        setTitle("WebView");
    }

}
