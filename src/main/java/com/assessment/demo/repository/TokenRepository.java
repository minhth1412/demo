package com.assessment.demo.repository;

import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByTokenId(UUID tokenId);

    @Query("SELECT u FROM User u JOIN Token t ON u.id = t.user.id WHERE t.id = :tokenId")
    List<User> findUsersByTokenId(@Param("tokenId") UUID tokenId);

    //@Modifying
//    @Query("DELETE t FROM Token t WHERE t.token = :token")
//    void deleteToken(@Param("token") String token);
}
