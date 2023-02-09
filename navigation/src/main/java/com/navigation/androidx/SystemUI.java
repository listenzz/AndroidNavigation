package com.navigation.androidx;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SystemUI {

    public static void setDecorFitsSystemWindows(@NonNull Window window, boolean decorFitsSystemWindows) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SystemUI30.disableDecorFitsSystemWindows(window, decorFitsSystemWindows);
            return;
        }

        View decorView = window.getDecorView();
        if (decorFitsSystemWindows) {
            WindowCompat.setDecorFitsSystemWindows(window, true);
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
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }
        decorView.requestApplyInsets();

        int systemUi = decorView.getSystemUiVisibility();
        systemUi |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        systemUi |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(systemUi);
    }

    public static void setRenderContentInShortEdgeCutoutAreas(@NonNull Window window, boolean shortEdges) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            if (shortEdges) {
                if (layoutParams.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
                    layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    window.setAttributes(layoutParams);
                }
            } else {
                if (layoutParams.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                    layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
                    window.setAttributes(layoutParams);
                }
            }
        }
    }

    public static void setStatusBarColor(@NonNull Window window, int color, boolean animated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SystemUI30.setStatusBarColor(window, color, animated);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return SystemUI30.getStatusBarColor(window);
        }
        return window.getStatusBarColor();
    }

    public static void setStatusBarStyle(@NonNull Window window, boolean dark) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SystemUI30.setStatusBarStyle(window, dark);
            return;
        }

        View decorView = window.getDecorView();
        int systemUi = decorView.getSystemUiVisibility();
        if (dark) {
            systemUi |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(systemUi);
    }

    public static boolean isStatusBarStyleDark(@NonNull Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return SystemUI30.isStatusBarStyleDark(window);
        }

        return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0;
    }

    public static BarStyle activityStatusBarStyle(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return SystemUI30.activityStatusBarStyle(activity);
        }
        boolean isDark = isStatusBarStyleDark(activity.getWindow());
        return isDark ? BarStyle.DarkContent : BarStyle.LightContent;
    }

    public static void setStatusBarHidden(@NonNull Window window, boolean hidden) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SystemUI30.setStatusBarHidden(window, hidden);
            return;
        }

        View decorView = window.getDecorView();
        int systemUi = decorView.getSystemUiVisibility();
        if (hidden) {
            systemUi |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        } else {
            systemUi &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        window.getDecorView().setSystemUiVisibility(systemUi);
    }

    public static boolean isStatusBarHidden(@NonNull Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return SystemUI30.isStatusBarHidden(window);
        }
        return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
    }

    public static int statusBarHeight(@NonNull Window window) {
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(window.getDecorView());
        assert windowInsets != null;
        return windowInsets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top;
    }

    public static int navigationBarHeight(@NonNull Window window) {
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(window.getDecorView());
        assert windowInsets != null;
        return windowInsets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).bottom;
    }

    // 是否刘海屏
    public static boolean isCutout(@NonNull Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return false;
        }

        View decorView = window.getDecorView();
        WindowInsets windowInsets = decorView.getRootWindowInsets();
        if (windowInsets == null) {
            throw new IllegalStateException("Activity has not yet attach to window, you must call `isCutout` after `Activity#onAttachedToWindow` is called.");
        }
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        return displayCutout != null;
    }

    public static boolean isGestureNavigationEnabled(ContentResolver contentResolver) {
        return Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2;
    }

    public static void setNavigationBarColor(final Window window, int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SystemUI30.setNavigationBarColor(window, color);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(color);
    }

    public static void setNavigationBarStyle(Window window, boolean dark) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SystemUI30.setNavigationBarStyle(window, dark);
            return;
        }

        View decorView = window.getDecorView();
        int systemUi = decorView.getSystemUiVisibility();
        if (dark) {
            systemUi |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decorView.setSystemUiVisibility(systemUi);
    }

    public static boolean isNavigationBarStyleDark(Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return SystemUI30.isNavigationBarStyleDark(window);
        }

        return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) != 0;
    }

    public static BarStyle activityNavigationBarStyle(Activity activity) {
        boolean isDark = isNavigationBarStyleDark(activity.getWindow());
        return isDark ? BarStyle.DarkContent : BarStyle.LightContent;
    }

    public static boolean isNavigationBarHidden(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return SystemUI30.isNavigationBarHidden(window);
        }
        return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;
    }

    public static void setNavigationBarHidden(Window window, boolean hidden) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SystemUI30.setNavigationBarHidden(window, hidden);
            return;
        }

        View decorView = window.getDecorView();
        int systemUi = decorView.getSystemUiVisibility();
        if (hidden) {
            systemUi |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        } else {
            systemUi &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        window.getDecorView().setSystemUiVisibility(systemUi);
    }

    public static boolean isImeVisible(@NonNull Window window) {
        WindowInsetsCompat insetsCompat = ViewCompat.getRootWindowInsets(window.getDecorView());
        assert insetsCompat != null;
        return insetsCompat.isVisible(WindowInsetsCompat.Type.ime());
    }

    public static boolean isImeVisible(@NonNull View view) {
        WindowInsetsCompat insetsCompat = ViewCompat.getRootWindowInsets(view.getRootView());
        assert insetsCompat != null;
        return insetsCompat.isVisible(WindowInsetsCompat.Type.ime());
    }

    public static int imeHeight(@NonNull View view) {
        WindowInsetsCompat insetsCompat = ViewCompat.getRootWindowInsets(view.getRootView());
        assert insetsCompat != null;
        return insetsCompat.getInsets(WindowInsetsCompat.Type.ime()).bottom;
    }

    public static void showIme(@NonNull Window window) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        assert controller != null;
        controller.show(WindowInsetsCompat.Type.ime());
    }

    public static void hideIme(@NonNull Window window) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        assert controller != null;
        controller.hide(WindowInsetsCompat.Type.ime());
    }

    public static void hideIme(@NonNull View view) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(view.getRootView());
        assert controller != null;
        controller.hide(WindowInsetsCompat.Type.ime());
    }

    public static EdgeInsets getEdgeInsetsForView(@NonNull View view) {
        ViewGroup root = (ViewGroup) view.getRootView();

        int windowHeight = root.getHeight();
        int windowWidth = root.getWidth();

        Rect offset = new Rect();
        view.getDrawingRect(offset);

        try {
            root.offsetDescendantRectToMyCoords(view, offset);
        } catch (Exception e) {
            // ignore
        }

        int leftMargin = 0;
        int topMargin = 0;
        int rightMargin = 0;
        int bottomMargin = 0;

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            leftMargin = lp.leftMargin;
            topMargin = lp.topMargin;
            rightMargin = lp.rightMargin;
            bottomMargin = lp.bottomMargin;
        }

        EdgeInsets insets = new EdgeInsets();
        insets.left = Math.max(offset.left - leftMargin, 0);
        insets.top = Math.max(offset.top - topMargin, 0);
        insets.right = Math.max(windowWidth - offset.right - rightMargin, 0);
        insets.bottom = Math.max(windowHeight - offset.bottom - bottomMargin, 0);
        return insets;
    }

}
