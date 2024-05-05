package org.example.xchange.data.repositories;

import org.example.xchange.data.models.FxRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxRateRepository extends JpaRepository<FxRate, Long> {
}
