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
}
