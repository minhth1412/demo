package com.assessment.demo.repository;

import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findById(UUID tokenId);

    Optional<Token> findByUser(User user);
}
