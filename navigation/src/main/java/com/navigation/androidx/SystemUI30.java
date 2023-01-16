package com.navigation.androidx;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;

@TargetApi(30)
public class SystemUI30 {

    public static void disableDecorFitsSystemWindows(@NonNull Window window, boolean excludeBottom) {
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
        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        int systemUi = decorView.getSystemUiVisibility();
        systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        decorView.setSystemUiVisibility(systemUi);
    }

    public static void setRenderContentInShortEdgeCutoutAreas(@NonNull Window window, boolean shortEdges) {

    }

    public static void setStatusBarColor(@NonNull Window window, int color, boolean animated) {
        window.setStatusBarContrastEnforced(false);

        if (animated) {
            int curColor = window.getStatusBarColor();
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, color);
            colorAnimation.addUpdateListener(
                    animator -> window.setStatusBarColor((Integer) animator.getAnimatedValue()));
            colorAnimation.setDuration(200).setStartDelay(0);
            colorAnimation.start();
        } else {
            window.setStatusBarColor(color);
        }
    }

    public static int getStatusBarColor(@NonNull Window window) {
        return window.getStatusBarColor();
    }

    public static void setStatusBarStyle(@NonNull Window window, boolean dark) {
        WindowInsetsController controller = window.getInsetsController();
        controller.setSystemBarsAppearance(dark ? APPEARANCE_LIGHT_STATUS_BARS : 0, APPEARANCE_LIGHT_STATUS_BARS);
    }

    public static boolean isStatusBarStyleDark(@NonNull Window window) {
        WindowInsetsController controller = window.getDecorView().getWindowInsetsController();
        return (controller.getSystemBarsAppearance() & APPEARANCE_LIGHT_STATUS_BARS) != 0;
    }

    public static BarStyle activityStatusBarStyle(@NonNull Activity activity) {
        boolean isDark = isStatusBarStyleDark(activity.getWindow());
        return isDark ? BarStyle.DarkContent : BarStyle.LightContent;
    }

    public static void setStatusBarHidden(@NonNull Window window, boolean hidden) {
        WindowInsetsController controller = window.getDecorView().getWindowInsetsController();
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

    public static void setNavigationBarColor(final Window window, int color) {
        window.setNavigationBarContrastEnforced(false);
        window.setNavigationBarColor(color);
    }

    public static void setNavigationBarStyle(Window window, boolean dark) {
        WindowInsetsController controller = window.getDecorView().getWindowInsetsController();
        controller.setSystemBarsAppearance(dark ? APPEARANCE_LIGHT_NAVIGATION_BARS : 0, APPEARANCE_LIGHT_NAVIGATION_BARS);
    }

    public static boolean isNavigationBarStyleDark(Window window) {
        WindowInsetsController controller = window.getDecorView().getWindowInsetsController();
        return (controller.getSystemBarsAppearance() & APPEARANCE_LIGHT_NAVIGATION_BARS) != 0;
    }


    public static boolean isNavigationBarHidden(Window window) {
        View decorView = window.getDecorView();
        WindowInsets insets = decorView.getRootWindowInsets();
        if (insets == null) {
            return false;
        }
        return !insets.isVisible(WindowInsets.Type.navigationBars());
    }

    public static void setNavigationBarHidden(Window window, boolean hidden) {
        WindowInsetsController controller = window.getDecorView().getWindowInsetsController();

        if (hidden) {
            controller.hide(WindowInsets.Type.navigationBars());
        } else {
            controller.show(WindowInsets.Type.navigationBars());
        }
    }

    public static void setNavigationBarLayoutHidden(Window window, boolean hidden) {
        disableDecorFitsSystemWindows(window, !hidden);
    }
}
