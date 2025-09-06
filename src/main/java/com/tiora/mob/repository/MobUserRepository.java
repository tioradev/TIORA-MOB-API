package com.tiora.mob.repository;

import com.tiora.mob.entity.MobUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobUserRepository extends JpaRepository<MobUser, String> {
    
    /**
     * Find a mobile user by barber phone number
     */
    Optional<MobUser> findByBarberPhone(String barberPhone);
    
    /**
     * Check if a barber phone exists in the system
     */
    boolean existsByBarberPhone(String barberPhone);
    
    /**
     * Find active barber by phone number
     */
    Optional<MobUser> findByBarberPhoneAndStatus(String barberPhone, MobUser.UserStatus status);
}
