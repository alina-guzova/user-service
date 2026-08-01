package com.inno.userservice.repository;

import com.inno.userservice.entity.PaymentCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

  boolean existsByNumber(String number);

  List<PaymentCard> findAllByUserId(Long userId);

  boolean existsByIdAndUserId(Long id, Long userId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE PaymentCard p SET p.active = true WHERE p.id = :id")
  int activatePaymentCard(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE PaymentCard p SET p.active = false WHERE p.id = :id")
  int deactivatePaymentCard(@Param("id") Long id);
}
