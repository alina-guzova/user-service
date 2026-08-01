package com.inno.userservice.integration;

import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_ALREADY_EXISTS_WITH_NUMBER;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_LIMIT_EXCEEDED;
import static com.inno.userservice.exception.ExceptionMessages.PAYMENT_CARD_NOT_FOUND;
import static com.inno.userservice.exception.ExceptionMessages.USER_NOT_FOUND;
import static com.inno.userservice.util.PaymentCardTestData.ACTIVATE_CARD_API_PATH;
import static com.inno.userservice.util.PaymentCardTestData.CARDS_BY_USER_API_PATH;
import static com.inno.userservice.util.PaymentCardTestData.CARD_BY_ID_API_PATH;
import static com.inno.userservice.util.PaymentCardTestData.DEACTIVATE_CARD_API_PATH;
import static com.inno.userservice.util.PaymentCardTestData.EXISTING_CARD_NUMBER;
import static com.inno.userservice.util.PaymentCardTestData.MASKED_CARD_NUMBER;
import static com.inno.userservice.util.PaymentCardTestData.NEW_CARD_NUMBER_PREFIX;
import static com.inno.userservice.util.PaymentCardTestData.NON_EXISTENT_CARD_ID;
import static com.inno.userservice.util.PaymentCardTestData.PAYMENT_CARDS_API_PATH;
import static com.inno.userservice.util.PaymentCardTestData.SECOND_CARD_NUMBER;
import static com.inno.userservice.util.PaymentCardTestData.SECOND_MASKED_CARD_NUMBER;
import static com.inno.userservice.util.UserTestData.DEFAULT_ID;
import static com.inno.userservice.util.UserTestData.NON_EXISTENT_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inno.userservice.dto.request.PaymentCardCreateRequestDTO;
import com.inno.userservice.dto.request.PaymentCardUpdateRequestDTO;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.util.PaymentCardTestData;
import com.inno.userservice.util.UserTestData;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

class PaymentCardControllerIT extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private PaymentCardRepository paymentCardRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    paymentCardRepository.deleteAll();
    userRepository.deleteAll();
    Cache usersCache = cacheManager.getCache("users");
    if (usersCache != null) {
      usersCache.clear();
    }
  }

  private RequestPostProcessor userAuth(Long userId, String role) {
    return request -> {
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              userId, null, List.of(new SimpleGrantedAuthority(role)));
      context.setAuthentication(auth);
      return securityContext(context).postProcessRequest(request);
    };
  }

  @Nested
  class CreateTests {

    @Test
    void create_shouldCreatePaymentCardAndReturnMaskedDto_whenRequestIsValid() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));

      PaymentCardCreateRequestDTO requestDto =
          PaymentCardCreateRequestDTO.builder()
              .userId(user.getId())
              .number(PaymentCardTestData.DEFAULT_NUMBER)
              .holder(PaymentCardTestData.DEFAULT_HOLDER)
              .expirationDate(PaymentCardTestData.DEFAULT_EXPIRATION_DATE_STRING)
              .build();

      // when & then
      mockMvc
          .perform(
              post(PAYMENT_CARDS_API_PATH)
                  .with(userAuth(user.getId(), "ROLE_USER"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", notNullValue()))
          .andExpect(jsonPath("$.number", is(MASKED_CARD_NUMBER)))
          .andExpect(jsonPath("$.holder", is(PaymentCardTestData.DEFAULT_HOLDER)))
          .andExpect(
              jsonPath("$.expirationDate", is(PaymentCardTestData.DEFAULT_EXPIRATION_DATE_STRING)))
          .andExpect(jsonPath("$.active", is(true)));

      // then
      assertThat(paymentCardRepository.count()).isEqualTo(1);
      PaymentCard savedCard = paymentCardRepository.findAll().get(0);
      assertThat(savedCard.getNumber()).isEqualTo(PaymentCardTestData.DEFAULT_NUMBER);
      assertThat(savedCard.getHolder()).isEqualTo(PaymentCardTestData.DEFAULT_HOLDER);
      assertThat(savedCard.getUser().getId()).isEqualTo(DEFAULT_ID);
    }

    @Test
    void create_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      // given
      PaymentCardCreateRequestDTO requestDto =
          PaymentCardCreateRequestDTO.builder()
              .userId(NON_EXISTENT_USER_ID)
              .number(PaymentCardTestData.DEFAULT_NUMBER)
              .holder(PaymentCardTestData.DEFAULT_HOLDER)
              .expirationDate(PaymentCardTestData.DEFAULT_EXPIRATION_DATE_STRING)
              .build();

      // when & then
      mockMvc
          .perform(
              post(PAYMENT_CARDS_API_PATH)
                  .with(userAuth(NON_EXISTENT_USER_ID, "ROLE_USER"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(jsonPath("$.message", is(USER_NOT_FOUND.formatted(NON_EXISTENT_USER_ID))));

      // then
      assertThat(paymentCardRepository.count()).isEqualTo(0);
    }

    @Test
    void create_shouldReturnConflict_whenCardNumberAlreadyExists() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard existingCard =
          PaymentCardTestData.buildPaymentCardEntity(null, EXISTING_CARD_NUMBER, user);
      paymentCardRepository.save(existingCard);

      PaymentCardCreateRequestDTO requestDto =
          PaymentCardCreateRequestDTO.builder()
              .userId(user.getId())
              .number(EXISTING_CARD_NUMBER)
              .holder(PaymentCardTestData.DEFAULT_HOLDER)
              .expirationDate(PaymentCardTestData.DEFAULT_EXPIRATION_DATE_STRING)
              .build();

      // when & then
      mockMvc
          .perform(
              post(PAYMENT_CARDS_API_PATH)
                  .with(userAuth(user.getId(), "ROLE_USER"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.status", is(409)))
          .andExpect(jsonPath("$.error", is("Conflict")))
          .andExpect(
              jsonPath(
                  "$.message",
                  is(PAYMENT_CARD_ALREADY_EXISTS_WITH_NUMBER.formatted(EXISTING_CARD_NUMBER))));

      // then
      assertThat(paymentCardRepository.count()).isEqualTo(1);
    }

    @Test
    void create_shouldReturnConflict_whenUserReachedLimitOfFiveCards() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));

      for (int i = 0; i < 5; i++) {
        PaymentCard card =
            PaymentCardTestData.buildPaymentCardEntity(null, NEW_CARD_NUMBER_PREFIX + i, user);
        paymentCardRepository.save(card);
      }

      PaymentCardCreateRequestDTO requestDto =
          PaymentCardCreateRequestDTO.builder()
              .userId(user.getId())
              .number("9999999999999999")
              .holder(PaymentCardTestData.DEFAULT_HOLDER)
              .expirationDate(PaymentCardTestData.DEFAULT_EXPIRATION_DATE_STRING)
              .build();

      // when & then
      mockMvc
          .perform(
              post(PAYMENT_CARDS_API_PATH)
                  .with(userAuth(user.getId(), "ROLE_USER"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.status", is(409)))
          .andExpect(jsonPath("$.error", is("Conflict")))
          .andExpect(
              jsonPath("$.message", is(PAYMENT_CARD_LIMIT_EXCEEDED.formatted(user.getId()))));

      // then
      assertThat(paymentCardRepository.count()).isEqualTo(5);
    }

    @Test
    void create_shouldReturnForbidden_whenUserHasAdminRole() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));

      PaymentCardCreateRequestDTO requestDto =
          PaymentCardCreateRequestDTO.builder()
              .userId(user.getId())
              .number(PaymentCardTestData.DEFAULT_NUMBER)
              .holder(PaymentCardTestData.DEFAULT_HOLDER)
              .expirationDate(PaymentCardTestData.DEFAULT_EXPIRATION_DATE_STRING)
              .build();

      // when & then
      mockMvc
          .perform(
              post(PAYMENT_CARDS_API_PATH)
                  .with(userAuth(user.getId(), "ROLE_ADMIN"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isForbidden());

      // then
      assertThat(paymentCardRepository.count()).isEqualTo(0);
    }
  }

  @Nested
  class UpdateTests {

    @Test
    void update_shouldUpdatePaymentCardAndReturnUpdatedDto_whenCardExists() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard initialCard =
          paymentCardRepository.save(
              PaymentCardTestData.buildPaymentCardEntity(
                  null, PaymentCardTestData.DEFAULT_NUMBER, user));
      Long cardId = initialCard.getId();

      PaymentCardUpdateRequestDTO updateRequestDto = PaymentCardTestData.buildUpdateRequestDto();

      // when & then
      mockMvc
          .perform(
              put(CARD_BY_ID_API_PATH, cardId)
                  .with(userAuth(user.getId(), "ROLE_USER"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequestDto)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(cardId.intValue())))
          .andExpect(jsonPath("$.holder", is(PaymentCardTestData.UPDATED_HOLDER)))
          .andExpect(
              jsonPath("$.expirationDate", is(PaymentCardTestData.UPDATED_EXPIRATION_DATE_STRING)))
          .andExpect(jsonPath("$.number", is(MASKED_CARD_NUMBER)))
          .andExpect(jsonPath("$.active", is(true)));

      // then
      PaymentCard updatedCardInDb = paymentCardRepository.findById(cardId).orElseThrow();
      assertThat(updatedCardInDb.getHolder()).isEqualTo(PaymentCardTestData.UPDATED_HOLDER);
      assertThat(updatedCardInDb.getExpirationDate()).isEqualTo(LocalDate.of(2030, 5, 1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturnNotFound_whenCardDoesNotExist() throws Exception {
      // given
      PaymentCardUpdateRequestDTO updateRequestDto = PaymentCardTestData.buildUpdateRequestDto();

      // when & then
      mockMvc
          .perform(
              put(CARD_BY_ID_API_PATH, NON_EXISTENT_CARD_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequestDto)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(
              jsonPath("$.message", is(PAYMENT_CARD_NOT_FOUND.formatted(NON_EXISTENT_CARD_ID))));
    }

    @Test
    void update_shouldReturnForbidden_whenUserIsNotOwnerAndNotAdmin() throws Exception {
      // given
      User owner =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card =
          paymentCardRepository.save(
              PaymentCardTestData.buildPaymentCardEntity(
                  null, PaymentCardTestData.DEFAULT_NUMBER, owner));

      PaymentCardUpdateRequestDTO updateRequestDto = PaymentCardTestData.buildUpdateRequestDto();
      Long foreignUserId = 999L;

      // when & then
      mockMvc
          .perform(
              put(CARD_BY_ID_API_PATH, card.getId())
                  .with(userAuth(foreignUserId, "ROLE_USER"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequestDto)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetByIdTests {

    @Test
    void getById_shouldReturnPaymentCardDto_whenCardExists() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card =
          paymentCardRepository.save(
              PaymentCardTestData.buildPaymentCardEntity(
                  null, PaymentCardTestData.DEFAULT_NUMBER, user));
      Long cardId = card.getId();

      // when & then
      mockMvc
          .perform(get(CARD_BY_ID_API_PATH, cardId).with(userAuth(user.getId(), "ROLE_USER")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(cardId.intValue())))
          .andExpect(jsonPath("$.number", is(MASKED_CARD_NUMBER)))
          .andExpect(jsonPath("$.holder", is(PaymentCardTestData.DEFAULT_HOLDER)))
          .andExpect(
              jsonPath("$.expirationDate", is(PaymentCardTestData.DEFAULT_EXPIRATION_DATE_STRING)))
          .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_shouldReturnNotFound_whenCardDoesNotExist() throws Exception {
      // when & then
      mockMvc
          .perform(get(CARD_BY_ID_API_PATH, NON_EXISTENT_CARD_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(
              jsonPath("$.message", is(PAYMENT_CARD_NOT_FOUND.formatted(NON_EXISTENT_CARD_ID))));
    }

    @Test
    void getById_shouldReturnForbidden_whenUserIsNotOwnerAndNotAdmin() throws Exception {
      // given
      User owner =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card =
          paymentCardRepository.save(
              PaymentCardTestData.buildPaymentCardEntity(
                  null, PaymentCardTestData.DEFAULT_NUMBER, owner));

      Long foreignUserId = 999L;

      // when & then
      mockMvc
          .perform(
              get(CARD_BY_ID_API_PATH, card.getId()).with(userAuth(foreignUserId, "ROLE_USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetAllTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_shouldReturnPagedPaymentCards_whenCardsExist() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));

      PaymentCard card1 =
          PaymentCardTestData.buildPaymentCardEntity(
              null, PaymentCardTestData.DEFAULT_NUMBER, user);
      PaymentCard card2 =
          PaymentCardTestData.buildPaymentCardEntity(null, SECOND_CARD_NUMBER, user);

      paymentCardRepository.saveAll(List.of(card1, card2));

      // when & then
      mockMvc
          .perform(get(PAYMENT_CARDS_API_PATH).param("page", "0").param("size", "15"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()", is(2)))
          .andExpect(jsonPath("$.content[0].number", is(MASKED_CARD_NUMBER)))
          .andExpect(jsonPath("$.content[1].number", is(SECOND_MASKED_CARD_NUMBER)))
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.totalPages", is(1)))
          .andExpect(jsonPath("$.size", is(15)))
          .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_shouldReturnEmptyPage_whenNoCardsExist() throws Exception {
      // when & then
      mockMvc
          .perform(get(PAYMENT_CARDS_API_PATH))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()", is(0)))
          .andExpect(jsonPath("$.totalElements", is(0)));
    }
  }

  @Nested
  class GetAllCardsByUserTests {

    @Test
    void getAllCardsByUser_shouldReturnCardsList_whenUserExists() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card1 =
          PaymentCardTestData.buildPaymentCardEntity(
              null, PaymentCardTestData.DEFAULT_NUMBER, user);
      PaymentCard card2 =
          PaymentCardTestData.buildPaymentCardEntity(null, SECOND_CARD_NUMBER, user);
      paymentCardRepository.saveAll(List.of(card1, card2));

      // when & then
      mockMvc
          .perform(
              get(CARDS_BY_USER_API_PATH, user.getId()).with(userAuth(user.getId(), "ROLE_USER")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()", is(2)))
          .andExpect(jsonPath("$[0].number", is(MASKED_CARD_NUMBER)))
          .andExpect(jsonPath("$[1].number", is(SECOND_MASKED_CARD_NUMBER)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCardsByUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      // when & then
      mockMvc
          .perform(get(CARDS_BY_USER_API_PATH, NON_EXISTENT_USER_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(jsonPath("$.message", is(USER_NOT_FOUND.formatted(NON_EXISTENT_USER_ID))));
    }

    @Test
    void getAllCardsByUser_shouldReturnForbidden_whenUserRequestsOtherUserCardsAndNotAdmin()
        throws Exception {
      // given
      User owner =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      Long foreignUserId = 999L;

      // when & then
      mockMvc
          .perform(
              get(CARDS_BY_USER_API_PATH, owner.getId()).with(userAuth(foreignUserId, "ROLE_USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class ActivateTests {

    @Test
    void activate_shouldActivateCardAndEvictCache_whenCardExists() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card =
          PaymentCardTestData.buildPaymentCardEntity(
              null, PaymentCardTestData.DEFAULT_NUMBER, user);
      card.setActive(false);
      card = paymentCardRepository.save(card);
      Long cardId = card.getId();

      Cache usersCache = cacheManager.getCache("users");
      if (usersCache != null) {
        usersCache.put(user.getId(), "cachedValue");
      }

      // when & then
      mockMvc
          .perform(patch(ACTIVATE_CARD_API_PATH, cardId).with(userAuth(user.getId(), "ROLE_USER")))
          .andExpect(status().isNoContent());

      // then
      PaymentCard updatedCard = paymentCardRepository.findById(cardId).orElseThrow();
      assertThat(updatedCard.getActive()).isTrue();

      if (usersCache != null) {
        assertThat(usersCache.get(user.getId())).isNull();
      }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activate_shouldReturnNotFound_whenCardDoesNotExist() throws Exception {
      // when & then
      mockMvc
          .perform(patch(ACTIVATE_CARD_API_PATH, NON_EXISTENT_CARD_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(
              jsonPath("$.message", is(PAYMENT_CARD_NOT_FOUND.formatted(NON_EXISTENT_CARD_ID))));
    }

    @Test
    void activate_shouldReturnForbidden_whenUserIsNotOwnerAndNotAdmin() throws Exception {
      // given
      User owner =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card =
          paymentCardRepository.save(
              PaymentCardTestData.buildPaymentCardEntity(
                  null, PaymentCardTestData.DEFAULT_NUMBER, owner));

      Long foreignUserId = 999L;

      // when & then
      mockMvc
          .perform(
              patch(ACTIVATE_CARD_API_PATH, card.getId())
                  .with(userAuth(foreignUserId, "ROLE_USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class DeactivateTests {

    @Test
    void deactivate_shouldDeactivateCardAndEvictCache_whenCardExists() throws Exception {
      // given
      User user =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card =
          PaymentCardTestData.buildPaymentCardEntity(
              null, PaymentCardTestData.DEFAULT_NUMBER, user);
      card.setActive(true);
      card = paymentCardRepository.save(card);
      Long cardId = card.getId();

      Cache usersCache = cacheManager.getCache("users");
      if (usersCache != null) {
        usersCache.put(user.getId(), "cachedValue");
      }

      // when & then
      mockMvc
          .perform(
              patch(DEACTIVATE_CARD_API_PATH, cardId).with(userAuth(user.getId(), "ROLE_USER")))
          .andExpect(status().isNoContent());

      // then
      PaymentCard updatedCard = paymentCardRepository.findById(cardId).orElseThrow();
      assertThat(updatedCard.getActive()).isFalse();

      if (usersCache != null) {
        assertThat(usersCache.get(user.getId())).isNull();
      }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_shouldReturnNotFound_whenCardDoesNotExist() throws Exception {
      // when & then
      mockMvc
          .perform(patch(DEACTIVATE_CARD_API_PATH, NON_EXISTENT_CARD_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(
              jsonPath("$.message", is(PAYMENT_CARD_NOT_FOUND.formatted(NON_EXISTENT_CARD_ID))));
    }

    @Test
    void deactivate_shouldReturnForbidden_whenUserIsNotOwnerAndNotAdmin() throws Exception {
      // given
      User owner =
          userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      PaymentCard card =
          paymentCardRepository.save(
              PaymentCardTestData.buildPaymentCardEntity(
                  null, PaymentCardTestData.DEFAULT_NUMBER, owner));

      Long foreignUserId = 999L;

      // when & then
      mockMvc
          .perform(
              patch(DEACTIVATE_CARD_API_PATH, card.getId())
                  .with(userAuth(foreignUserId, "ROLE_USER")))
          .andExpect(status().isForbidden());
    }
  }
}
