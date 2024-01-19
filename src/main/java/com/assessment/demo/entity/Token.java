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

    @Column(name = "token")
    private byte[] CompressedTokenData;

    @Column(name = "refreshToken")
    private byte[] CompressedRefreshTokenData;

    @Column(name = "tokenExpireAt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenExpireAt;

    @Column(name = "refreshTokenExpireAt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date refreshTokenExpireAt;

    @OneToOne(mappedBy = "token", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    public Token(byte[] CompressedTokenData, byte[] CompressedRefreshTokenData){
        super();
        this.tokenId = UUID.randomUUID();
        this.CompressedTokenData = CompressedTokenData;
        this.CompressedRefreshTokenData = CompressedRefreshTokenData;
    }
}
