package com.inno.userservice.repository;

import com.inno.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  boolean existsByEmail(String email);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE User u SET u.active = true WHERE u.id = :id")
  int activateUser(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE User u SET u.active = false WHERE u.id = :id")
  int deactivateUser(@Param("id") Long id);

  @Query(value = "SELECT COUNT(*) FROM payment_cards WHERE user_id = :id", nativeQuery = true)
  long countCardsByUserId(@Param("id") Long id);
}
