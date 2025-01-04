package com.app.sam_backend.repository;


import com.app.sam_backend.entity.Redirect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RedirectRepository extends JpaRepository<Redirect, Long> {
    Optional<Redirect> findByShortenedUrlAndNameAndLocationAndVersion(String shortenedUrl, String name, String location, String version);

    Optional<Redirect> findByNameAndLocationAndVersion(String name, String location, String version);

    List<Redirect> findByNameContainingIgnoreCase(String name);

    List<Redirect> findByLocationContainingIgnoreCase(String name);
}
