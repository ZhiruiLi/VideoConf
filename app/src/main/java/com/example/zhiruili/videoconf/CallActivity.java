package com.example.zhiruili.videoconf;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.zhiruili.videoconf.call.constants.CallResultCode;
import com.tencent.av.sdk.AVView;
import com.tencent.callsdk.ILVCallConstants;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVCallOption;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.view.AVRootView;

import java.util.ArrayList;

public final class CallActivity
        extends AppCompatActivity
        implements CallFragment.OnFragmentInteractionListener, ILVCallListener {

    private static final String TAG = CallActivity.class.getSimpleName();
    private AVRootView mAvRootView;
    private int mCallId;

    public static final class IntentExtras {
        public static final class Request {
            public static final String CALL_ID = "call_id";
            public static final String CALL_TYPE = "call_type";
            public static final String SPONSOR = "sponsor";
            public static final String MEMBERS = "members";
        }

        public static final class Result {
            public static final String MESSAGE = "message";
        }
    }

    public static Intent createIntent(Context context, int callId, int callType, String sponsor, ArrayList<String> members) {
        return new Intent()
                .setClass(context, CallActivity.class)
                .putExtra(IntentExtras.Request.CALL_ID, callId)
                .putExtra(IntentExtras.Request.CALL_TYPE, callType)
                .putExtra(IntentExtras.Request.SPONSOR, sponsor)
                .putExtra(IntentExtras.Request.MEMBERS, members);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_call);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Intent intent = getIntent();
        mCallId = intent.getIntExtra(IntentExtras.Request.CALL_ID, -1);
        final int callType = intent.getIntExtra(IntentExtras.Request.CALL_TYPE, -1);
        final String sponsor = intent.getStringExtra(IntentExtras.Request.SPONSOR);
        final ArrayList<String> members = intent.getStringArrayListExtra(IntentExtras.Request.MEMBERS);
        if (mCallId == -1 || callType == -1 || sponsor == null || members == null) {
            throw new IllegalArgumentException("should have intent extra of call_id, sponsor and members");
        }

        CallFragment fragment = CallFragment.newInstance(mCallId, sponsor, members);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.call_main_container, fragment)
                .commit();

        ILVCallOption option = new ILVCallOption(sponsor);
        if (mCallId == 0) {

            ILVCallManager.getInstance().makeMutiCall(members, option.setCallType(ILVCallConstants.CALL_TYPE_VIDEO), new ILiveCallBack() {

                @Override
                public void onSuccess(Object data) {
                    Log.d(TAG, "call success");
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Log.d(TAG, "call error, module: " + module + ", errorCode: " + errCode + ", errorMessage: " + errMsg);
                    finish();
                }
            });
        } else {
            ILVCallManager.getInstance().acceptCall(mCallId, option.setCallType(callType));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILVCallManager.getInstance().onResume();
        ILVCallManager.getInstance().addCallListener(this);
    }

    @Override
    public void onActionEndCall(int callId, String sponsor, ArrayList<String> members) {
        Log.d(TAG, "onActionEndCall, callId: " + callId);
        // TODO: 这里如果我调用 `ILVCallManager.getInstance().endCall(mCallId)` 是不会触发 `onCallEnd` 的，这是为什么？
        endCall();
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
        ILVCallManager.getInstance().removeCallListener(this);
        ILVCallManager.getInstance().onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ILVCallManager.getInstance().onDestory();
        super.onDestroy();
    }

    @Override
    public void onCallEstablish(int callId) {
        Log.d(TAG, "call establish, callId: " + callId);
    }

    @Override
    public void onCallEnd(int callId, int endResult, String endInfo) {
        Log.d(TAG, "onCallEnd, callId: " + callId + ", endResult: " + endResult + ", " + endInfo);
        setResult(endResult, new Intent().putExtra(IntentExtras.Result.MESSAGE, endInfo));
        finish();
    }

    @Override
    public void onException(int iExceptionId, int errCode, String errMsg) {
        Log.d(TAG, "onException, exceptionId: " + iExceptionId + ", errorCode: " + errCode + ", errorMessage: " + errMsg);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPress");
        endCall();
    }

    private void endCall() {
        ILVCallManager.getInstance().endCall(mCallId);
        setResult(CallResultCode.LOCAL_CANCEL, new Intent().putExtra(IntentExtras.Result.MESSAGE, "结束通话"));
        finish();
    }
}
