package com.app.sam_backend.controller;

import com.app.sam_backend.entity.Redirect;
import com.app.sam_backend.repository.RedirectRepository;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/")
public class UrlShortenerController {

    @Autowired(required = true)
    private RedirectRepository redirectRepository;

    private static final String BASE_URL = "http://localhost:8080";  // Replace with your base domain


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
    public ResponseEntity<?> serveContentFromFullUrl(@PathVariable String shortenedUrl) {
        Optional<Redirect> redirectOpt = redirectRepository.findByShortenedUrl(shortenedUrl);

        if (redirectOpt.isPresent()) {
            String fullUrl = redirectOpt.get().getFullUrl();

            try {
                // Use Selenium or any other headless browser to fetch the rendered HTML
                String htmlContent = fetchRenderedContent(fullUrl);

                // Rewrite relative URLs to absolute URLs
                String updatedHtml = rewriteRelativeUrls(htmlContent, fullUrl);

                // Set content type as HTML
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_HTML);

                // Return the content as HTML
                return new ResponseEntity<>(updatedHtml, headers, HttpStatus.OK);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Error fetching the content from the destination URL: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shortened URL not found.");
        }
    }

    // Helper method to fetch rendered content using Selenium
    private String fetchRenderedContent(String url) {
        // Set up Selenium WebDriver (e.g., ChromeDriver)
        WebDriver driver = new ChromeDriver();

        try {
            // Load the page
            driver.get(url);

            // Wait for the page to load completely
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

            // Get the rendered HTML
            return driver.getPageSource();
        } finally {
            driver.quit(); // Ensure the driver quits after usage
        }
    }

    // Helper method to rewrite relative URLs
    private String rewriteRelativeUrls(String html, String baseUrl) {
        try {
            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(html, baseUrl);

            document.select("[href]").forEach(element -> {
                String href = element.attr("href");
                if (!href.isEmpty()) {
                    element.attr("href", element.absUrl("href"));
                }
            });

            document.select("[src]").forEach(element -> {
                String src = element.attr("src");
                if (!src.isEmpty()) {
                    element.attr("src", element.absUrl("src"));
                }
            });

            return document.outerHtml();
        } catch (Exception e) {
            return html;
        }
    }

}
