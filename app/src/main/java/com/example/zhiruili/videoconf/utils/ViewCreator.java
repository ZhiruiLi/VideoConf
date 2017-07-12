package com.example.zhiruili.videoconf.utils;

import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.example.zhiruili.videoconf.R;

public final class ViewCreator {

    public static AppCompatButton createTag(Context context, String text, String tag, View.OnClickListener listener) {
        Context wrapper = new ContextThemeWrapper(context, R.style.Base_Widget_AppCompat_Button_NoAllCaps);
        AppCompatButton tagView = new AppCompatButton(wrapper, null, 0);
        tagView.setText(text);
        tagView.setTag(tag);
        tagView.setOnClickListener(listener);
        return tagView;
    }

    private static int scaleDp(Context context, int sizeInDp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInDp * scale + 0.5f);
    }

}
