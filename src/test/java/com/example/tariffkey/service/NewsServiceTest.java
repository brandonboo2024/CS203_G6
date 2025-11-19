package com.example.tariffkey.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NewsServiceTest {

    private NewsService newsService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws Exception {
        newsService = new NewsService();
        restTemplate = mock(RestTemplate.class);

        // Inject mock RestTemplate
        Field restField = NewsService.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(newsService, restTemplate);

        // Set fake API key
        Field apiKeyField = NewsService.class.getDeclaredField("newsApiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(newsService, "fake_api_key");
    }

    @Test
    void testGetLatestNewsSuccess() {
        String fakeJson = """
            {
              "results": [
                {"title":"Article 1","link":"url1","source_id":"Source1","pubDate":"2025-01-01"},
                {"title":"Article 2","link":"url2","source_id":"Source2","pubDate":"2025-01-02"}
              ]
            }
        """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(fakeJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        List<Map<String, String>> news = newsService.getLatestNews();

        assertEquals(2, news.size());
        assertEquals("Article 1", news.get(0).get("title"));
        assertEquals("url2", news.get(1).get("url"));
    }

    @Test
    void testGetLatestNewsNoApiKey() throws Exception {
        // Clear API key
        Field apiKeyField = NewsService.class.getDeclaredField("newsApiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(newsService, "");

        List<Map<String, String>> news = newsService.getLatestNews();
        assertTrue(news.isEmpty());
    }

    @Test
    void testGetLatestNews_ShouldReturnCachedNews_WhenCacheIsValid() throws Exception {
        // Arrange - First call to populate cache
        String fakeJson = """
            {
              "results": [
                {"title":"Cached Article","link":"url1","source_id":"Source1","pubDate":"2025-01-01"}
              ]
            }
        """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(fakeJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // First call to populate cache
        List<Map<String, String>> firstCall = newsService.getLatestNews();

        // Act - Second call within 24 hours should return cached data
        List<Map<String, String>> secondCall = newsService.getLatestNews();

        // Assert
        assertEquals(1, secondCall.size());
        assertEquals("Cached Article", secondCall.get(0).get("title"));

        // Verify restTemplate was only called once (cache hit on second call)
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testGetLatestNews_ShouldReturnCachedNews_WhenResponseIsNot2xx() {
        // Arrange
        ResponseEntity<String> badResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(badResponse);

        // Act
        List<Map<String, String>> news = newsService.getLatestNews();

        // Assert
        assertTrue(news.isEmpty());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testGetLatestNews_ShouldReturnCachedNews_WhenResponseBodyIsNull() {
        // Arrange
        ResponseEntity<String> nullBodyResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(nullBodyResponse);

        // Act
        List<Map<String, String>> news = newsService.getLatestNews();

        // Assert
        assertTrue(news.isEmpty());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testGetLatestNews_ShouldReturnCachedNews_WhenArticlesArrayIsNull() {
        // Arrange
        String jsonWithoutResults = """
            {
              "status": "success"
            }
        """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonWithoutResults, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        List<Map<String, String>> news = newsService.getLatestNews();

        // Assert
        assertTrue(news.isEmpty());
    }

    @Test
    void testGetLatestNews_ShouldHandleException_WhenRestTemplateThrowsException() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act
        List<Map<String, String>> news = newsService.getLatestNews();

        // Assert
        assertTrue(news.isEmpty());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testGetLatestNews_ShouldLimitTo6Articles_WhenMoreThan6ArticlesReturned() {
        // Arrange
        String jsonWithManyArticles = """
            {
              "results": [
                {"title":"Article 1","link":"url1","source_id":"S1","pubDate":"2025-01-01"},
                {"title":"Article 2","link":"url2","source_id":"S2","pubDate":"2025-01-02"},
                {"title":"Article 3","link":"url3","source_id":"S3","pubDate":"2025-01-03"},
                {"title":"Article 4","link":"url4","source_id":"S4","pubDate":"2025-01-04"},
                {"title":"Article 5","link":"url5","source_id":"S5","pubDate":"2025-01-05"},
                {"title":"Article 6","link":"url6","source_id":"S6","pubDate":"2025-01-06"},
                {"title":"Article 7","link":"url7","source_id":"S7","pubDate":"2025-01-07"},
                {"title":"Article 8","link":"url8","source_id":"S8","pubDate":"2025-01-08"}
              ]
            }
        """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonWithManyArticles, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        List<Map<String, String>> news = newsService.getLatestNews();

        // Assert
        assertEquals(6, news.size());
        assertEquals("Article 1", news.get(0).get("title"));
        assertEquals("Article 6", news.get(5).get("title"));
    }

    @Test
    void testGetLatestNews_ShouldHandleMissingFields_WithDefaultValues() {
        // Arrange
        String jsonWithMissingFields = """
            {
              "results": [
                {"title":"Only Title"}
              ]
            }
        """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonWithMissingFields, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        List<Map<String, String>> news = newsService.getLatestNews();

        // Assert
        assertEquals(1, news.size());
        assertEquals("Only Title", news.get(0).get("title"));
        assertEquals("#", news.get(0).get("url"));
        assertEquals("NewsData.io", news.get(0).get("source"));
        assertEquals("", news.get(0).get("date"));
    }
}
