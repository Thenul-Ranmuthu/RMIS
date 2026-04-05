package com.rmis.rmis.utils.impl;

import com.rmis.rmis.utils.TicketNumberGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TicketNumberGeneratorImpl implements TicketNumberGenerator {
    
    private final AtomicLong SEQUENCE = new AtomicLong(1);

    @Override
    public String generate() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("ST-%s-%04d", date, SEQUENCE.getAndIncrement());
    }
}
