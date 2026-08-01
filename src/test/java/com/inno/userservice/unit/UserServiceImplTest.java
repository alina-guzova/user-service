package com.inno.userservice.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.inno.userservice.dto.request.UserCreateRequestDTO;
import com.inno.userservice.dto.request.UserFilterDTO;
import com.inno.userservice.dto.request.UserUpdateRequestDTO;
import com.inno.userservice.dto.response.UserResponseDTO;
import com.inno.userservice.entity.User;
import com.inno.userservice.exception.BaseAppException;
import com.inno.userservice.mapper.UserMapper;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.service.impl.UserServiceImpl;
import com.inno.userservice.util.UserTestData;
import jakarta.persistence.EntityManager;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private UserMapper userMapper;

  @Mock private EntityManager entityManager;

  @InjectMocks private UserServiceImpl userService;

  @Nested
  class CreateTests {

    @Test
    void whenCreateUser_thenUserIsSavedAndReturned() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserCreateRequestDTO requestDto = UserTestData.buildCreateRequestDto();
      User mappedUser = UserTestData.buildUserEntity(null, requestDto.email());
      UserResponseDTO expectedResponse = UserTestData.buildResponseDto();

      when(userRepository.existsById(userId)).thenReturn(false);
      when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);
      when(userMapper.toEntity(requestDto)).thenReturn(mappedUser);
      when(userMapper.toDto(mappedUser)).thenReturn(expectedResponse);

      // when
      UserResponseDTO actualResponse = userService.create(userId, requestDto);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(userRepository).existsById(userId);
      verify(userRepository).existsByEmail(requestDto.email());
      verify(userMapper).toEntity(requestDto);
      verify(entityManager).persist(mappedUser);
      verify(userMapper).toDto(mappedUser);

      assertThat(mappedUser.getId()).isEqualTo(userId);
    }

    @Test
    void whenCreateUserWithExistingId_thenThrowConflictException() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserCreateRequestDTO requestDto = UserTestData.buildCreateRequestDto();

      when(userRepository.existsById(userId)).thenReturn(true);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> userService.create(userId, requestDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

      verify(userRepository).existsById(userId);
      verifyNoMoreInteractions(userRepository);
      verifyNoInteractions(userMapper, entityManager);
    }

    @Test
    void whenCreateUserWithExistingEmail_thenThrowConflictException() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserCreateRequestDTO requestDto = UserTestData.buildCreateRequestDto();

      when(userRepository.existsByEmail(requestDto.email())).thenReturn(true);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> userService.create(userId, requestDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

      verify(userRepository).existsById(userId);
      verify(userRepository).existsByEmail(requestDto.email());
      verifyNoInteractions(userMapper);
      verifyNoMoreInteractions(userRepository);
    }
  }

  @Nested
  class UpdateTests {

    @Test
    void whenUpdateUser_thenUserIsUpdatedAndReturned() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserUpdateRequestDTO updateDto = UserTestData.buildUpdateRequestDto();
      User existingUser = UserTestData.buildUserEntity();
      UserResponseDTO expectedResponse = UserTestData.buildResponseDto();

      when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
      doNothing().when(userMapper).updateEntity(updateDto, existingUser);

      when(userRepository.save(existingUser)).thenReturn(existingUser);

      when(userMapper.toDto(existingUser)).thenReturn(expectedResponse);

      // when
      UserResponseDTO actualResponse = userService.update(userId, updateDto);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(userRepository).findById(userId);
      verify(userMapper).updateEntity(updateDto, existingUser);
      verify(userRepository).save(existingUser);
      verify(userMapper).toDto(existingUser);
    }

    @Test
    void whenUpdateUserWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long userId = 999L;
      UserUpdateRequestDTO updateDto = UserTestData.buildUpdateRequestDto();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> userService.update(userId, updateDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(userRepository).findById(userId);
      verifyNoInteractions(userMapper);
    }
  }

  @Nested
  class GetByIdTests {

    @Test
    void whenGetUserById_thenUserIsReturned() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      User user = UserTestData.buildUserEntity();
      UserResponseDTO expectedResponse = UserTestData.buildResponseDto();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userMapper.toDto(user)).thenReturn(expectedResponse);

      // when
      UserResponseDTO actualResponse = userService.getById(userId);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(userRepository).findById(userId);
      verify(userMapper).toDto(user);
    }

    @Test
    void whenGetUserByIdWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long userId = 999L;

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> userService.getById(userId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(userRepository).findById(userId);
      verifyNoInteractions(userMapper);
    }
  }

  @Nested
  class GetAllTests {

    @Test
    void whenGetAllUsers_thenPageOfUsersIsReturned() {
      // given
      UserFilterDTO filter = UserTestData.buildFilterDto();
      Pageable pageable = PageRequest.of(0, 10);

      User user1 = UserTestData.buildUserEntity(1L, "alice@gmail.com");
      User user2 = UserTestData.buildUserEntity(2L, "britt@gmail.com");

      UserResponseDTO responseDto1 = UserTestData.buildResponseDto(1L, "alice@gmail.com");
      UserResponseDTO responseDto2 = UserTestData.buildResponseDto(2L, "britt@gmail.com");

      List<User> users = List.of(user1, user2);
      Page<User> userPage = new PageImpl<>(users, pageable, users.size());

      when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
      when(userMapper.toDto(user1)).thenReturn(responseDto1);
      when(userMapper.toDto(user2)).thenReturn(responseDto2);

      // when
      Page<UserResponseDTO> actualPage = userService.getAll(filter, pageable);

      // then
      assertThat(actualPage).isNotNull();
      assertThat(actualPage.getContent()).hasSize(2).containsExactly(responseDto1, responseDto2);

      verify(userRepository).findAll(any(Specification.class), eq(pageable));
      verify(userMapper).toDto(user1);
      verify(userMapper).toDto(user2);
    }

    @Test
    void whenGetAllUsersWithEmptyFilter_thenPageOfUsersIsReturned() {
      // given
      UserFilterDTO emptyFilter = UserTestData.buildEmptyFilterDto();
      Pageable pageable = PageRequest.of(0, 10);

      User user = UserTestData.buildUserEntity();
      UserResponseDTO responseDto = UserTestData.buildResponseDto();

      List<User> users = List.of(user);
      Page<User> userPage = new PageImpl<>(users, pageable, users.size());

      when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
      when(userMapper.toDto(user)).thenReturn(responseDto);

      // when
      Page<UserResponseDTO> actualPage = userService.getAll(emptyFilter, pageable);

      // then
      assertThat(actualPage).isNotNull();
      assertThat(actualPage.getContent()).hasSize(1).containsExactly(responseDto);

      verify(userRepository).findAll(any(Specification.class), eq(pageable));
      verify(userMapper).toDto(user);
    }
  }

  @Nested
  class ActivateTests {

    @Test
    void whenActivateUser_thenUserIsActivated() {
      // given
      Long userId = UserTestData.DEFAULT_ID;

      when(userRepository.activateUser(userId)).thenReturn(1);

      // when
      userService.activate(userId);

      // then
      verify(userRepository).activateUser(userId);
    }

    @Test
    void whenActivateUserWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long userId = 999L;

      when(userRepository.activateUser(userId)).thenReturn(0);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> userService.activate(userId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(userRepository).activateUser(userId);
    }
  }

  @Nested
  class DeactivateTests {

    @Test
    void whenDeactivateUser_thenUserIsDeactivated() {
      // given
      Long userId = UserTestData.DEFAULT_ID;

      when(userRepository.deactivateUser(userId)).thenReturn(1);

      // when
      userService.deactivate(userId);

      // then
      verify(userRepository).deactivateUser(userId);
    }

    @Test
    void whenDeactivateUserWithNonExistentId_thenThrowNotFoundException() {
      // given
      Long userId = 999L;

      when(userRepository.deactivateUser(userId)).thenReturn(0);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> userService.deactivate(userId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(userRepository).deactivateUser(userId);
    }
  }
}
