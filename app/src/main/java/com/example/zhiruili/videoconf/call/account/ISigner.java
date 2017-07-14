package com.example.zhiruili.videoconf.call.account;

import io.reactivex.Single;

/**
 * 用户账户签名管理接口
 */
public interface ISigner {

    /**
     * 获取签名
     * @param userName  用户名
     * @param password  密码
     * @return  返回 Single 对象，成功时向监听者传递签名，错误时向监听者传递 AccountException 对象
     */
    Single<String> fetchSign(String userName, String password);

    /**
     * 注册新用户
     * @param userName  用户名
     * @param password  密码
     * @return  返回 Single 对象，成功时向监听者传递用户名，错误时向监听者传递 AccountException 对象
     */
    Single<String> register(String userName, String password);
}
