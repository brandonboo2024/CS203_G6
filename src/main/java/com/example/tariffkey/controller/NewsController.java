package com.example.tariffkey.controller;

import com.example.tariffkey.service.NewsService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "http://localhost:5173") 
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<Map<String, String>> getNews() {
        return newsService.getLatestNews();
    }
}
