package com.navigation.androidx;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;

@TargetApi(30)
public class SystemUI30 {

    public static void setDecorFitsSystemWindows(@NonNull Window window, boolean excludeBottom) {
        View decorView = window.getDecorView();
        if (excludeBottom) {
            window.setDecorFitsSystemWindows(true);
            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                boolean portrait = window.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
                return defaultInsets.replaceSystemWindowInsets(
                        portrait ? defaultInsets.getSystemWindowInsetLeft() : 0,
                        0,
                        portrait ? defaultInsets.getSystemWindowInsetRight() : 0,
                        defaultInsets.getSystemWindowInsetBottom());
            });
        } else {
            decorView.setOnApplyWindowInsetsListener(null);
            window.setDecorFitsSystemWindows(false);
        }
        decorView.requestApplyInsets();

        WindowInsetsController controller = decorView.getWindowInsetsController();
        assert controller != null;
        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public static void setStatusBarHidden(@NonNull Window window, boolean hidden) {
        WindowInsetsController controller = window.getDecorView().getWindowInsetsController();
        assert controller != null;
        if (hidden) {
            controller.hide(WindowInsets.Type.statusBars());
        } else {
            controller.show(WindowInsets.Type.statusBars());
        }
    }

    public static boolean isStatusBarHidden(@NonNull Window window) {
        View decorView = window.getDecorView();
        WindowInsets insets = decorView.getRootWindowInsets();
        if (insets == null) {
            return false;
        }
        return !insets.isVisible(WindowInsets.Type.statusBars());
    }

    public static void setNavigationBarHidden(Window window, boolean hidden) {
        WindowInsetsController controller = window.getDecorView().getWindowInsetsController();
        assert controller != null;
        if (hidden) {
            controller.hide(WindowInsets.Type.navigationBars());
        } else {
            controller.show(WindowInsets.Type.navigationBars());
        }
    }

    public static boolean isNavigationBarHidden(Window window) {
        View decorView = window.getDecorView();
        WindowInsets insets = decorView.getRootWindowInsets();
        if (insets == null) {
            return false;
        }
        return !insets.isVisible(WindowInsets.Type.navigationBars());
    }
}
