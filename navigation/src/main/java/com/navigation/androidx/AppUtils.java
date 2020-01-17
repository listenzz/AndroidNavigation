package com.navigation.androidx;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.lang.reflect.Method;

/**
 * Created by listen on 2018/2/3.
 */

public class AppUtils {

    private static final String TAG = "Navigation";

    private AppUtils() {
    }

    public static boolean isBlackColor(int color, int level) {
        int grey = toGrey(color);
        return grey < level;
    }

    public static int toGrey(int rgb) {
        int blue = rgb & 0x000000FF;
        int green = (rgb & 0x0000FF00) >> 8;
        int red = (rgb & 0x00FF0000) >> 16;
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    public static String colorToString(@ColorInt int color) {
        return String.format("#%08X", color);
    }

    public static Bitmap createBitmapFromView(View view) {
        view.clearFocus();
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        if (bitmap != null) {
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            canvas.save();
            canvas.drawColor(Color.TRANSPARENT);
            view.draw(canvas);
            canvas.restore();
            canvas.setBitmap(null);
        }
        return bitmap;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static int fetchContextColor(Context context, int androidAttribute) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{androidAttribute});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int fetchContextDimension(Context context, int androidAttribute) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{androidAttribute});
        int dimension = a.getDimensionPixelOffset(0, 0);
        a.recycle();
        return dimension;
    }

    public static int fetchContextResource(Context context, int androidAttribute) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{androidAttribute});
        int resource = a.getResourceId(0, 0);
        a.recycle();
        return resource;
    }

    public static int dp2px(Context context, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static void hideSoftInput(View view) {
        if (view == null || view.getContext() == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private static final String BRAND = Build.BRAND.toLowerCase();

    public static boolean isHuawei() {
        return BRAND.contains("huawei") || BRAND.contains("honor");
    }

    public static boolean isXiaomi() {
        return Build.MANUFACTURER.toLowerCase().equals("xiaomi");
    }

    public static boolean isVivo() {
        return BRAND.contains("vivo") || BRAND.contains("bbk");
    }

    public static boolean isOppo() {
        return BRAND.contains("oppo");
    }

    public static void setStatusBarTranslucent(Window window, boolean translucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setRenderContentInShortEdgeCutoutAreas(window, translucent);

            View decorView = window.getDecorView();
            if (translucent) {
                decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                    WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                    return defaultInsets.replaceSystemWindowInsets(
                            defaultInsets.getSystemWindowInsetLeft(),
                            0,
                            defaultInsets.getSystemWindowInsetRight(),
                            defaultInsets.getSystemWindowInsetBottom());
                });
            } else {
                decorView.setOnApplyWindowInsetsListener(null);
            }

            ViewCompat.requestApplyInsets(decorView);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (translucent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            ViewCompat.requestApplyInsets(window.getDecorView());
        }
    }

    public static boolean shouldAdjustStatusBarColor(@NonNull AwesomeFragment fragment) {
        boolean shouldAdjustForWhiteStatusBar = !AppUtils.isBlackColor(fragment.preferredStatusBarColor(), 176);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            shouldAdjustForWhiteStatusBar = shouldAdjustForWhiteStatusBar && fragment.preferredStatusBarStyle() == BarStyle.LightContent;
        }
        return shouldAdjustForWhiteStatusBar;
    }

    public static void setNavigationBarColor(final Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    public static boolean isDarNavigationBarStyle(Window window) {
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


    public static void setStatusBarColor(final Window window, int color, boolean animated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup decorViewGroup = (ViewGroup) window.getDecorView();
            View statusBarView = decorViewGroup.findViewWithTag("custom_status_bar_tag");
            if (statusBarView == null) {
                statusBarView = new View(window.getContext());
                statusBarView.setTag("custom_status_bar_tag");
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight(window.getContext()));
                params.gravity = Gravity.TOP;
                statusBarView.setLayoutParams(params);
                decorViewGroup.addView(statusBarView);
            }


            if (animated) {
                Drawable drawable = statusBarView.getBackground();
                int curColor = Integer.MAX_VALUE;
                if (drawable instanceof ColorDrawable) {
                    ColorDrawable colorDrawable = (ColorDrawable) drawable;
                    curColor = colorDrawable.getColor();
                }
                if (curColor != Integer.MAX_VALUE) {
                    final View barView = statusBarView;
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, color);
                    colorAnimation.addUpdateListener(
                            animator -> barView.setBackground(new ColorDrawable((Integer) animator.getAnimatedValue())));
                    colorAnimation.setDuration(200).setStartDelay(0);
                    colorAnimation.start();
                } else {
                    statusBarView.setBackground(new ColorDrawable(color));
                }
            } else {
                statusBarView.setBackground(new ColorDrawable(color));
            }
        }
    }

    public static int getStatusBarColor(final Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return window.getStatusBarColor();
        } else {
            ViewGroup decorViewGroup = (ViewGroup) window.getDecorView();
            View statusBarView = decorViewGroup.findViewWithTag("custom_status_bar_tag");
            if (statusBarView != null) {
                Drawable drawable = statusBarView.getBackground();
                if (drawable instanceof ColorDrawable) {
                    ColorDrawable colorDrawable = (ColorDrawable) drawable;
                    return colorDrawable.getColor();
                }
            }
        }
        return Color.BLACK;
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

    public static boolean isDarkStatusBarStyle(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0;
        }
        return false;
    }

    public static void setStatusBarHidden(Window window, boolean hidden) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ViewGroup decorViewGroup = (ViewGroup) window.getDecorView();
            final View statusBarView = decorViewGroup.findViewWithTag("custom_status_bar_tag");
            if (statusBarView != null) {
                boolean hiding = statusBarView.isClickable();
                if (hiding == hidden) {
                    return;
                }

                if (hidden) {
                    statusBarView.setClickable(true);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(statusBarView, "y", -getStatusBarHeight(window.getContext()));
                    animator.setDuration(200);
                    animator.setStartDelay(200);
                    animator.start();
                } else {
                    statusBarView.setClickable(false);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(statusBarView, "y", 0);
                    animator.setDuration(200);
                    animator.start();
                }
            }
        }
    }

    public static boolean isStatusBarHidden(Window window) {
        return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
    }

    public static void appendStatusBarPadding(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (view != null) {
                int statusBarHeight = getStatusBarHeight(context);
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if (lp != null && lp.height > 0) {
                    lp.height += statusBarHeight;//增高
                }
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + statusBarHeight,
                        view.getPaddingRight(), view.getPaddingBottom());
            }
        }
    }


    public static void removeStatusBarPadding(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (view != null) {
                int statusBarHeight = getStatusBarHeight(context);
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if (lp != null && lp.height > 0) {
                    lp.height -= statusBarHeight;//增高
                }
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop() - statusBarHeight,
                        view.getPaddingRight(), view.getPaddingBottom());
            }
        }
    }

    public static void appendStatusBarMargin(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) lp).topMargin += getStatusBarHeight(context);//增高
            }
            view.setLayoutParams(lp);
        }
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
        if (sHasCheckCutout) {
            return sIsCutout;
        }

        sHasCheckCutout = true;

        // 低于 API 27 的，都不会是刘海屏、凹凸屏
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            sIsCutout = false;
            return false;
        }

        sIsCutout = isHuaweiCutout(activity) || isOppoCutout(activity) || isVivoCutout(activity) || isXiaomiCutout(activity);

        if (!isGoogleCutoutSupport()) {
            return sIsCutout;
        }

        if (!sIsCutout) {
            Window window = activity.getWindow();
            if (window == null) {
                throw new IllegalStateException("activity has not attach to window");
            }
            View decorView = window.getDecorView();
            sIsCutout = attachHasOfficialNotch(decorView);
        }

        return sIsCutout;
    }


    @TargetApi(28)
    private static boolean attachHasOfficialNotch(View view) {
        WindowInsets windowInsets = view.getRootWindowInsets();
        if (windowInsets != null) {
            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
            return displayCutout != null;
        } else {
            throw new IllegalStateException("activity has not yet attach to window, you must call `isCutout` after `Activity#onAttachedToWindow` is called.");
        }
    }

    public static boolean isHuaweiCutout(Context context) {
        if (!isHuawei()) {
            return false;
        }

        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (Exception e) {
            // ignore
        }
        return ret;
    }

    public static boolean isOppoCutout(Context context) {
        if (!isOppo()) {
            return false;
        }
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    public static final int NOTCH_IN_SCREEN_VOIO = 0x00000020;//是否有凹槽
    public static final int ROUNDED_IN_SCREEN_VOIO = 0x00000008;//是否有圆角

    public static boolean isVivoCutout(Context context) {
        if (!isVivo()) {
            return false;
        }

        boolean ret = false;

        try {
            ClassLoader cl = context.getClassLoader();
            Class ftFeature = cl.loadClass("android.util.FtFeature");
            Method[] methods = ftFeature.getDeclaredMethods();
            if (methods != null) {
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getName().equalsIgnoreCase("isFeatureSupport")) {
                        ret = (boolean) method.invoke(ftFeature, NOTCH_IN_SCREEN_VOIO);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return ret;
    }

    private static final String MIUI_NOTCH = "ro.miui.notch";

    @SuppressLint("PrivateApi")
    public static boolean isXiaomiCutout(Context context) {
        if (!isXiaomi()) {
            return false;
        }
        try {
            Class spClass = Class.forName("android.os.SystemProperties");
            Method getMethod = spClass.getDeclaredMethod("getInt", String.class, int.class);
            getMethod.setAccessible(true);
            int hasNotch = (int) getMethod.invoke(null, MIUI_NOTCH, 0);
            return hasNotch == 1;
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    public static boolean isGoogleCutoutSupport() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public static void setRenderContentInShortEdgeCutoutAreas(Window window, boolean shortEdges) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            if (shortEdges) {
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            }
            window.setAttributes(layoutParams);
        }
    }

}
