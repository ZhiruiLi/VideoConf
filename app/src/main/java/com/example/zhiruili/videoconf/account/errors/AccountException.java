package com.example.zhiruili.videoconf.account.errors;

import android.text.TextUtils;

public final class AccountException extends RuntimeException {

    private final String mModule;
    private final int mErrorCode;
    private final String mErrorMessage;
    private final String mPrefixMessage;

    public AccountException(String module, int errorCode, String errorMessage) {
        this("", module, errorCode, errorMessage);
    }

    public AccountException(String prefixMessage, String module, int errorCode, String errorMessage) {
        mPrefixMessage = prefixMessage != null ? prefixMessage : "";
        mModule = module != null ? module : "";
        mErrorCode = errorCode;
        mErrorMessage = errorMessage != null ? errorMessage : "";
    }

    @Override
    public String getMessage() {
        return !TextUtils.isEmpty(mErrorMessage) ? mErrorMessage : super.getMessage();
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    @Override
    public String toString() {
        return (TextUtils.isEmpty(mPrefixMessage) ? "" : mPrefixMessage + " | ") +
                "Error module: " + mModule +
                " | Error code: " + mErrorCode +
                " | Error message: " + mErrorMessage;
    }
}
