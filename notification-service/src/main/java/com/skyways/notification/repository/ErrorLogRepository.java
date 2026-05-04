package com.skyways.notification.repository;

import com.skyways.notification.domain.ErrorLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, UUID> {
}
