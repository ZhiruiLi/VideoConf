package com.example.zhiruili.videoconf.call.account;

import com.example.zhiruili.videoconf.call.errors.AccountException;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

import io.reactivex.Single;

public enum TlsSigner implements ISigner {

    INSTANCE;

    @Override
    public Single<String> fetchSign(final String userName, final String password) {

        return Single
                .create(source -> ILiveLoginManager.getInstance()
                        .tlsLogin(userName, password, new ILiveCallBack<String>() {
                            @Override
                            public void onSuccess(String sig) {
                                source.onSuccess(sig);
                            }

                            @Override
                            public void onError(String module, int errCode, String errMsg) {
                                source.onError(new AccountException("Login failed", module, errCode, errMsg));
                            }
                        }));
    }

    @Override
    public Single<String> register(String userName, String password) {

        return Single
                .create(source -> ILiveLoginManager.getInstance()
                .tlsRegister(userName, password, new ILiveCallBack() {

                    @Override
                    public void onSuccess(Object data) {
                        source.onSuccess(userName);
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        source.onError(new AccountException("Register fail", module, errCode, errMsg));
                    }
                }));
    }
}
