package org.umc.travlocksserver.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "travel_themes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TravelTheme extends SoftDeleteBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_theme_id")
    private Long id;

    @Column(name = "content", length = 10, nullable = false)
    private String content;
}

