package com.inno.userservice.service;

import com.inno.userservice.dto.request.PaymentCardCreateRequestDTO;
import com.inno.userservice.dto.request.PaymentCardUpdateRequestDTO;
import com.inno.userservice.dto.response.PaymentCardResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCardService {

  PaymentCardResponseDTO create(PaymentCardCreateRequestDTO dto);

  PaymentCardResponseDTO update(Long cardId, PaymentCardUpdateRequestDTO dto);

  PaymentCardResponseDTO getById(Long cardId);

  Page<PaymentCardResponseDTO> getAll(Pageable pageable);

  List<PaymentCardResponseDTO> getAllCardsByUser(Long userId);

  void activate(Long cardId);

  void deactivate(Long cardId);
}
