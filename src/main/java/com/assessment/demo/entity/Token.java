package com.assessment.demo.entity;

import com.assessment.demo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "Token")
public class Token extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tokenId", columnDefinition = "BINARY(16)")
    private UUID tokenId;

    // ----------------------------------------------
    // AFTER SUCCESSFULLY SET UP THE PLAIN TOKEN,
    //  CHANGE INTO COMPRESS ONE OR SOMETHING ELSE
    // ----------------------------------------------
    @Column(name = "token")
    private String CompressedTokenData;

    @Column(name = "refreshToken")
    private String CompressedRefreshTokenData;

    @Column(name = "tokenExpireAt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenExpireAt;

    @Column(name = "refreshTokenExpireAt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date refreshTokenExpireAt;

    @OneToOne(mappedBy = "token", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    public Token() {
        this.updateTimeExpired();
    }

    public Token(String CompressedTokenData, String CompressedRefreshTokenData){
        super();
        this.updateTimeExpired();
        this.tokenId = UUID.randomUUID();
        this.CompressedTokenData = CompressedTokenData;
        this.CompressedRefreshTokenData = CompressedRefreshTokenData;
    }

    // Method to update new expiration time of current tokens
    public void updateTimeExpired() {
        this.setUpdatedAt(new Date());
        this.tokenExpireAt = this.getUpdatedAt();
        this.refreshTokenExpireAt = this.getUpdatedAt();
    }

    public void updateToken(String token, String refreshToken) {
        this.CompressedTokenData = token;
        this.CompressedRefreshTokenData = refreshToken;
        this.updateTimeExpired();
    }
}
