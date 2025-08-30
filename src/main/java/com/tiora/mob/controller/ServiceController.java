package com.tiora.mob.controller;

import com.tiora.mob.dto.response.ServiceResponse;
import com.tiora.mob.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/salons")
public class ServiceController {

    @Autowired
    private AvailabilityService availabilityService;

    @GetMapping("/{salonId}/services")
    public List<ServiceResponse> getAvailableServices(
            @PathVariable Long salonId,
            @RequestParam(value = "gender", required = false) String gender) {
        return availabilityService.getAvailableServicesBySalonId(salonId, gender);
    }
}
