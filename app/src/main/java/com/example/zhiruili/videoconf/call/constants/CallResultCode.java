package com.example.zhiruili.videoconf.call.constants;

import com.tencent.callsdk.ILVCallConstants;

public final class CallResultCode {

    private CallResultCode() { }

    public static final int SPONSOR_CANCEL = ILVCallConstants.ERR_CALL_SPONSOR_CANCEL;
    public static final int SPONSOR_TIMEOUT = ILVCallConstants.ERR_CALL_SPONSOR_TIMEOUT;
    public static final int RESPONDER_REFUSE = ILVCallConstants.ERR_CALL_RESPONDER_REFUSE;
    public static final int HANGUP = ILVCallConstants.ERR_CALL_HANGUP;
    public static final int RESPONDER_LINEBUSY = ILVCallConstants.ERR_CALL_RESPONDER_LINEBUSY;
    public static final int DISCONNECT = ILVCallConstants.ERR_CALL_DISCONNECT;
    public static final int NOT_EXIST = ILVCallConstants.ERR_CALL_NOT_EXIST;
    public static final int FAILED = ILVCallConstants.ERR_CALL_FAILED;
    public static final int LOCAL_CANCEL = ILVCallConstants.ERR_CALL_LOCAL_CANCEL;
}
