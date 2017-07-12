package com.example.zhiruili.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.EditText;

public final class ViewUtils {

    private ViewUtils() { }

    public static void animShowOrHideView(View view, boolean show, int animTime) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
        view.animate()
                .setDuration(animTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    public static void putError(EditText view, String message) {
        view.setError(message);
        view.requestFocus();
    }

    public static void clearErrors(EditText ... views) {
        for (EditText v : views) {
            v.setError(null);
        }
    }
}
