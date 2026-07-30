package com.inno.userservice.integration;

import static com.inno.userservice.exception.ExceptionMessages.USER_ALREADY_EXISTS_WITH_EMAIL;
import static com.inno.userservice.exception.ExceptionMessages.USER_ALREADY_EXISTS_WITH_ID;
import static com.inno.userservice.exception.ExceptionMessages.USER_NOT_FOUND;
import static com.inno.userservice.util.UserTestData.ACTIVATE_USER_API_PATH;
import static com.inno.userservice.util.UserTestData.ANOTHER_EMAIL;
import static com.inno.userservice.util.UserTestData.DEACTIVATE_USER_API_PATH;
import static com.inno.userservice.util.UserTestData.DEFAULT_ID;
import static com.inno.userservice.util.UserTestData.EXISTING_EMAIL;
import static com.inno.userservice.util.UserTestData.NON_EXISTENT_USER_ID;
import static com.inno.userservice.util.UserTestData.REDIS_CACHE_KEY_PREFIX;
import static com.inno.userservice.util.UserTestData.SEARCH_NAME;
import static com.inno.userservice.util.UserTestData.SEARCH_SURNAME;
import static com.inno.userservice.util.UserTestData.USERS_API_PATH;
import static com.inno.userservice.util.UserTestData.USER_BY_ID_API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inno.userservice.dto.request.UserCreateRequestDTO;
import com.inno.userservice.dto.request.UserUpdateRequestDTO;
import com.inno.userservice.entity.User;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.util.UserTestData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

class UserControllerIT extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private StringRedisTemplate redisTemplate;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    if (redisTemplate.getConnectionFactory() != null) {
      redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }
  }

  @Nested
  class CreateUserTests {

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
      // given
      Long userId = DEFAULT_ID;
      UserCreateRequestDTO requestDto = UserTestData.buildCreateRequestDto();

      // when & then
      mockMvc
          .perform(
              post(USER_BY_ID_API_PATH, userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(userId.intValue())))
          .andExpect(jsonPath("$.name", is(UserTestData.DEFAULT_NAME)))
          .andExpect(jsonPath("$.surname", is(UserTestData.DEFAULT_SURNAME)))
          .andExpect(jsonPath("$.email", is(UserTestData.DEFAULT_EMAIL)))
          .andExpect(jsonPath("$.birthDate", is(UserTestData.DEFAULT_BIRTH_DATE.toString())));

      assertThat(userRepository.count()).isEqualTo(1);

      User savedUser = userRepository.findById(userId).orElseThrow();
      assertThat(savedUser.getId()).isEqualTo(userId);
      assertThat(savedUser.getEmail()).isEqualTo(UserTestData.DEFAULT_EMAIL);
      assertThat(savedUser.getName()).isEqualTo(UserTestData.DEFAULT_NAME);
      assertThat(savedUser.getSurname()).isEqualTo(UserTestData.DEFAULT_SURNAME);
    }

    @Test
    void shouldReturnConflictWhenUserIdAlreadyExists() throws Exception {
      // given
      Long existingUserId = DEFAULT_ID;
      User existingUser = UserTestData.buildUserEntity(existingUserId, EXISTING_EMAIL);
      userRepository.save(existingUser);

      UserCreateRequestDTO requestDto = UserTestData.buildCreateRequestDto();

      // when & then
      mockMvc
          .perform(
              post(USER_BY_ID_API_PATH, existingUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.status", is(409)))
          .andExpect(jsonPath("$.error", is("Conflict")))
          .andExpect(
              jsonPath("$.message", is(USER_ALREADY_EXISTS_WITH_ID.formatted(existingUserId))));

      assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
      // given
      Long existingUserId = DEFAULT_ID;
      User existingUser = UserTestData.buildUserEntity(existingUserId, EXISTING_EMAIL);
      userRepository.save(existingUser);

      Long newUserId = NON_EXISTENT_USER_ID;
      UserCreateRequestDTO requestWithExistingEmail =
          UserTestData.buildCreateRequestDto(EXISTING_EMAIL);

      // when & then
      mockMvc
          .perform(
              post(USER_BY_ID_API_PATH, newUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestWithExistingEmail)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.status", is(409)))
          .andExpect(jsonPath("$.error", is("Conflict")))
          .andExpect(
              jsonPath("$.message", is(USER_ALREADY_EXISTS_WITH_EMAIL.formatted(EXISTING_EMAIL))));

      assertThat(userRepository.count()).isEqualTo(1);
    }
  }

  @Nested
  class UpdateUserTests {

    @Test
    void update_ShouldUpdateUserAndReturnUpdatedDto_WhenUserExists() throws Exception {
      // given
      Long userId = DEFAULT_ID;
      userRepository.saveAndFlush(UserTestData.buildUserEntity(userId, UserTestData.DEFAULT_EMAIL));

      UserUpdateRequestDTO updateRequestDto = UserTestData.buildUpdateRequestDto();

      // when & then
      mockMvc
          .perform(
              put(USER_BY_ID_API_PATH, userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequestDto)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(DEFAULT_ID.intValue())))
          .andExpect(jsonPath("$.name", is(UserTestData.UPDATED_NAME)))
          .andExpect(jsonPath("$.surname", is(UserTestData.UPDATED_SURNAME)))
          .andExpect(jsonPath("$.email", is(UserTestData.DEFAULT_EMAIL)))
          .andExpect(jsonPath("$.birthDate", is(UserTestData.DEFAULT_BIRTH_DATE.toString())));

      // then
      User updatedUserInDb = userRepository.findById(userId).orElseThrow();
      assertThat(updatedUserInDb.getName()).isEqualTo(UserTestData.UPDATED_NAME);
      assertThat(updatedUserInDb.getSurname()).isEqualTo(UserTestData.UPDATED_SURNAME);
      assertThat(updatedUserInDb.getEmail()).isEqualTo(UserTestData.DEFAULT_EMAIL);
    }

    @Test
    void update_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
      // given
      UserUpdateRequestDTO updateRequestDto = UserTestData.buildUpdateRequestDto();

      // when & then
      mockMvc
          .perform(
              put(USER_BY_ID_API_PATH, NON_EXISTENT_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequestDto)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(jsonPath("$.message", is(USER_NOT_FOUND.formatted(NON_EXISTENT_USER_ID))));
    }

    @Test
    void update_shouldEvictUserFromRedisCache_whenUserIsUpdated() throws Exception {
      // given
      Long userId = DEFAULT_ID;
      userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      ;
      String cacheKey = REDIS_CACHE_KEY_PREFIX + userId;

      mockMvc.perform(get(USER_BY_ID_API_PATH, userId)).andExpect(status().isOk());
      assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

      UserUpdateRequestDTO updateRequestDto = UserTestData.buildUpdateRequestDto();

      // when
      mockMvc
          .perform(
              put(USER_BY_ID_API_PATH, userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequestDto)))
          .andExpect(status().isOk());

      // then
      assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }
  }

  @Nested
  class GetByIdTests {

    @Test
    void getById_shouldReturnUserDto_whenUserExists() throws Exception {
      // given
      Long userId = DEFAULT_ID;
      userRepository.save(UserTestData.buildUserEntity(userId, UserTestData.DEFAULT_EMAIL));

      // when & then
      mockMvc
          .perform(get(USER_BY_ID_API_PATH, userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(userId.intValue())))
          .andExpect(jsonPath("$.name", is(UserTestData.DEFAULT_NAME)))
          .andExpect(jsonPath("$.surname", is(UserTestData.DEFAULT_SURNAME)))
          .andExpect(jsonPath("$.email", is(UserTestData.DEFAULT_EMAIL)))
          .andExpect(jsonPath("$.birthDate", is(UserTestData.DEFAULT_BIRTH_DATE.toString())));
    }

    @Test
    void getById_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      // when & then
      mockMvc
          .perform(get(USER_BY_ID_API_PATH, NON_EXISTENT_USER_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(jsonPath("$.message", is(USER_NOT_FOUND.formatted(NON_EXISTENT_USER_ID))));
    }

    @Test
    void getById_shouldCacheUserInRedis_whenCalledFirstTime() throws Exception {
      // given
      Long userId = DEFAULT_ID;
      userRepository.save(UserTestData.buildUserEntity(userId, UserTestData.DEFAULT_EMAIL));
      String cacheKey = REDIS_CACHE_KEY_PREFIX + userId;

      assertThat(redisTemplate.hasKey(cacheKey)).isFalse();

      // when
      mockMvc.perform(get(USER_BY_ID_API_PATH, userId)).andExpect(status().isOk());

      // then
      assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

      // given
      userRepository.deleteAll();

      // when & then
      mockMvc
          .perform(get(USER_BY_ID_API_PATH, userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(userId.intValue())))
          .andExpect(jsonPath("$.email", is(UserTestData.DEFAULT_EMAIL)));
    }
  }

  @Nested
  class GetAllTests {

    @Test
    void getAll_shouldReturnPagedUsersWithoutFilter_whenNoFilterProvided() throws Exception {
      // given
      userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));
      userRepository.save(UserTestData.buildUserEntity(NON_EXISTENT_USER_ID, ANOTHER_EMAIL));

      // when & then
      mockMvc
          .perform(get(USERS_API_PATH).param("page", "0").param("size", "15"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()", is(2)))
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.totalPages", is(1)))
          .andExpect(jsonPath("$.size", is(15)))
          .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void getAll_shouldReturnFilteredUsers_whenFilterIsProvided() throws Exception {
      // given
      User alice = UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL);

      User bob = UserTestData.buildUserEntity(NON_EXISTENT_USER_ID, ANOTHER_EMAIL);
      bob.setName("Bob");
      bob.setSurname("Marley");

      userRepository.saveAll(List.of(alice, bob));

      // when & then
      mockMvc
          .perform(get(USERS_API_PATH).param("name", SEARCH_NAME).param("surname", SEARCH_SURNAME))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()", is(1)))
          .andExpect(jsonPath("$.content[0].name", is(SEARCH_NAME)))
          .andExpect(jsonPath("$.content[0].surname", is(SEARCH_SURNAME)))
          .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoUsersMatchFilter() throws Exception {
      // given
      userRepository.save(UserTestData.buildUserEntity(DEFAULT_ID, UserTestData.DEFAULT_EMAIL));

      // when & then
      mockMvc
          .perform(get(USERS_API_PATH).param("name", "NonExistentName"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()", is(0)))
          .andExpect(jsonPath("$.totalElements", is(0)));
    }
  }

  @Nested
  class ActivateTests {

    @Test
    void activate_shouldActivateUserAndReturnNoContent_whenUserExists() throws Exception {
      // given
      Long userId = DEFAULT_ID;
      User user = UserTestData.buildUserEntity(userId, UserTestData.DEFAULT_EMAIL);
      user.setActive(false);
      userRepository.save(user);

      // when & then
      mockMvc.perform(patch(ACTIVATE_USER_API_PATH, userId)).andExpect(status().isNoContent());

      // then
      User updatedUser = userRepository.findById(userId).orElseThrow();
      assertThat(updatedUser.getActive()).isTrue();
    }

    @Test
    void activate_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      // when & then
      mockMvc
          .perform(patch(ACTIVATE_USER_API_PATH, NON_EXISTENT_USER_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(jsonPath("$.message", is(USER_NOT_FOUND.formatted(NON_EXISTENT_USER_ID))));
    }
  }

  @Nested
  class DeactivateTests {

    @Test
    void deactivate_shouldDeactivateUserAndReturnNoContent_whenUserExists() throws Exception {
      // given
      Long userId = DEFAULT_ID;
      User user = UserTestData.buildUserEntity(userId, UserTestData.DEFAULT_EMAIL);
      user.setActive(true);
      userRepository.save(user);

      // when & then
      mockMvc.perform(patch(DEACTIVATE_USER_API_PATH, userId)).andExpect(status().isNoContent());

      // then
      User updatedUser = userRepository.findById(userId).orElseThrow();
      assertThat(updatedUser.getActive()).isFalse();
    }

    @Test
    void deactivate_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      // when & then
      mockMvc
          .perform(patch(DEACTIVATE_USER_API_PATH, NON_EXISTENT_USER_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.error", is("Not Found")))
          .andExpect(jsonPath("$.message", is(USER_NOT_FOUND.formatted(NON_EXISTENT_USER_ID))));
    }
  }
}
