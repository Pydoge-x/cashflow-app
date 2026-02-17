package com.cashflow.app.repository;

import com.cashflow.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone);

    Boolean existsByUsername(String username);
}
