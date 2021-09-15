package com.navigation.androidx;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;

public class SystemUI {

    public static void setStatusBarTranslucent(Window window, boolean translucent) {
        View decorView = window.getDecorView();
        if (translucent) {
            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                return defaultInsets.consumeSystemWindowInsets();
            });
        } else {
            decorView.setOnApplyWindowInsetsListener(null);
        }
        ViewCompat.requestApplyInsets(decorView);
    }

    public static void setRenderContentInShortEdgeCutoutAreas(Window window, boolean shortEdges) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            if (shortEdges) {
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            }
            window.setAttributes(layoutParams);
        }
    }

    public static void setStatusBarColor(final Window window, int color, boolean animated) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }
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

    public static int getStatusBarColor(final Window window) {
        return window.getStatusBarColor();
    }

    public static void setStatusBarStyle(Window window, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int systemUi = decorView.getSystemUiVisibility();
            if (dark) {
                systemUi |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(systemUi);
        }
    }

    public static boolean isStatusBarStyleDark(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0;
        }
        return false;
    }

    public static void setStatusBarHidden(Window window, boolean hidden) {
        View decorView = window.getDecorView();
        int systemUi = decorView.getSystemUiVisibility();
        if (hidden) {
            systemUi |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            systemUi |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        } else {
            systemUi &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            systemUi &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        window.getDecorView().setSystemUiVisibility(systemUi);
    }

    public static boolean isStatusBarHidden(Window window) {
        return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
    }

    public static void appendStatusBarPadding(Context context, View view) {
        if (view != null) {
            int statusBarHeight = getStatusBarHeight(context);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp != null && lp.height > 0) {
                lp.height += statusBarHeight;
            }
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + statusBarHeight,
                    view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    public static void removeStatusBarPadding(Context context, View view) {
        if (view != null) {
            int statusBarHeight = getStatusBarHeight(context);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp != null && lp.height > 0) {
                lp.height -= statusBarHeight;
            }
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() - statusBarHeight,
                    view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    public static void appendStatusBarMargin(Context context, View view) {
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
        if (windowInsets != null) {
            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
            return displayCutout != null;
        } else {
            throw new IllegalStateException("activity has not yet attach to window, you must call `isCutout` after `Activity#onAttachedToWindow` is called.");
        }
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

    public static void setNavigationBarColor(final Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setNavigationBarContrastEnforced(false);
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(color);
        }
    }

    public static void setNavigationBarStyle(Window window, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = window.getDecorView();
            int systemUi = decorView.getSystemUiVisibility();
            if (dark) {
                systemUi |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decorView.setSystemUiVisibility(systemUi);
        }
    }

    public static boolean isNavigationBarStyleDark(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) != 0;
        }
        return false;
    }

    public static void setNavigationBarHidden(Window window, boolean hidden) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int systemUi = decorView.getSystemUiVisibility();
            if (hidden) {
                systemUi |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                systemUi |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                systemUi &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                systemUi &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            window.getDecorView().setSystemUiVisibility(systemUi);
        }
    }

}
