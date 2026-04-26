package com.rmis.rmis.utils.impl;

import com.rmis.rmis.utils.TicketNumberGenerator;
import org.springframework.stereotype.Component;

@Component
public class TicketNumberGeneratorImpl implements TicketNumberGenerator {
    

    @Override
    public String generate() {
        String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String random = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("ST-%s-%s", date, random);
    }
}
