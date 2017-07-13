package com.example.zhiruili.videoconf;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.av.sdk.AVView;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVCallOption;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.ilivesdk.view.AVVideoView;

import java.util.ArrayList;

public final class CallActivity
        extends AppCompatActivity
        implements CallFragment.OnFragmentInteractionListener, ILiveCallBack, ILVCallListener {

    private static final String TAG = CallActivity.class.getSimpleName();
    private AVRootView mAvRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_call);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Intent intent = getIntent();
        final int callId = intent.getIntExtra(getString(R.string.intent_extra_call_id), -1);
        final String sponsor = intent.getStringExtra(getString(R.string.intent_extra_sponsor));
        final ArrayList<String> members = intent.getStringArrayListExtra(getString(R.string.intent_extra_members));
        if (callId == -1 || sponsor == null || members == null) {
            throw new IllegalArgumentException("should have intent extra of call_id, sponsor and members");
        }
        CallFragment fragment = CallFragment.newInstance(callId, sponsor, members);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.call_main_container, fragment)
                .commit();
        if (callId == 0) {
            ILVCallManager.getInstance().makeMutiCall(members, new ILVCallOption(sponsor), this);
        } else {
            ILVCallManager.getInstance().acceptCall(callId, new ILVCallOption(sponsor));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILVCallManager.getInstance().addCallListener(this);
        ILVCallManager.getInstance().onResume();
    }

    @Override
    public void onActionEndCall(int callId, String sponsor, ArrayList<String> members) {
        finish();
    }

    @Override
    public void onCreateAvRootView(AVRootView avView) {

        mAvRootView = avView;
        ILVCallManager.getInstance().initAvView(mAvRootView);
    }

    @Override
    public void onActionSwitchCamera(boolean enableCamera) {
        ILVCallManager.getInstance().enableCamera(ILiveConstants.FRONT_CAMERA, enableCamera);
        if (!enableCamera) {
            mAvRootView.closeUserView(ILiveLoginManager.getInstance().getMyUserId(), AVView.VIDEO_SRC_TYPE_CAMERA, true);
        }
    }

    @Override
    protected void onPause() {
        ILVCallManager.getInstance().onPause();
        ILVCallManager.getInstance().removeCallListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ILVCallManager.getInstance().onDestory();
        super.onDestroy();
    }

    // ILiveCallBack
    @Override
    public void onSuccess(Object data) {
        Log.d(TAG, "call success");
        finish();
    }

    // ILiveCallBack
    @Override
    public void onError(String module, int errCode, String errMsg) {
        Log.d(TAG, "call error, module: " + module + ", errorCode: " + errCode + ", errorMessage: " + errMsg);
        finish();
    }

    @Override
    public void onCallEstablish(int callId) {

    }

    @Override
    public void onCallEnd(int callId, int endResult, String endInfo) {
        Log.d(TAG, "onCallEnd, callId: " + callId + ", endResult: " + endResult + ", " + endInfo);
        ILVCallManager.getInstance().endCall(callId);
        finish();
    }

    @Override
    public void onException(int iExceptionId, int errCode, String errMsg) {
        finish();
    }
}
