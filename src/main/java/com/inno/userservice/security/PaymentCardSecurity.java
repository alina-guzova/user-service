package com.inno.userservice.security;

import com.inno.userservice.repository.PaymentCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("paymentCardSecurity")
@RequiredArgsConstructor
public class PaymentCardSecurity {
  private final PaymentCardRepository cardRepository;

  public boolean isCardOwner(Long cardId, Object principal) {
    if (!(principal instanceof Long userId)) return false;
    return cardRepository.existsByIdAndUserId(cardId, userId);
  }
}
