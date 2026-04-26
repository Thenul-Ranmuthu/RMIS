package com.rmis.rmis.services.impl.export;

import com.rmis.rmis.domain.enums.ExportFormat;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ExportServiceFactory {

    private final Map<ExportFormat, ExportService> registry = new EnumMap<>(ExportFormat.class);

    /**
     * Spring auto-wires all ExportService implementations here.
     * Each implementation registers itself by its supported format.
     */
    public ExportServiceFactory(List<ExportService> services) {
        for (ExportService service : services) {
            registry.put(service.getSupportedFormat(), service);
        }
    }

    /**
     * Returns the correct ExportService for the requested format.
     *
     * @throws IllegalArgumentException if the format is not supported
     */
    public ExportService getService(ExportFormat format) {
        ExportService service = registry.get(format);
        if (service == null) {
            throw new IllegalArgumentException(
                    "No export service registered for format: " + format);
        }
        return service;
    }
}