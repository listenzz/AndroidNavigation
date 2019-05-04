package com.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import me.listenzz.navigation.AwesomeActivity;

/**
 * Created by Listen on 2018/2/9.
 */

public class SplashActivity extends AwesomeActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
        }, 1500);
    }
}
