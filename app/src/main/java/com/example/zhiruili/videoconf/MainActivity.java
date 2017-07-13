package com.example.zhiruili.videoconf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.zhiruili.videoconf.account.ILiveHelper;
import com.tencent.callsdk.ILVCallConfig;
import com.tencent.callsdk.ILVCallConstants;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVCallNotification;
import com.tencent.callsdk.ILVCallNotificationListener;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;
import com.tencent.ilivesdk.core.ILiveLoginManager;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 应用主界面
 */
public final class MainActivity
        extends AppCompatActivity
        implements
        ToCallBufferFragment.OnFragmentInteractionListener,
        ILVCallNotificationListener, ILVIncomingListener, ILVCallListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewGroup mMainContainer;

    // 是否登录
    private boolean mHasLogin = false;
    // 当前导航 tab id
    private int mCurrNavItemId = -1;
    // 导航 tab 点击事件监听器
    private BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener = item -> {

        Log.d(TAG, "Select item " + item.getItemId());
        int itemId = item.getItemId();
        if (itemId == mCurrNavItemId) {
            return false;
        }
        if (itemId == R.id.navigation_recent_calls) {

        } else if (itemId == R.id.navigation_contacts) {

        } else {
            return false;
        }
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        if (ILiveHelper.initSdk(getApplicationContext(), AppConstants.APP_ID, AppConstants.ACCOUNT_TYPE)) {
            Log.v(TAG, "Init iLive SDK success");
        } else {
            Log.v(TAG, "Call init multi times");
        }
        Intent intent = getIntent();
        // 从 LoginActivity 登录后跳转而来
        mHasLogin = intent.getBooleanExtra(getString(R.string.intent_extra_has_login), false);
        if (mHasLogin) {
            Log.v(TAG, "User has login");
            initViews();
            initCallSdk();
            return;
        }
        // 确认是否有登录信息
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
                                Log.v(TAG, "login success");
                                mHasLogin = true;
                                initViews();
                                initCallSdk();
                            },
                            err -> {
                                Log.e(TAG, "login fail", err);
                                err.printStackTrace();
                                Snackbar.make(mMainContainer, getString(R.string.label_login_fail), Snackbar.LENGTH_SHORT).show();
                                gotoLogin();
                            });
        }
    }

    private void initViews() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_recent_calls);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCallSdkListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterCallSdkListeners();
    }

    private void initCallSdk() {
        ILVCallManager
                .getInstance()
                .init(new ILVCallConfig()
                        .setNotificationListener(this)
                        .setAutoBusy(true));
    }

    private void registerCallSdkListeners() {
        ILVCallManager.getInstance().addIncomingListener(this);
        ILVCallManager.getInstance().addCallListener(this);
    }

    private void unregisterCallSdkListeners() {
        ILVCallManager.getInstance().removeCallListener(this);
        ILVCallManager.getInstance().removeIncomingListener(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            default:
                return false;
        }
    }

    /**
     * 注销并跳转到登录页
     */
    private void logout() {
        ILiveHelper
                .logout()
                .map(_ignore -> getSharedPreferences(getString(R.string.shared_pref_id_user_account), MODE_PRIVATE)
                        .edit()
                        .remove(getString(R.string.shared_pref_key_user_name))
                        .remove(getString(R.string.shared_pref_key_password))
                        .remove(getString(R.string.shared_pref_key_signature))
                        .commit())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        _ignore -> {
                            Log.v(TAG, "logout success");
                            Snackbar.make(mMainContainer, getString(R.string.label_logout_success), Snackbar.LENGTH_SHORT).show();
                            gotoLogin();
                        },
                        err -> {
                            Log.e(TAG, "logout error", err);
                            err.printStackTrace();
                            gotoLogin();
                        }
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHasLogin) {
            ILiveHelper
                    .logout()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            _ignore -> Log.v(TAG, "logout success"),
                            err -> {
                                Log.e(TAG, "logout error", err);
                                err.printStackTrace();
                            }
                    );
            mHasLogin = false;
        }
    }

    @Override
    public void onStartCalling(ArrayList<String> calledIds) {
        gotoCall(ILiveLoginManager.getInstance().getMyUserId(), 0, ILVCallConstants.CALL_TYPE_VIDEO, calledIds);
    }

    @Override
    public void onRecvNotification(int callId, ILVCallNotification notification) {
        Log.d(TAG, "onRecvNotification, callId: " + callId);
    }

    @Override
    public void onNewIncomingCall(int callId, int callType, ILVIncomingNotification notification) {
        Log.d(TAG, "onNewIncomingCall, callId: " + callId + ", callType: " + callType);
        Log.d(TAG, "onNewIncomingCall, sponsorId: " + notification.getSponsorId() + ", sender: " + notification.getSender() + ", members: " + notification.getMembersString());
        final String membersMsg;
        if (notification.getMembersString() == null) {
            membersMsg = "";
        } else {
            membersMsg = "\n其他参与者：" + notification.getMembersString();
        }
        new AlertDialog.Builder(this)
                .setTitle("新会议请求")
                .setMessage("来电发起人：" + notification.getSponsorId() + membersMsg)
                .setNegativeButton("拒绝", null)
                .setPositiveButton("接受", (dialog, which) -> {
                    final ArrayList<String> members = new ArrayList<>();
                    if (notification.getMembers() != null) {
                        members.addAll(notification.getMembers());
                    }
                    gotoCall(notification.getSponsorId(), callId, callType, members);
                })
                .show();
    }

    private void gotoCall(String sponsor, int callId, int callType, ArrayList<String> members) {
        Intent intent = new Intent();
        intent.setClass(this, CallActivity.class);
        intent.putExtra(getString(R.string.intent_extra_sponsor), sponsor);
        intent.putExtra(getString(R.string.intent_extra_call_id), callId);
        intent.putExtra(getString(R.string.intent_extra_call_type), callType);
        intent.putExtra(getString(R.string.intent_extra_members), members);
        startActivity(intent);
    }

    @Override
    public void onCallEstablish(int callId) {
        Log.d(TAG, "onCallEstablish, callId: " + callId);
    }

    @Override
    public void onCallEnd(int callId, int endResult, String endInfo) {
        Log.d(TAG, "onCallEnd, callId: " + callId + ", endResult: " + endResult);
    }

    @Override
    public void onException(int iExceptionId, int errCode, String errMsg) {
        Log.d(TAG, "onException, exceptionId: " + iExceptionId + ", errorCode: " + errCode + ", errMsg: " + errMsg);
    }
}
