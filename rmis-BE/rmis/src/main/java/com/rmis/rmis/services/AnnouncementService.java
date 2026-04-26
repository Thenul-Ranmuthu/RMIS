package com.rmis.rmis.services;

import com.rmis.rmis.domain.entities.Announcement;
import java.util.List;

public interface AnnouncementService {
    List<Announcement> getAllAnnouncements();
    Announcement createAnnouncement(Announcement announcement);
}
