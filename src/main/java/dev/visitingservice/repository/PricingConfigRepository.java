package dev.visitingservice.repository;

import dev.visitingservice.model.PricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingConfigRepository extends JpaRepository<PricingConfig, UUID> {

    @Query("SELECT pc FROM PricingConfig pc WHERE pc.isActive = true AND pc.configName = 'default' ORDER BY pc.updatedAt DESC")
    Optional<PricingConfig> findActiveDefaultConfig();

    @Query("SELECT pc FROM PricingConfig pc WHERE pc.isActive = true AND pc.configName = ?1")
    Optional<PricingConfig> findActiveConfigByName(String configName);
}
