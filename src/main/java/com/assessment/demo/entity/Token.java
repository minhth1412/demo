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

    @OneToOne(mappedBy = "token", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private User user;

    public Token() {
        super();
    }

    public Token(String token, String refreshToken, Date tokenExpireAt, Date refreshTokenExpireAt){
        super();
        this.updateTimeExpired(tokenExpireAt, refreshTokenExpireAt);
        this.tokenId = UUID.randomUUID();
        this.CompressedTokenData = token;
        this.CompressedRefreshTokenData = refreshToken;
    }

    // Method to update new expiration time of current tokens
    public void updateTimeExpired(Date tokenExpireAt, Date refreshTokenExpireAt) {
        this.tokenExpireAt = tokenExpireAt;
        this.refreshTokenExpireAt = refreshTokenExpireAt;
    }

    public void updateToken(String token, String refreshToken, Date tokenExpireAt, Date refreshTokenExpireAt) {
        this.CompressedTokenData = token;
        this.CompressedRefreshTokenData = refreshToken;
        this.updateTimeExpired(tokenExpireAt, refreshTokenExpireAt);
        this.setUpdatedAt(new Date());
    }
}
