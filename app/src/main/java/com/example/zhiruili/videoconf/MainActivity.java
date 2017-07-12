package com.example.zhiruili.videoconf;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.example.zhiruili.videoconf.account.ILiveHelper;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewGroup mMainContainer;

    private boolean mHasLogin = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        Log.d(TAG, "select item " + item.getItemId());
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        if (ILiveHelper.initSdk(getApplicationContext(), AppConstants.APP_ID, AppConstants.ACCOUNT_TYPE)) {
            Log.v(TAG, "Init iLive SDK success");
        } else {
            Log.v(TAG, "Call init multi time");
        }
        SharedPreferences pref = getSharedPreferences(getString(R.string.shared_pref_id_user_account), MODE_PRIVATE);
        String userName = pref.getString(getString(R.string.shared_pref_key_user_name), null);
        String sign = pref.getString(getString(R.string.shared_pref_key_signature), null);
        if (userName == null || sign == null) {
            gotoLogin();
        } else {
            ILiveHelper
                    .loginBySign(userName, sign)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            _ignore -> {
                                BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
                                navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                                navigation.setSelectedItemId(0);
                            },
                            err -> {
                                Log.e(TAG, "login fail", err);
                                err.printStackTrace();
                                Snackbar.make(mMainContainer, getString(R.string.label_login_fail), Snackbar.LENGTH_SHORT).show();
                            });
        }
    }

    private void bindViews() {
        mMainContainer = (ViewGroup) findViewById(R.id.main_container);
    }

    private void gotoLogin() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHasLogin) {
            // TODO: logout
        }
    }
}
