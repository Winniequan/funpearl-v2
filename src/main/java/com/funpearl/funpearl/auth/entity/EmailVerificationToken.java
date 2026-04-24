package com.funpearl.funpearl.auth.entity;

import com.funpearl.funpearl.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@Data
@NoArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    public EmailVerificationToken(User user, long expirationMs) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = Instant.now().plusMillis(expirationMs);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}
