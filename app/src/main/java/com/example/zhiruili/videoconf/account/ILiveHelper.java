package com.example.zhiruili.videoconf.account;

import android.content.Context;

import com.example.zhiruili.videoconf.account.errors.AccountException;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;

import io.reactivex.Single;

/**
 * iLive SDK 操作的辅助类
 */
public final class ILiveHelper {

    private static boolean sHasInit = false;

    private ILiveHelper() { }

    /**
     * 初始化 iLive SDK
     * @param appCtx        Application Context
     * @param appId         App ID 由官方发放
     * @param accountType   用户类型
     * @return  是否为首次初始化
     */
    public synchronized static boolean initSdk(Context appCtx, int appId, int accountType) {
        if (sHasInit) {
            return false;
        } else {
            ILiveSDK.getInstance().initSdk(appCtx, appId, accountType);
            sHasInit = true;
            return true;
        }
    }

    /**
     * 登录
     * @param userName  用户名
     * @param password  密码
     * @param signer    签名获取器
     * @return  返回 Single 对象，成功时向监听者传递签名，错误时向监听者传递 AccountException 对象
     */
    public static Single<String> login(final String userName, final String password, final ISigner signer) {
        return signer
                .fetchSign(userName, password)
                .flatMap(sig -> loginBySign(userName, sig));
    }

    /**
     * 登录
     * @param userName  用户名
     * @param userSig   用户签名
     * @return  返回 Single 对象，成功时向监听者传递签名，错误时向监听者传递 AccountException 对象
     */
    public static Single<String> loginBySign(final String userName, final String userSig){

        ILiveLoginManager manager = ILiveLoginManager.getInstance();

        return Single.create(source -> manager
                .iLiveLogin(userName, userSig, new ILiveCallBack() {

                    @Override
                    public void onSuccess(Object data) {
                        source.onSuccess(userSig);
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        source.onError(new AccountException("Login failed", module, errCode, errMsg));
                    }
                }));
    }
}
