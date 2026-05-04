package com.skyways.payment.repository;

import com.skyways.payment.domain.Payment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
}
