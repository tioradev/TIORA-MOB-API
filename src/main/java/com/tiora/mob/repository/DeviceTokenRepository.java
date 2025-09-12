package com.tiora.mob.repository;

import com.tiora.mob.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByEmployeeIdAndIsActiveTrue(Long employeeId);
    List<DeviceToken> findByCustomerIdAndIsActiveTrue(Long customerId);
    Optional<DeviceToken> findByDeviceTokenAndIsActiveTrue(String deviceToken);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.isActive = false WHERE d.employeeId = :employeeId AND d.deviceToken != :currentToken")
    void deactivateOtherTokensForEmployee(@Param("employeeId") Long employeeId, @Param("currentToken") String currentToken);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.isActive = false WHERE d.customerId = :customerId AND d.deviceToken != :currentToken")
    void deactivateOtherTokensForCustomer(@Param("customerId") Long customerId, @Param("currentToken") String currentToken);

    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken d SET d.lastUsedAt = :timestamp WHERE d.deviceToken = :deviceToken")
    void updateLastUsedAt(@Param("deviceToken") String deviceToken, @Param("timestamp") LocalDateTime timestamp);

    @Modifying
    @Query("DELETE FROM DeviceToken d WHERE d.isActive = false AND d.updatedAt < :cutoffDate")
    void deleteInactiveTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
