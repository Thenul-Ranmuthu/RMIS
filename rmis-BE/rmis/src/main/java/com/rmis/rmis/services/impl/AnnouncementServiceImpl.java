package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.entities.Announcement;
import com.rmis.rmis.repositories.AnnouncementRepository;
import com.rmis.rmis.services.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Override
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByDateDesc();
    }

    @Override
    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }
}
