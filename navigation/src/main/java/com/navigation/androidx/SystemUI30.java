package com.navigation.androidx;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SystemUI30 {

    @TargetApi(30)
    public static void setStatusBarTranslucent(@NonNull Window window, boolean translucent) {
        View decorView = window.getDecorView();
        if (translucent) {
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
        }

        int systemUi = decorView.getSystemUiVisibility();
        systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        decorView.setSystemUiVisibility(systemUi);

        window.setDecorFitsSystemWindows(false);
        WindowInsetsController controller = decorView.getWindowInsetsController();
        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        ViewCompat.requestApplyInsets(decorView);
    }

    @TargetApi(30)
    public static void setRenderContentInShortEdgeCutoutAreas(@NonNull Window window, boolean shortEdges) {
//        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        if (shortEdges) {
//            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//        } else {
//            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
//        }
//        window.setAttributes(layoutParams);
    }

    @TargetApi(30)
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

    @TargetApi(30)
    public static void setStatusBarStyle(@NonNull Window window, boolean dark) {
        WindowInsetsController controller = window.getInsetsController();
        controller.setSystemBarsAppearance(dark ? APPEARANCE_LIGHT_STATUS_BARS : 0, APPEARANCE_LIGHT_STATUS_BARS);
    }

    @TargetApi(30)
    public static boolean isStatusBarStyleDark(@NonNull Window window) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        assert controller != null;
        return controller.isAppearanceLightStatusBars();
    }

    public static BarStyle activityStatusBarStyle(@NonNull Activity activity) {
        boolean isDark = isStatusBarStyleDark(activity.getWindow());
        return isDark ? BarStyle.DarkContent : BarStyle.LightContent;
    }

    @TargetApi(30)
    public static void setStatusBarHidden(@NonNull Window window, boolean hidden) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        assert controller != null;
        if (hidden) {
            controller.hide(WindowInsetsCompat.Type.statusBars());
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars());
        }
    }

    @TargetApi(30)
    public static boolean isStatusBarHidden(@NonNull Window window) {
        View decorView = window.getDecorView();
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(decorView);
        assert insets != null;
        return insets.isVisible(WindowInsetsCompat.Type.statusBars());
    }

    public static void appendStatusBarPadding(@NonNull Context context, @NonNull View view) {
        int statusBarHeight = getStatusBarHeight(context);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null && lp.height > 0) {
            lp.height += statusBarHeight;
        }
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + statusBarHeight,
                view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void removeStatusBarPadding(@NonNull Context context, @NonNull View view) {
        int statusBarHeight = getStatusBarHeight(context);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null && lp.height > 0) {
            lp.height -= statusBarHeight;
        }
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop() - statusBarHeight,
                view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void appendStatusBarMargin(@NonNull Context context, @NonNull View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) lp).topMargin += getStatusBarHeight(context);
        }
        view.setLayoutParams(lp);
    }

    private static int statusBarHeight = -1;

    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight != -1) {
            return statusBarHeight;
        }

        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    private volatile static boolean sHasCheckCutout;
    private volatile static boolean sIsCutout;

    // 是否刘海屏
    public static boolean isCutout(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return false;
        }

        if (sHasCheckCutout) {
            return sIsCutout;
        }
        sHasCheckCutout = true;

        Window window = activity.getWindow();
        if (window == null) {
            throw new IllegalStateException("activity has not attach to window");
        }
        View decorView = window.getDecorView();
        sIsCutout = hasNotch(decorView);

        return sIsCutout;
    }

    @TargetApi(28)
    private static boolean hasNotch(View view) {
        WindowInsets windowInsets = view.getRootWindowInsets();
        if (windowInsets == null) {
            throw new IllegalStateException("activity has not yet attach to window, you must call `isCutout` after `Activity#onAttachedToWindow` is called.");
        }
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        return displayCutout != null;
    }

    @TargetApi(28)
    private static void getSafeInsetRect(Window window, Rect out) {
        View decorView = window.getDecorView();
        WindowInsets rootWindowInsets = decorView.getRootWindowInsets();
        if (rootWindowInsets == null) {
            return;
        }
        DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
        if (displayCutout != null) {
            out.set(displayCutout.getSafeInsetLeft(), displayCutout.getSafeInsetTop(),
                    displayCutout.getSafeInsetRight(), displayCutout.getSafeInsetBottom());
        }
    }

    @TargetApi(30)
    public static void setNavigationBarColor(final Window window, int color) {
        window.setNavigationBarContrastEnforced(false);
        window.setNavigationBarColor(color);
    }

    @TargetApi(30)
    public static void setNavigationBarStyle(Window window, boolean dark) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        assert (controller != null);
        controller.setAppearanceLightNavigationBars(dark);
    }

    @TargetApi(30)
    public static boolean isNavigationBarStyleDark(Window window) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        assert (controller != null);
        return controller.isAppearanceLightNavigationBars();
    }

    public static BarStyle activityNavigationBarStyle(Activity activity) {
        boolean isDark = isNavigationBarStyleDark(activity.getWindow());
        return isDark ? BarStyle.DarkContent : BarStyle.LightContent;
    }

    @TargetApi(30)
    public static boolean isNavigationBarHidden(Window window) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(window.getDecorView());
        assert (insets != null);
        return insets.isVisible(WindowInsetsCompat.Type.navigationBars());
    }

    @TargetApi(30)
    public static void setNavigationBarHidden(Window window, boolean hidden) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        assert (controller != null);
        if (hidden) {
            controller.hide(WindowInsetsCompat.Type.navigationBars());
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars());
        }
    }

    @TargetApi(30)
    public static void setNavigationBarLayoutHidden(Window window, boolean hidden) {
        if (window.isFloating()) {
            window.setNavigationBarContrastEnforced(true);
        } else {
            window.setDecorFitsSystemWindows(!hidden);
        }
    }
}
