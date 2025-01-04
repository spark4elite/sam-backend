package com.app.sam_backend.service;

import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UrlShortnerService {

    Optional<String> createShortenedUrl(String name, String location, String version, String fullUrl);
}
