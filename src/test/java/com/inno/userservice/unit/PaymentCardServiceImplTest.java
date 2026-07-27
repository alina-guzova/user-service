package com.inno.userservice.unit;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import com.inno.userservice.cache.UserCacheEvictor;
import com.inno.userservice.dto.request.PaymentCardCreateRequestDTO;
import com.inno.userservice.dto.request.PaymentCardUpdateRequestDTO;
import com.inno.userservice.dto.response.PaymentCardResponseDTO;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import com.inno.userservice.exception.BaseAppException;
import com.inno.userservice.mapper.PaymentCardMapper;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.service.impl.PaymentCardServiceImpl;
import com.inno.userservice.util.PaymentCardTestData;
import com.inno.userservice.util.UserTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PaymentCardRepository paymentCardRepository;

  @Mock
  private PaymentCardMapper paymentCardMapper;

  @Mock
  private UserCacheEvictor userCacheEvictor;

  @InjectMocks
  private PaymentCardServiceImpl paymentCardService;

  @Nested
  class CreateTests {

    @Test
    void whenCreatePaymentCard_thenCardIsSavedAndReturned() {
      // given
      PaymentCardCreateRequestDTO requestDto = PaymentCardTestData.buildCreateRequestDto();
      User user = UserTestData.buildUserEntity();
      PaymentCard mappedCard =
        PaymentCardTestData.buildPaymentCardEntity(null, requestDto.number(), null);
      PaymentCard savedCard = PaymentCardTestData.buildPaymentCardEntity(user);
      PaymentCardResponseDTO expectedResponse = PaymentCardTestData.buildResponseDto();

      when(paymentCardRepository.existsByNumber(requestDto.number())).thenReturn(false);
      when(userRepository.findById(requestDto.userId())).thenReturn(Optional.of(user));
      when(userRepository.countCardsByUserId(requestDto.userId())).thenReturn(2L);
      when(paymentCardMapper.toEntity(requestDto)).thenReturn(mappedCard);
      when(paymentCardRepository.save(mappedCard)).thenReturn(savedCard);
      when(paymentCardMapper.toDto(savedCard)).thenReturn(expectedResponse);

      // when
      PaymentCardResponseDTO actualResponse = paymentCardService.create(requestDto);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(paymentCardRepository).existsByNumber(requestDto.number());
      verify(userRepository).findById(requestDto.userId());
      verify(userRepository).countCardsByUserId(requestDto.userId());
      verify(paymentCardMapper).toEntity(requestDto);
      verify(paymentCardRepository).save(mappedCard);
      verify(paymentCardMapper).toDto(savedCard);
    }

    @Test
    void whenCreatePaymentCardWithExistingNumber_thenThrowConflictException() {
      // given
      PaymentCardCreateRequestDTO requestDto = PaymentCardTestData.buildCreateRequestDto();

      when(paymentCardRepository.existsByNumber(requestDto.number())).thenReturn(true);

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.create(requestDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

      verify(paymentCardRepository).existsByNumber(requestDto.number());
      verifyNoInteractions(userRepository, paymentCardMapper);
      verifyNoMoreInteractions(paymentCardRepository);
    }

    @Test
    void whenCreatePaymentCardWithNonExistentUser_thenThrowNotFoundException() {
      // given
      PaymentCardCreateRequestDTO requestDto = PaymentCardTestData.buildCreateRequestDto();

      when(paymentCardRepository.existsByNumber(requestDto.number())).thenReturn(false);
      when(userRepository.findById(requestDto.userId())).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.create(requestDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(paymentCardRepository).existsByNumber(requestDto.number());
      verify(userRepository).findById(requestDto.userId());
      verifyNoInteractions(paymentCardMapper);
      verifyNoMoreInteractions(paymentCardRepository);
    }

    @Test
    void whenCreatePaymentCardWhenLimitExceeded_thenThrowConflictException() {
      // given
      PaymentCardCreateRequestDTO requestDto = PaymentCardTestData.buildCreateRequestDto();
      User user = UserTestData.buildUserEntity();

      when(paymentCardRepository.existsByNumber(requestDto.number())).thenReturn(false);
      when(userRepository.findById(requestDto.userId())).thenReturn(Optional.of(user));
      when(userRepository.countCardsByUserId(requestDto.userId())).thenReturn(5L);

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.create(requestDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

      verify(paymentCardRepository).existsByNumber(requestDto.number());
      verify(userRepository).findById(requestDto.userId());
      verify(userRepository).countCardsByUserId(requestDto.userId());
      verifyNoInteractions(paymentCardMapper);
      verifyNoMoreInteractions(paymentCardRepository);
    }
  }

  @Nested
  class UpdateTests {

    @Test
    void whenUpdatePaymentCard_thenCardIsUpdatedAndReturned() {
      // given
      Long cardId = PaymentCardTestData.DEFAULT_CARD_ID;
      PaymentCardUpdateRequestDTO updateDto = PaymentCardTestData.buildUpdateRequestDto();
      User user = UserTestData.buildUserEntity();
      PaymentCard existingCard = PaymentCardTestData.buildPaymentCardEntity(user);
      PaymentCardResponseDTO expectedResponse = PaymentCardTestData.buildResponseDto();

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
      doNothing().when(paymentCardMapper).updateEntity(updateDto, existingCard);
      when(paymentCardRepository.save(existingCard)).thenReturn(existingCard);
      when(paymentCardMapper.toDto(existingCard)).thenReturn(expectedResponse);

      // when
      PaymentCardResponseDTO actualResponse = paymentCardService.update(cardId, updateDto);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(paymentCardRepository).findById(cardId);
      verify(paymentCardMapper).updateEntity(updateDto, existingCard);
      verify(paymentCardRepository).save(existingCard);
      verify(paymentCardMapper).toDto(existingCard);
    }

    @Test
    void whenUpdatePaymentCardWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long cardId = 999L;
      PaymentCardUpdateRequestDTO updateDto = PaymentCardTestData.buildUpdateRequestDto();

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.update(cardId, updateDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(paymentCardRepository).findById(cardId);
      verifyNoInteractions(paymentCardMapper);
      verifyNoMoreInteractions(paymentCardRepository);
    }
  }

  @Nested
  class GetByIdTests {

    @Test
    void whenGetById_thenCardIsReturned() {
      // given
      Long cardId = PaymentCardTestData.DEFAULT_CARD_ID;
      User user = UserTestData.buildUserEntity();
      PaymentCard card = PaymentCardTestData.buildPaymentCardEntity(user);
      PaymentCardResponseDTO expectedResponse = PaymentCardTestData.buildResponseDto();

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
      when(paymentCardMapper.toDto(card)).thenReturn(expectedResponse);

      // when
      PaymentCardResponseDTO actualResponse = paymentCardService.getById(cardId);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(paymentCardRepository).findById(cardId);
      verify(paymentCardMapper).toDto(card);
    }

    @Test
    void whenGetByIdWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long cardId = 999L;

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.getById(cardId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(paymentCardRepository).findById(cardId);
      verifyNoInteractions(paymentCardMapper);
    }
  }

  @Nested
  class GetAllTests {

    @Test
    void whenGetAll_thenPageOfCardsIsReturned() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      User user = UserTestData.buildUserEntity();
      PaymentCard card = PaymentCardTestData.buildPaymentCardEntity(user);
      Page<PaymentCard> cardPage = new PageImpl<>(List.of(card), pageable, 1);
      PaymentCardResponseDTO responseDto = PaymentCardTestData.buildResponseDto();

      when(paymentCardRepository.findAll(pageable)).thenReturn(cardPage);
      when(paymentCardMapper.toDto(card)).thenReturn(responseDto);

      // when
      Page<PaymentCardResponseDTO> actualPage = paymentCardService.getAll(pageable);

      // then
      assertThat(actualPage).isNotNull().hasSize(1);
      assertThat(actualPage.getContent().get(0)).isEqualTo(responseDto);

      verify(paymentCardRepository).findAll(pageable);
      verify(paymentCardMapper).toDto(card);
    }
  }

  @Nested
  class GetAllCardsByUserTests {

    @Test
    void whenGetAllCardsByUser_thenListOfCardsIsReturned() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      User user = UserTestData.buildUserEntity();
      PaymentCard card = PaymentCardTestData.buildPaymentCardEntity(user);
      List<PaymentCard> cardList = List.of(card);
      PaymentCardResponseDTO responseDto = PaymentCardTestData.buildResponseDto();

      when(userRepository.existsById(userId)).thenReturn(true);
      when(paymentCardRepository.findAllByUserId(userId)).thenReturn(cardList);
      when(paymentCardMapper.toDto(card)).thenReturn(responseDto);

      // when
      List<PaymentCardResponseDTO> actualList = paymentCardService.getAllCardsByUser(userId);

      // then
      assertThat(actualList).isNotNull().hasSize(1).containsExactly(responseDto);

      verify(userRepository).existsById(userId);
      verify(paymentCardRepository).findAllByUserId(userId);
      verify(paymentCardMapper).toDto(card);
    }

    @Test
    void whenGetAllCardsByUserWithNonExistentUserId_thenThrowNotFoundException() {
      // given
      Long userId = 999L;

      when(userRepository.existsById(userId)).thenReturn(false);

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.getAllCardsByUser(userId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(userRepository).existsById(userId);
      verifyNoInteractions(paymentCardRepository, paymentCardMapper);
    }
  }

  @Nested
  class ActivateTests {

    @Test
    void whenActivatePaymentCard_thenCardIsActivatedAndCacheEvicted() {
      // given
      Long cardId = PaymentCardTestData.DEFAULT_CARD_ID;
      User user = UserTestData.buildUserEntity();
      PaymentCard card = PaymentCardTestData.buildPaymentCardEntity(user);

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));

      // when
      paymentCardService.activate(cardId);

      // then
      verify(paymentCardRepository).findById(cardId);
      verify(paymentCardRepository).activatePaymentCard(cardId);
      verify(userCacheEvictor).evictUserById(user.getId());
      verifyNoInteractions(paymentCardMapper, userRepository);
    }

    @Test
    void whenActivatePaymentCardWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long cardId = 999L;

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.activate(cardId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(paymentCardRepository).findById(cardId);
      verify(paymentCardRepository, never()).activatePaymentCard(any());
      verifyNoInteractions(userCacheEvictor, paymentCardMapper, userRepository);
    }
  }

  @Nested
  class DeactivateTests {

    @Test
    void whenDeactivatePaymentCard_thenCardIsDeactivatedAndCacheEvicted() {
      // given
      Long cardId = PaymentCardTestData.DEFAULT_CARD_ID;
      User user = UserTestData.buildUserEntity();
      PaymentCard card = PaymentCardTestData.buildPaymentCardEntity(user);

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));

      // when
      paymentCardService.deactivate(cardId);

      // then
      verify(paymentCardRepository).findById(cardId);
      verify(paymentCardRepository).deactivatePaymentCard(cardId);
      verify(paymentCardRepository).findById(cardId);
      verify(paymentCardRepository).deactivatePaymentCard(cardId);
      verify(userCacheEvictor).evictUserById(user.getId());
      verifyNoInteractions(paymentCardMapper, userRepository);
    }

    @Test
    void whenDeactivatePaymentCardWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long cardId = 999L;

      when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
        assertThrows(BaseAppException.class, () -> paymentCardService.deactivate(cardId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(paymentCardRepository).findById(cardId);
      verify(paymentCardRepository, never()).deactivatePaymentCard(any());
      verifyNoInteractions(userCacheEvictor, paymentCardMapper, userRepository);
    }
  }
}
