package dev.visitingservice.service;

import dev.visitingservice.model.PricingConfig;
import dev.visitingservice.repository.PricingConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PricingConfigService {

    @Autowired
    private PricingConfigRepository pricingConfigRepository;

    public PricingConfig getActiveConfig() {
        return pricingConfigRepository.findActiveDefaultConfig()
            .orElseThrow(() -> new RuntimeException("No active pricing configuration found"));
    }

    public Optional<PricingConfig> getConfigByName(String configName) {
        return pricingConfigRepository.findActiveConfigByName(configName);
    }

    public PricingConfig createOrUpdateConfig(PricingConfig config) {
        return pricingConfigRepository.save(config);
    }

    public List<PricingConfig> getAllConfigs() {
        return pricingConfigRepository.findAll();
    }

    public void deactivateConfig(UUID configId) {
        Optional<PricingConfig> config = pricingConfigRepository.findById(configId);
        if (config.isPresent()) {
            config.get().setIsActive(false);
            pricingConfigRepository.save(config.get());
        }
    }

    // Initialize default configuration if none exists
    public void initializeDefaultConfig() {
        if (pricingConfigRepository.findActiveDefaultConfig().isEmpty()) {
            PricingConfig defaultConfig = new PricingConfig("default");
            pricingConfigRepository.save(defaultConfig);
        }
    }
}
