package com.app.sam_backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "redirect",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "location", "version"})  // Enforcing the unique combination
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Redirect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "version")
    private String version;

    @Column(name = "full_url", nullable = false, columnDefinition = "TEXT")
    private String fullUrl;

    @Column(name = "shortened_url", nullable = false)
    private String shortenedUrl;

    @Column(name = "status", nullable = false)
    private boolean status;

}
