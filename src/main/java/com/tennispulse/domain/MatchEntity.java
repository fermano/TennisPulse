package com.tennispulse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private ClubEntity club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id")
    private PlayerEntity player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private PlayerEntity player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private PlayerEntity winner;

    private Instant startTime;
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Column(name = "final_score", length = 64)
    private String finalScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

