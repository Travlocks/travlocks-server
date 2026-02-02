package org.umc.travlocksserver.domain.traveltheme.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.CreatedSoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "travel_themes")
public class TravelTheme extends CreatedSoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_theme_id")
    private Long id;

    @Column(name = "content", nullable = false, length = 20)
    private String content;
}
