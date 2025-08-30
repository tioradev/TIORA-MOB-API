
package com.tiora.mob.repository;

import com.tiora.mob.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

	// Find by service ID and salon ID
	@Query("SELECT s FROM Service s WHERE s.id = :id AND s.salon.id = :salonId")
	Optional<Service> findByIdAndSalonId(@Param("id") Long id, @Param("salonId") Long salonId);

	@Query("SELECT s FROM Service s JOIN FETCH s.salon")
	List<Service> findAllWithSalon();

}