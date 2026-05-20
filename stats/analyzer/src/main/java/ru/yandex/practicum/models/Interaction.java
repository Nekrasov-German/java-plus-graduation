package ru.yandex.practicum.models;

import lombok.*;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "interactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Float rating;

    @Column(name = "ts", nullable = false)
    private Instant timestamp;
}
