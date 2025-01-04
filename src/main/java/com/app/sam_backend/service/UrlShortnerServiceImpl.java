package com.app.sam_backend.service;

import com.app.sam_backend.entity.Redirect;
import com.app.sam_backend.repository.RedirectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class UrlShortnerServiceImpl implements  UrlShortnerService{

    @Autowired(required = true)
    private RedirectRepository redirectRepository;

    @Override
    public Optional<String> createShortenedUrl(String name, String location, String version, String fullUrl) {
        String shortenedUrl = generateShortenedUrl();

        Optional<Redirect> existingRedirect = redirectRepository.findByNameAndLocationAndVersion(name, location, version);

        if (existingRedirect.isPresent()) {
            // If an entry exists, return a message asking to upgrade the version
            return Optional.empty();
        }

        Redirect redirect = Redirect.builder()
                .name(name)
                .location(location)
                .version(version)
                .shortenedUrl(shortenedUrl)
                .fullUrl(fullUrl)
                .build();

        redirectRepository.save(redirect);

        return Optional.of(shortenedUrl);
    }

    // Helper method to generate a random 6-character shortened URL
    private String generateShortenedUrl() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder shortened = new StringBuilder();
        Random random = new Random();

        // Generate 6 characters
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            shortened.append(chars.charAt(index));
        }

        return shortened.toString();
    }

}
