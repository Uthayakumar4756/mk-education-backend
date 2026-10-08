package com.mkcareer.repository;

import com.mkcareer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndPasswordAndStatus(
        String username,
        String password,
        Integer status
    );
}