package me.listenzz.navigation;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
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

    public static void setStatusBarTranslucent(Window window, boolean translucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setRenderContentInShortEdgeCutoutAreas(window, translucent);

            View decorView = window.getDecorView();
            if (translucent) {
                decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                        return defaultInsets.replaceSystemWindowInsets(
                                defaultInsets.getSystemWindowInsetLeft(),
                                0,
                                defaultInsets.getSystemWindowInsetRight(),
                                defaultInsets.getSystemWindowInsetBottom());
                    }
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

    public static void setNavigationBarColor(final Window window, int color, boolean animated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int curColor = window.getNavigationBarColor();
            if (curColor == color) {
                return;
            }

            setNavigationBarStyle(window, !isBlackColor(color, 176));

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (animated) {
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, color);
                colorAnimation.addUpdateListener(
                        new ValueAnimator.AnimatorUpdateListener() {
                            @TargetApi(21)
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                window.setNavigationBarColor((Integer) animator.getAnimatedValue());
                            }
                        });
                colorAnimation.setDuration(200).setStartDelay(0);
                colorAnimation.start();
            } else {
                window.setNavigationBarColor(color);
            }
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
            int curColor = window.getStatusBarColor();
            if (curColor == color) {
                return;
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (animated) {
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, color);
                colorAnimation.addUpdateListener(
                        new ValueAnimator.AnimatorUpdateListener() {
                            @TargetApi(21)
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                            }
                        });
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
                            new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animator) {
                                    barView.setBackground(new ColorDrawable((Integer) animator.getAnimatedValue()));
                                }
                            });
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

    public static void appendStatusBarPadding(View view, int viewHeight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (view != null) {
                int statusBarHeight = getStatusBarHeight(view.getContext());
                view.setPadding(view.getPaddingLeft(), statusBarHeight, view.getPaddingRight(), view.getPaddingBottom());
                if (viewHeight > 0) {
                    view.getLayoutParams().height = statusBarHeight + viewHeight;
                } else {
                    view.getLayoutParams().height = viewHeight;
                }
            }
        }
    }

    public static void removeStatusBarPadding(View view, int viewHeight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (view != null) {
                view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(),
                        view.getPaddingBottom());
                view.getLayoutParams().height = viewHeight;
            }
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

        // 低于 API 27 的，都不会是刘海屏、凹凸屏
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return false;
        }

        sHasCheckCutout = true;
        sIsCutout = isHuaweiCutout(activity) || isOppoCutout(activity) || isVivoCutout(activity) || isXiaomiCutout(activity);

        if (!sIsCutout && isGoogleCutoutSupport()) {
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
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    public static final int NOTCH_IN_SCREEN_VOIO = 0x00000020;//是否有凹槽
    public static final int ROUNDED_IN_SCREEN_VOIO = 0x00000008;//是否有圆角

    public static boolean isVivoCutout(Context context) {
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
