package com.skyways.flight.repository;

import com.skyways.flight.domain.SeatHold;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> {
}
