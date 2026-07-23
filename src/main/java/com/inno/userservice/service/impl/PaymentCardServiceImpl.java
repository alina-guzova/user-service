package com.inno.userservice.service.impl;

import static com.inno.userservice.exception.BaseAppExceptionBuilder.buildConflictException;
import static com.inno.userservice.exception.BaseAppExceptionBuilder.buildNotFoundException;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_ALREADY_EXISTS_WITH_NUMBER;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_LIMIT_EXCEEDED;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_NOT_FOUND;
import static com.inno.userservice.exception.ExceptionMessages.USER_NOT_FOUND;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {

  private final PaymentCardMapper paymentCardMapper;
  private final PaymentCardRepository paymentCardRepository;
  private final UserRepository userRepository;
  private final CacheManager cacheManager;

  @Autowired
  public PaymentCardServiceImpl(
      PaymentCardMapper paymentCardMapper,
      PaymentCardRepository paymentCardRepository,
      UserRepository userRepository,
      CacheManager cacheManager) {
    this.paymentCardMapper = paymentCardMapper;
    this.paymentCardRepository = paymentCardRepository;
    this.userRepository = userRepository;
    this.cacheManager = cacheManager;
  }

  @Override
  @Transactional
  @CacheEvict(value = "users", key = "#dto.userId()")
  public PaymentCardResponseDTO create(PaymentCardCreateRequestDTO dto) {

    if (paymentCardRepository.existsByNumber(dto.number())) {
      throw buildConflictException(PAYMENT_CARD_ALREADY_EXISTS_WITH_NUMBER, dto.number());
    }

    User user =
        userRepository
            .findById(dto.userId())
            .orElseThrow(() -> buildNotFoundException(USER_NOT_FOUND, dto.userId()));

    if (userRepository.countCardsByUserId(dto.userId()) >= 5) {
      throw buildConflictException(PAYMENT_CARD_LIMIT_EXCEEDED, dto.userId());
    }

    PaymentCard paymentCard = paymentCardMapper.toEntity(dto);
    paymentCard.setUser(user);

    return paymentCardMapper.toDto(paymentCardRepository.save(paymentCard));
  }

  @Override
  @Transactional
  @CacheEvict(value = "users", key = "#result.id")
  public PaymentCardResponseDTO update(Long cardId, PaymentCardUpdateRequestDTO dto) {

    PaymentCard card =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(() -> buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId));

    paymentCardMapper.updateEntity(dto, card);

    return paymentCardMapper.toDto(paymentCardRepository.save(card));
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentCardResponseDTO getById(Long cardId) {

    PaymentCard card =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(() -> buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId));

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
            .orElseThrow(() -> buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId));

    paymentCardRepository.activatePaymentCard(cardId);

    Cache cache = cacheManager.getCache("users");
    if (cache != null && card.getUser() != null) {
      cache.evict(card.getUser().getId());
    }
  }

  @Override
  @Transactional
  public void deactivate(Long cardId) {
    PaymentCard card =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(() -> buildNotFoundException(PAYMENT_CARD_NOT_FOUND, cardId));

    paymentCardRepository.deactivatePaymentCard(cardId);

    Cache cache = cacheManager.getCache("users");
    if (cache != null && card.getUser() != null) {
      cache.evict(card.getUser().getId());
    }
  }
}
