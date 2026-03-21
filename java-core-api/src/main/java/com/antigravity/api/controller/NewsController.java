package com.antigravity.api.controller;

import com.antigravity.api.entity.News;
import com.antigravity.api.entity.User;
import com.antigravity.api.service.NewsService;
import com.antigravity.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final UserService userService;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }

    @GetMapping
    public ResponseEntity<Page<News>> getNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt,desc") String sort,
            @RequestParam(required = false) List<String> symbols,
            @RequestParam(defaultValue = "false") boolean watchlistOnly) {

        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        User user = getAuthenticatedUser();
        Page<News> newsPage = newsService.getNews(user, symbols, watchlistOnly, pageable);
        return ResponseEntity.ok(newsPage);
    }

    @PostMapping("/weekly-report")
    public ResponseEntity<Map<String, Object>> generateWeeklyReport() {
        User user = getAuthenticatedUser();
        Map<String, Object> report = newsService.generateWeeklyReport(user);
        return ResponseEntity.ok(report);
    }
}
