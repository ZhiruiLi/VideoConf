package com.example.zhiruili.videoconf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.example.zhiruili.utils.ViewUtils;
import com.example.zhiruili.videoconf.account.ILiveHelper;
import com.example.zhiruili.videoconf.account.TlsSigner;
import com.example.zhiruili.videoconf.account.errors.AccountException;
import com.example.zhiruili.videoconf.account.errors.ErrorCode;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.example.zhiruili.videoconf.utils.TextUtils.isPasswordValid;
import static com.example.zhiruili.videoconf.utils.TextUtils.isUserNameValid;

/**
 * 注册登录页
 */
public final class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mLoginContainer;

    private Disposable mLoginDisposable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindViews();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginDisposable != null) {
            mLoginDisposable.dispose();
            mLoginDisposable = null;
        }
    }

    private void bindViews() {
        mUserNameView = (EditText) findViewById(R.id.tv_user_name);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mLoginContainer = findViewById(R.id.login_container);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void setupListeners() {
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            } else {
                return false;
            }
        });
        findViewById(R.id.login_button).setOnClickListener(view -> attemptLogin());
        findViewById(R.id.register_button).setOnClickListener(view -> attemptRegister());
    }

    private void attemptLogin() {
        if (mLoginDisposable != null) {
            return;
        }
        ViewUtils.clearErrors(mUserNameView, mPasswordView);
        String userName = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            ViewUtils.putError(mUserNameView, getString(R.string.error_user_name_field_required));
            return;
        } else if (TextUtils.isEmpty(password)) {
            ViewUtils.putError(mPasswordView, getString(R.string.error_password_field_required));
            return;
        }
        showProgress(true);
        mLoginDisposable = ILiveHelper
                .login(userName, password, TlsSigner.INSTANCE)
                .doOnSuccess(sig -> {
                    SharedPreferences.Editor editor =
                            getSharedPreferences(getString(R.string.shared_pref_id_user_account), MODE_PRIVATE).edit();
                    editor.putString(getString(R.string.shared_pref_key_user_name), userName);
                    editor.putString(getString(R.string.shared_pref_key_password), password);
                    editor.putString(getString(R.string.shared_pref_key_signature), sig);
                    editor.apply();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(_ignore -> Snackbar.make(mLoginContainer, getString(R.string.label_login_success), Snackbar.LENGTH_SHORT).show())
                .subscribe(_ignore -> {
                    Log.v(TAG, "login success");
                    showProgress(false);
                    mLoginDisposable = null;
                    gotoMain();
                    finish();
                }, err -> {
                    Log.v(TAG, "login fail");
                    showProgress(false);
                    if (err instanceof AccountException) {
                        AccountException aExp = (AccountException) err;
                        if (aExp.getErrorCode() == ErrorCode.NO_SUCH_USER) {
                            ViewUtils.putError(mUserNameView, getString(R.string.error_no_such_user));
                        } else if (aExp.getErrorCode() == ErrorCode.WRONG_PASSWORD) {
                            ViewUtils.putError(mPasswordView, getString(R.string.error_incorrect_password));
                        } else {
                            Snackbar.make(mLoginContainer, getString(R.string.label_login_fail) + err.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, err.getMessage(), err);
                        Snackbar.make(mLoginContainer, getString(R.string.label_login_fail_unknown) + " : " + err.toString(), Snackbar.LENGTH_SHORT).show();
                    }
                    mLoginDisposable = null;
                });
    }

    private void attemptRegister() {
        if (mLoginDisposable != null) {
            return;
        }
        ViewUtils.clearErrors(mUserNameView, mPasswordView);
        String userName = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(userName) || !isUserNameValid(userName)) {
            ViewUtils.putError(mUserNameView, getString(R.string.error_invalid_user_name));
            return;
        } else if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            ViewUtils.putError(mUserNameView, getString(R.string.error_invalid_password));
            return;
        }
        showProgress(true);
        mLoginDisposable = TlsSigner.INSTANCE
                .register(userName, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        _ignore -> {
                            Log.v(TAG, "register success");
                            Snackbar.make(mLoginContainer, getString(R.string.label_register_success), Snackbar.LENGTH_SHORT).show();
                            showProgress(false);
                            mLoginDisposable = null;
                        },
                        err -> {
                            Log.v(TAG, "register fail");
                            showProgress(false);
                            if (err instanceof AccountException) {
                                if (((AccountException) err).getErrorCode() == ErrorCode.USER_EXISTED) {
                                    ViewUtils.putError(mUserNameView, getString(R.string.error_user_existed));
                                } else {
                                    Snackbar.make(mLoginContainer, getString(R.string.label_register_fail) + err.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, err.getMessage(), err);
                                Snackbar.make(mLoginContainer, getString(R.string.label_register_fail_unknown) + " : " + err.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                            mLoginDisposable = null;
                        }
                );
    }

    private void gotoMain() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.putExtra(getString(R.string.intent_extra_has_login), true);
        startActivity(intent);
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        ViewUtils.animShowOrHideView(mLoginFormView, !show, shortAnimTime);
        ViewUtils.animShowOrHideView(mProgressView, show, shortAnimTime);
    }
}

