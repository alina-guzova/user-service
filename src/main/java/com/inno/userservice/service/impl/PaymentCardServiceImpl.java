package com.inno.userservice.service.impl;

import static com.inno.userservice.cache.CacheConstants.USERS_CACHE;
import static com.inno.userservice.exception.BaseAppExceptionBuilder.buildConflictException;
import static com.inno.userservice.exception.BaseAppExceptionBuilder.buildNotFoundException;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_ALREADY_EXISTS_WITH_NUMBER;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_LIMIT_EXCEEDED;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_NOT_FOUND;
import static com.inno.userservice.exception.ExceptionMessages.USER_NOT_FOUND;

import com.inno.userservice.cache.UserCacheEvictor;
import com.inno.userservice.dto.request.PaymentCardCreateRequestDTO;
import com.inno.userservice.dto.request.PaymentCardUpdateRequestDTO;
import com.inno.userservice.dto.response.PaymentCardResponseDTO;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import com.inno.userservice.mapper.PaymentCardMapper;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.service.PaymentCardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentCardServiceImpl implements PaymentCardService {

  private final PaymentCardMapper paymentCardMapper;
  private final PaymentCardRepository paymentCardRepository;
  private final UserRepository userRepository;
  private final UserCacheEvictor userCacheEvictor;

  @Override
  @Transactional
  @CacheEvict(value = USERS_CACHE, key = "#dto.userId()")
  public PaymentCardResponseDTO create(PaymentCardCreateRequestDTO dto) {

    if (paymentCardRepository.existsByNumber(dto.number())) {
      log.warn(
          "Payment card creation failed: card number already exists for userId={}", dto.userId());
      throw buildConflictException(PAYMENT_CARD_ALREADY_EXISTS_WITH_NUMBER, dto.number());
    }

    User user =
        userRepository
            .findById(dto.userId())
            .orElseThrow(
                () -> {
                  log.warn(
                      "Payment card creation failed: user not found | userId={}", dto.userId());
                  return buildNotFoundException(USER_NOT_FOUND, dto.userId());
                });

    if (userRepository.countCardsByUserId(dto.userId()) >= 5) {
      log.warn("Payment card creation failed: limit reached for userId={}", dto.userId());
      throw buildConflictException(PAYMENT_CARD_LIMIT_EXCEEDED, dto.userId());
    }

    PaymentCard paymentCard = paymentCardMapper.toEntity(dto);
    paymentCard.setUser(user);

    PaymentCard savedCard = paymentCardRepository.save(paymentCard);
    log.info(
        "Payment card created successfully | cardId={}, userId={}",
        savedCard.getId(),
        dto.userId());

    return paymentCardMapper.toDto(savedCard);
  }

  @Override
  @Transactional
  @CacheEvict(value = USERS_CACHE, key = "#result.id")
  public PaymentCardResponseDTO update(Long cardId, PaymentCardUpdateRequestDTO dto) {

    PaymentCard card =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(
                () -> {
                  log.warn("Payment card update failed: card not found | cardId={}", cardId);
                  return buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId);
                });

    paymentCardMapper.updateEntity(dto, card);
    log.info("Payment card updated successfully | cardId={}", cardId);

    return paymentCardMapper.toDto(paymentCardRepository.save(card));
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentCardResponseDTO getById(Long cardId) {

    PaymentCard card =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(
                () -> {
                  log.warn("Payment card fetch failed: card not found | cardId={}", cardId);
                  return buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId);
                });

    return paymentCardMapper.toDto(card);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentCardResponseDTO> getAll(Pageable pageable) {

    return paymentCardRepository.findAll(pageable).map(paymentCardMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentCardResponseDTO> getAllCardsByUser(Long userId) {

    if (!userRepository.existsById(userId)) {
      log.warn("Failed to get payment cards: user not found | userId={}", userId);
      throw buildNotFoundException(USER_NOT_FOUND, userId);
    }

    return paymentCardRepository.findAllByUserId(userId).stream()
        .map(paymentCardMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void activate(Long cardId) {
    PaymentCard card =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(
                () -> {
                  log.warn("Payment card activation failed: card not found | cardId={}", cardId);
                  return buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId);
                });

    paymentCardRepository.activatePaymentCard(cardId);
    log.info("Payment card activated | cardId={}", cardId);

    if (card.getUser() != null) {
      userCacheEvictor.evictUserById(card.getUser().getId());
    }
  }

  @Override
  @Transactional
  public void deactivate(Long cardId) {
    PaymentCard card =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(
                () -> {
                  log.warn("Payment card deactivation failed: card not found | cardId={}", cardId);
                  return buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId);
                });

    paymentCardRepository.deactivatePaymentCard(cardId);
    log.info("Payment card deactivated | cardId={}", cardId);

    if (card.getUser() != null) {
      userCacheEvictor.evictUserById(card.getUser().getId());
    }
  }
}
