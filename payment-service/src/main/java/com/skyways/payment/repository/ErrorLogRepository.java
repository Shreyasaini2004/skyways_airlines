package com.skyways.payment.repository;

import com.skyways.payment.domain.ErrorLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, UUID> {
}
