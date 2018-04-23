package me.listenzz.navigation;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.TypedValue;
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

    private AppUtils() {
    }

    private static boolean sIsMiuiV6;

    static {
        try {
            Class<?> sysClass = Class.forName("android.os.SystemProperties");
            Method getStringMethod = sysClass.getDeclaredMethod("get", String.class);
            String version = (String) getStringMethod.invoke(sysClass, "ro.miui.ui.version.name");
            if (!TextUtils.isEmpty(version)) {
                try {
                    int num = Integer.valueOf(version.substring(1));
                    sIsMiuiV6 = num >= 6;
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMiuiV6() {
        return sIsMiuiV6;
    }

    /**
     * 判断颜色是否偏黑色
     *
     * @param color 颜色
     * @param level 级别
     * @return
     */
    public static boolean isBlackColor(int color, int level) {
        int grey = toGrey(color);
        return grey < level;
    }

    /**
     * 颜色转换成灰度值
     *
     * @param rgb 颜色
     * @return　灰度值
     */
    public static int toGrey(int rgb) {
        int blue = rgb & 0x000000FF;
        int green = (rgb & 0x0000FF00) >> 8;
        int red = (rgb & 0x00FF0000) >> 16;
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }


    /**
     * @param context used to get system services
     * @return screenWidth in pixels
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        return size.x;
    }

    /**
     * This method can be extended to get all android attributes color, string, dimension ...etc
     *
     * @param context          used to fetch android attribute
     * @param androidAttribute attribute codes like R.attr.colorAccent
     * @return in this case color of android attribute
     */
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


    /**
     * @param context used to fetch display metrics
     * @param dp      dp value
     * @return pixel value
     */
    public static int dp2px(Context context, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return (int)px;
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

    public static void setStatusBarColor(final Window window, int color, boolean animated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (animated) {
                int curColor = window.getStatusBarColor();
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
                if (drawable != null && drawable instanceof ColorDrawable) {
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
            decorView.setSystemUiVisibility(
                    dark ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0);
        }
    }

    public static void setStatusBarHidden(Window window, boolean hidden) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (hidden) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
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


}
