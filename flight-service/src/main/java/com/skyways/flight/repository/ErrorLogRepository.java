package com.skyways.flight.repository;

import com.skyways.flight.domain.ErrorLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, UUID> {
}
