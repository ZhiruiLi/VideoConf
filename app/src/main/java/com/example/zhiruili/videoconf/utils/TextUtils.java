package com.example.zhiruili.videoconf.utils;

public final class TextUtils {

    public static boolean isUserNameValid(String userName) {
        return userName.length() >= 4 &&
                userName.length() <= 24 &&
                userName.matches("^[A-Za-z0-9]*[A-Za-z][A-Za-z0-9]*$");
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 8 && password.length() <= 16 && password.matches("^[A-Za-z0-9]*$");
    }
}
