package com.bootcamp.paymentdemo.common.token.entity;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 1000)
    private String refreshToken;

    private LocalDateTime expirationAt;

    private RefreshToken(Member member, String refreshToken, LocalDateTime expirationAt) {
        this.member = member;
        this.refreshToken = refreshToken;
        this.expirationAt = expirationAt;
    }

    public static RefreshToken register(Member member, String refreshToken, LocalDateTime expirationAt) {
       return new RefreshToken(member, refreshToken, expirationAt);
    }
}
