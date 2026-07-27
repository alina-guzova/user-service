package com.inno.userservice.repository.specification;

import com.inno.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

  public static Specification<User> hasName(String name) {

    return (root, query, cb) -> {
      if (name != null && !name.isBlank()) {
        return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
      }
      return cb.conjunction();
    };
  }

  public static Specification<User> hasSurname(String surname) {

    return (root, query, cb) -> {
      if (surname != null && !surname.isBlank()) {
        return cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
      }
      return cb.conjunction();
    };
  }
}
