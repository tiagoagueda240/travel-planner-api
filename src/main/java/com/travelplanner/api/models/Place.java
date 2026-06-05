package com.travelplanner.api.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "places")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    @JsonBackReference
    private Day day;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    private String type; // monument | restaurant | museum | park | beach | shopping | entertainment

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "place_photos", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "url", length = 1000)
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    private Integer priceKids;
    private Integer priceStudents;
    private Integer priceAdults;

    private Integer orderIndex;

    // User-editable fields (added after visit)
    @Builder.Default
    private boolean visited = false;

    @Column(length = 1000)
    private String personalNote;
}
