package com.tiora.mob.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tiora.mob.entity.WeeklySchedule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class WeeklyScheduleConverter implements AttributeConverter<WeeklySchedule, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(WeeklyScheduleConverter.class);
    private final ObjectMapper objectMapper;
    
    public WeeklyScheduleConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public String convertToDatabaseColumn(WeeklySchedule weeklySchedule) {
        if (weeklySchedule == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(weeklySchedule);
        } catch (JsonProcessingException e) {
            logger.error("Error converting WeeklySchedule to JSON", e);
            throw new RuntimeException("Error converting WeeklySchedule to JSON", e);
        }
    }
    
    @Override
    public WeeklySchedule convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new WeeklySchedule(); // Return default schedule
        }
        
        try {
            return objectMapper.readValue(dbData, WeeklySchedule.class);
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSON to WeeklySchedule", e);
            return new WeeklySchedule(); // Return default schedule on error
        }
    }
}
