package com.app.sam_backend.controller;

import com.app.sam_backend.entity.Redirect;
import com.app.sam_backend.repository.RedirectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/inchArtDesigns")
public class UrlShortenerController {

    @Autowired(required = true)
    private RedirectRepository redirectRepository;


    // Generate the shortened URL and store it in the database
    @PostMapping("/shorten")
    public ResponseEntity<?> createShortenedUrl(@RequestBody Map<String, String> request) {
        String fullUrl = request.get("fullUrl");

        if (fullUrl == null || fullUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid URL");
        }

        String shortenedUrl = generateShortenedUrl();

        Redirect redirect = new Redirect();
        redirect.setShortenedUrl(shortenedUrl);
        redirect.setFullUrl(fullUrl);

        redirectRepository.save(redirect);

        Map<String, String> response = new HashMap<>();
        response.put("shortenedUrl", "http://localhost:8080/" + shortenedUrl);

        return ResponseEntity.ok(response);
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

    @GetMapping("/{shortenedUrl}")
    public ResponseEntity<String> redirectToOriginalUrl(@PathVariable String shortenedUrl) {

        System.out.println("testing...");
        // Fetch the original URL from the database
        Optional<Redirect> mapping = redirectRepository.findByShortenedUrl(shortenedUrl);

        return mapping
                .map(redirect -> {
                    String originalUrl = redirect.getFullUrl();
                    // Create HTML content with iframe
                    String htmlContent = "<html><body>" +
                            "<iframe src='" + originalUrl + "' width='100%' height='100%' style='border:none;'></iframe>" +
                            "</body></html>";
                    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlContent);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}

