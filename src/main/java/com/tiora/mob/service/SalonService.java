package com.tiora.mob.service;



import com.tiora.mob.dto.response.SalonResponse;
import com.tiora.mob.entity.Salon;
import com.tiora.mob.exception.ResourceNotFoundException;
import com.tiora.mob.repository.SalonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class SalonService {

    private static final Logger logger = LoggerFactory.getLogger(SalonService.class);

    @Autowired
    private SalonRepository salonRepository;



    // Get salon by ID
    @Transactional(readOnly = true)
    public Salon getSalonById(Long salonId) {
        return salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found with id: " + salonId));
    }


    // Get active salons
    @Transactional(readOnly = true)
    public List<Salon> getActiveSalons() {
        return salonRepository.findByStatusOrderByName(Salon.SalonStatus.ACTIVE);
    }


    // Get salons with pagination
    @Transactional(readOnly = true)
    public Page<Salon> getSalonsWithPagination(Salon.SalonStatus status, Pageable pageable) {
        return salonRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }




    // Get salons with pagination
    @Transactional(readOnly = true)
    public Page<Salon> getSalonsPaginated(Pageable pageable) {
        return salonRepository.findAll(pageable);
    }
}
