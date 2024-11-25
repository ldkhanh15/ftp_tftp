package com.java.repository;

import com.java.dto.UserDTO;
import com.java.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    @Query("SELECT new com.java.dto.UserDTO(u.id, u.username, u.role) FROM User u WHERE u.username = :username")
    Optional<UserDTO> findUserDTOByUsername(@Param("username") String username);

    @Transactional
    void deleteByUsername(String username);

    List<User> findByUsernameContaining(String keyword);
}
