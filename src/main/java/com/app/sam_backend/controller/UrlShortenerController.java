package com.app.sam_backend.controller;

import com.app.sam_backend.entity.Redirect;
import com.app.sam_backend.repository.RedirectRepository;
import com.app.sam_backend.service.UrlShortnerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class UrlShortenerController {

    @Value("${host.domain}")
    String domain;

    @Autowired(required = true)
    private RedirectRepository redirectRepository;

    @Autowired
    UrlShortnerService urlShortnerService;

    // Generate the shortened URL and store it in the database
    @PostMapping("/shorten")
    public String createShortenedUrl(
            @RequestParam String name,
            @RequestParam String location,
            @RequestParam(defaultValue = "v1") String version,
            @Parameter(
                    description = "Full URL to be shortened",
                    required = true,
                    schema = @Schema(type = "string", example = "")
            ) String fullUrl) {

        if (fullUrl == null || fullUrl.isEmpty()) {
            return "Invalid URL";
        }

        Optional<String> shortenedUrl = urlShortnerService.createShortenedUrl(name, location, version, fullUrl);

        return shortenedUrl.map(s -> domain + "/" + s + "?name=" + name + "&location=" + location + "&version=" + version).orElse("Entry already exists, please upgrade the version.");
    }

    @GetMapping("/all")
    public List<Redirect> getAllRedirects() {
        // Fetch all entries from the Redirect repository
        return redirectRepository.findAll();
    }

    @GetMapping("/{shortenedUrl}")
    public ResponseEntity<String> getFullUrl(
            @PathVariable String shortenedUrl,
            @RequestParam String name,
            @RequestParam String location,
            @RequestParam(defaultValue = "v1") String version) {

        // Validate required parameters (name, location, shortenedUrl)
        if (name == null || location == null || shortenedUrl == null || name.isEmpty() || location.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing or invalid parameters");
        }

        // Fetch the corresponding redirect mapping from the database
        Optional<Redirect> mapping = redirectRepository.findByShortenedUrlAndNameAndLocationAndVersion(shortenedUrl, name, location, version);

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

