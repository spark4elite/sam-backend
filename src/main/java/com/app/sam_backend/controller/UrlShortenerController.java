package com.app.sam_backend.controller;

import com.app.sam_backend.entity.ChangeStatusRequest;
import com.app.sam_backend.entity.Redirect;
import com.app.sam_backend.repository.RedirectRepository;
import com.app.sam_backend.service.UrlShortnerService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class UrlShortenerController {

    @Value("${host.domain}")
    String domain;

    @Autowired(required = true)
    private RedirectRepository redirectRepository;

    @Autowired
    UrlShortnerService urlShortnerService;

    @PostMapping("/shorten")
    @Operation(
            summary = "API to add the URL to the Database"
    )
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

        fullUrl = fullUrl.trim();

        // Check if the full URL is valid by making an HTTP request
        if (!isValidUrl(fullUrl)) {
            return "Invalid URL";
        }

        Optional<String> shortenedUrl = urlShortnerService.createShortenedUrl(name.trim(), location.trim(), version.trim(), fullUrl);

        return shortenedUrl.map(s -> domain + "/" + s + "?name=" + name.trim() + "&location=" + location.trim() + "&version=" + version.trim())
                .orElse("Entry already exists, please upgrade the version.");
    }

    private boolean isValidUrl(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Send a GET request to the full URL
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // Check if the status code is 200 OK
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            // If any exception occurs (e.g., invalid URL, connection failure), return false
            return false;
        }
    }

    @GetMapping("/all")
    @Operation(
            summary = "Search for All Entries",
            description = "This endpoint allows users to search entire database entries."
    )
    public List<Redirect> getAllRedirects() {
        // Fetch all entries from the Redirect repository
        return redirectRepository.findAll()
                .stream()
                .peek(redirect -> {
                    // Modify the shortenedUrl to include the domain and name
                    String modifiedShortenedUrl = domain + "/" + redirect.getShortenedUrl()
                            + "?name=" + redirect.getName() + "&location=" + redirect.getLocation()
                            + "&version=" + redirect.getVersion();
                    redirect.setShortenedUrl(modifiedShortenedUrl);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/searchByName")
    @Operation(
            summary = "Search Redirects by Name",
            description = "This endpoint allows users to search for redirects by a partial or full match of the `name` field. The search is case-insensitive and returns a list of matching redirects with the modified shortened URL."
    )
    public List<Redirect> searchByName(@RequestParam String name) {
        // Fetch entries that match the partial name
        return redirectRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .peek(redirect -> {
                    // Modify the shortenedUrl to include the domain and name
                    String modifiedShortenedUrl = domain + "/" + redirect.getShortenedUrl()
                            + "?name=" + redirect.getName() + "&location=" + redirect.getLocation()
                            + "&version=" + redirect.getVersion();
                    redirect.setShortenedUrl(modifiedShortenedUrl);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/searchByLocation")
    @Operation(
            summary = "Search Redirects by Location",
            description = "This endpoint allows users to search for redirects by a partial or full match of the `location` field. The search is case-insensitive and returns a list of matching redirects with the modified shortened URL."
    )
    public List<Redirect> searchByLocation(@RequestParam String name) {
        // Fetch entries that match the partial name
        return redirectRepository.findByLocationContainingIgnoreCase(name)
                .stream()
                .peek(redirect -> {
                    // Modify the shortenedUrl to include the domain and name
                    String modifiedShortenedUrl = domain + "/" + redirect.getShortenedUrl()
                            + "?name=" + redirect.getName() + "&location=" + redirect.getLocation()
                            + "&version=" + redirect.getVersion();
                    redirect.setShortenedUrl(modifiedShortenedUrl);
                })
                .collect(Collectors.toList());
    }


    @GetMapping("/{shortenedUrl}")
    @Hidden
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

        if (mapping.isPresent()) {
            String htmlContent = "<html>" +
                    "<head><title>The Elite Touch</title></head>" +
                    "<body>" +
                    "<h1>This url is blocked, contact admin for more details :)</h1>" +
                    "</body>" +
                    "</html>";
            if (!mapping.get().isStatus()) {
                return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlContent);
            }
        }

        return mapping
                .map(redirect -> {
                    String originalUrl = redirect.getFullUrl();
                    // Create HTML content with iframe and title properly structured
                    String htmlContent = "<html>" +
                            "<head><title>The Elite Touch</title></head>" +
                            "<body>" +
                            "<iframe src='" + originalUrl + "' width='100%' height='100%' style='border:none;'></iframe>" +
                            "</body>" +
                            "</html>";
                    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlContent);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());

    }


    @PatchMapping("/changeStatus")
    @Operation(
            summary = "API to change the status of a Redirect",
            description = "Allows admin to update the status (active/inactive) of a Redirect entry by its ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request body containing the ID and new status of the Redirect entry",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangeStatusRequest.class))
            )
    )
    public ResponseEntity<String> changeStatus(@RequestParam(name = "id") Long id, @RequestParam(name = "status") boolean status) {
        Optional<Redirect> redirectOptional = redirectRepository.findById(id);

        if (redirectOptional.isPresent()) {
            Redirect redirect = redirectOptional.get();
            redirect.setStatus(status);
            redirectRepository.save(redirect);
            return ResponseEntity.ok("Status updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Redirect entry not found.");
        }
    }
}

