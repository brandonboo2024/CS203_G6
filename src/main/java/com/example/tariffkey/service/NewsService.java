package com.example.tariffkey.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Service
public class NewsService {
    @Value("${NEWS_API_KEY}")
    String NEWS_API_KEY; 
    private final RestTemplate restTemplate = new RestTemplate();
    private String getNewsUrl() {
        return "https://newsdata.io/api/1/news?apikey=" + NEWS_API_KEY + "&q=tariff+trade+import+export&language=en&country=sg";
    }
    private List<Map<String, String>> cachedNews = new ArrayList<>();
    private long lastFetchTime = 0;

    public List<Map<String, String>> getLatestNews() {
        long now = System.currentTimeMillis();
        if (now - lastFetchTime < 24 * 60 * 60 * 1000 && !cachedNews.isEmpty()) {
            return cachedNews;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (TariffKey/1.0)");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    getNewsUrl(), HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.out.println("[NewsService] Failed to fetch news: " + response.getStatusCode());
                return cachedNews;
            }

            String json = response.getBody();
            if (json == null) return cachedNews;

            JSONObject obj = new JSONObject(json);
            JSONArray articles = obj.optJSONArray("results");
            if (articles == null) return cachedNews;

            List<Map<String, String>> results = new ArrayList<>();
            for (int i = 0; i < Math.min(6, articles.length()); i++) {
                JSONObject a = articles.getJSONObject(i);
                Map<String, String> article = new HashMap<>();
                article.put("title", a.optString("title", "No title"));
                article.put("url", a.optString("link", "#"));
                article.put("source", a.optString("source_id", "NewsData.io"));
                article.put("date", a.optString("pubDate", ""));
                results.add(article);
            }

            cachedNews = results;
            lastFetchTime = now;
            System.out.println("[NewsService] Successfully fetched " + results.size() + " articles.");
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return cachedNews;
        }
    }
}
