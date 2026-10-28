package com.smartparking.repository;

import com.smartparking.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
    void delete(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long count();
}
