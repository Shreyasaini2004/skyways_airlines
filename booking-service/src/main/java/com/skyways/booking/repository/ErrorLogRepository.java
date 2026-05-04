package com.skyways.booking.repository;

import com.skyways.booking.domain.ErrorLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, UUID> {
}
