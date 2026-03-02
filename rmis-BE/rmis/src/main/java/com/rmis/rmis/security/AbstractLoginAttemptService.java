package com.rmis.rmis.security;

import com.rmis.rmis.exceptions.AccountLockedException;
import com.rmis.rmis.security.interfaces.LockableAccount;
import com.rmis.rmis.security.interfaces.LoginAttemptService;

import java.time.LocalDateTime;

public abstract class AbstractLoginAttemptService<T extends LockableAccount>
        implements LoginAttemptService<T> {

    private final int maxFailedAttempts;
    private final int lockMinutes;

    protected AbstractLoginAttemptService(int maxFailedAttempts, int lockMinutes) {
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockMinutes = lockMinutes;
    }

    // Each concrete service must define how to save the entity
    protected abstract T save(T user);

    @Override
    public void preAuthenticateCheck(T user) {
        LocalDateTime now = LocalDateTime.now();

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AccountLockedException(
                    "Account locked. Try again after " + user.getLockedUntil().toLocalTime()
            );
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(now)) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            save(user);
        }
    }

    @Override
    public void onSuccess(T user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        save(user);
    }

    @Override
    public void onFailure(T user) {
        LocalDateTime now = LocalDateTime.now();

        int attempts;
        if (user.getFailedLoginAttempts() == null || user.getFailedLoginAttempts() == 0) attempts = 0;
        else attempts = user.getFailedLoginAttempts();
        attempts++;
        user.setFailedLoginAttempts(attempts);
        System.out.println("failed login attempts " + attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(now.plusMinutes(lockMinutes));
            save(user);
            throw new AccountLockedException(
                    "Account locked. Try again after " + user.getLockedUntil()
            );
        }

        save(user);
    }

    @Override
    public int remainingAttempts(T user) {
        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        return Math.max(0, maxFailedAttempts - attempts);
    }
}
