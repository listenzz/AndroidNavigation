package com.navigation.androidx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by Listen on 2017/12/26.
 */

public class DrawableUtils {

    private static final String TAG = "Navigation";

    @Nullable
    public static Drawable fromUri(@NonNull Context context, @NonNull String uri) {
        Drawable drawable = null;
        if (uri.startsWith("http")) {
            try {
                StrictMode.ThreadPolicy threadPolicy = StrictMode.getThreadPolicy();
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

                URL url = new URL(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                drawable = new BitmapDrawable(context.getResources(), bitmap);

                StrictMode.setThreadPolicy(threadPolicy);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else if (uri.startsWith("file")) {
            Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(uri).getPath());
            drawable = new BitmapDrawable(context.getResources(), bitmap);
        } else if (uri.startsWith("font")) {
            String filepath = filepathFromFont(context, uri);
            if (filepath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(filepath).getPath());
                drawable = new BitmapDrawable(context.getResources(), bitmap);
            }
        } else {
            int resId = fromResourceDrawableId(context, uri);
            drawable = resId > 0 ? ContextCompat.getDrawable(context, resId) : null;
        }
        return drawable;
    }

    public static int fromResourceDrawableId(@NonNull Context context, @Nullable String name) {
        if (name == null || name.isEmpty()) {
            return 0;
        }
        return context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());
    }

    @Nullable
    public static String filepathFromFont(@NonNull Context context, @NonNull String fontUri) {
        Uri u = Uri.parse(fontUri);
        String fontFamily = u.getHost();
        List<String> fragments = u.getPathSegments();
        if (fontFamily == null || fragments.size() < 2) {
            throw new IllegalArgumentException("font uri 格式不对。");
        }
        String glyph = fragments.get(0);
        Integer fontSize = Integer.valueOf(fragments.get(1));

        int color = Color.WHITE;

        if (fragments.size() == 3) {
            String hex = fragments.get(2);
            color = Color.parseColor("#" + hex);
        }
        return filepathFromFont(context, fontFamily, glyph, fontSize, color);
    }

    @Nullable
    public static String filepathFromFont(@NonNull Context context, @NonNull String fontFamily, @NonNull String glyph, @NonNull Integer fontSize, @NonNull Integer color) {
        File cacheFolder = context.getCacheDir();
        String cacheFolderPath = cacheFolder.getAbsolutePath() + "/";

        float scale = context.getResources().getDisplayMetrics().density;
        String scaleSuffix = "@" + (scale == (int) scale ? Integer.toString((int) scale) : Float.toString(scale)) + "x";
        int size = Math.round(fontSize * scale);
        String cacheKey = fontFamily + ":" + glyph + ":" + color;
        String hash = Integer.toString(cacheKey.hashCode(), 32);
        String cacheFilePath = cacheFolderPath + hash + "_" + fontSize + scaleSuffix + ".png";
        String cacheFileUrl = "file://" + cacheFilePath;
        File cacheFile = new File(cacheFilePath);

        if (cacheFile.exists()) {
            return cacheFileUrl;
        } else {
            FileOutputStream fos = null;
            Typeface typeface = FontManager.getInstance().getTypeface(fontFamily, 0, context.getAssets());
            Paint paint = new Paint();
            paint.setTypeface(typeface);
            paint.setColor(color);
            paint.setTextSize(size);
            paint.setAntiAlias(true);
            Rect textBounds = new Rect();
            paint.getTextBounds(glyph, 0, glyph.length(), textBounds);

            Bitmap bitmap = Bitmap.createBitmap(textBounds.width(), textBounds.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(glyph, -textBounds.left, -textBounds.top, paint);

            try {
                fos = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                fos = null;
                return cacheFileUrl;
            } catch (IOException e) {
                Log.e(TAG, "", e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
