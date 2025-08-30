
package com.tiora.mob.service;

import com.tiora.mob.dto.response.ServiceResponse;
// Use fully qualified name for Service entity to avoid ambiguity
import com.tiora.mob.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    @Autowired
    private ServiceRepository serviceRepository;


    /**
     * Get available services for a salon, filtered by genderAvailability.
     * @param salonId the salon id
     * @param gender MALE, FEMALE, or BOTH (case-insensitive, null/empty means BOTH)
     * @return list of ServiceResponse
     */
    public List<ServiceResponse> getAvailableServicesBySalonId(Long salonId, String gender) {
        String genderNorm = (gender == null || gender.isBlank()) ? "BOTH" : gender.trim().toUpperCase();
        List<com.tiora.mob.entity.Service> services = serviceRepository.findAll()
                .stream()
                .filter(s -> s.getSalon() != null && s.getSalon().getSalonId().equals(salonId))
                .filter(s -> s.getStatus() == com.tiora.mob.entity.Service.ServiceStatus.ACTIVE)
                .filter(s -> {
                    String avail = s.getGenderAvailability() != null ? s.getGenderAvailability().name() : "BOTH";
                    return avail.equals(genderNorm);
                })
                .collect(Collectors.toList());

        return services.stream().map(this::toServiceResponse).collect(Collectors.toList());
    }

    private ServiceResponse toServiceResponse(com.tiora.mob.entity.Service service) {
        ServiceResponse dto = new ServiceResponse();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setCategory(service.getCategory() != null ? service.getCategory().name() : null);
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setPrice(service.getPrice());
        dto.setImageUrl(service.getImageUrl());
        dto.setIsPopular(service.getIsPopular());
        dto.setStatus(service.getStatus() != null ? service.getStatus().name() : null);
        dto.setGenderAvailability(service.getGenderAvailability() != null ? service.getGenderAvailability().name() : null);
        dto.setDiscountPercentage(service.getDiscountPercentage());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }
}
