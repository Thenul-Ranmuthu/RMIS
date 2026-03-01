package com.rmis.rmis.security.interfaces;

public interface LoginAttemptService<T extends LockableAccount> {
    void preAuthenticateCheck(T user);
    void onSuccess(T user);
    void onFailure(T user);
    int remainingAttempts(T user);
}