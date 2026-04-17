package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.entities.Announcement;
import com.rmis.rmis.services.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/public/announcements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Adjust this to your frontend URL in production
public class PublicAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public List<Announcement> getAnnouncements() {
        return announcementService.getAllAnnouncements();
    }
}
