package com.rmis.rmis.security.interfaces;

import java.time.LocalDateTime;

public interface LockableAccount {
    Integer getFailedLoginAttempts();
    void setFailedLoginAttempts(Integer attempts);

    LocalDateTime getLockedUntil();
    void setLockedUntil(LocalDateTime lockedUntil);
}
