package com.rmis.rmis.security.service;

import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.repositories.PublicUserRepository;
import com.rmis.rmis.security.AbstractLoginAttemptService;
import org.springframework.stereotype.Service;

@Service
public class PublicUserLoginAttemptService extends AbstractLoginAttemptService<PublicUser> {

    private final PublicUserRepository userRepo;

    public PublicUserLoginAttemptService(PublicUserRepository userRepo) {
        super(3, 5);
        this.userRepo = userRepo;
    }

    @Override
    protected PublicUser save(PublicUser user) {
        return userRepo.save(user);
    }
}
