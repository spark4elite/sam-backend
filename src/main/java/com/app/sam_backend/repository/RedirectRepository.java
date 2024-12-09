package com.app.sam_backend.repository;


import com.app.sam_backend.entity.Redirect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RedirectRepository extends JpaRepository<Redirect, Long> {
    Optional<Redirect> findByShortenedUrl(String shortenedUrl);
}
