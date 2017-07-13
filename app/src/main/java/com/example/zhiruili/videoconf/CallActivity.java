package com.example.zhiruili.videoconf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public final class CallActivity
        extends AppCompatActivity
        implements CallFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
    }
}
