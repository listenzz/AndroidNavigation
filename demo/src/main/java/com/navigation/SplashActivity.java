package com.navigation;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.navigation.androidx.AwesomeActivity;

public class SplashActivity extends AwesomeActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                splashScreenView.remove();
                getSplashScreen().clearOnExitAnimationListener();
            });
        }

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
        }, 1500);
    }
}
