package co.news.insight.controller;

import co.news.insight.service.ChatgptService;
import co.news.insight.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

  private final NewsService newsService;
  private final ChatgptService chatgptService;

  @GetMapping("/fetch")
  public String fetchNews() {
    newsService.fetchAndProcessNews(1);
    return "Fetching news data. This process might take some time!";
  }

  @GetMapping("/analyze")
  public String analyzeNews() {
    chatgptService.analyzeAndSaveKeywords();
    return "analyze new data.";
  }

  @GetMapping("/analyze/year")
  public String analyzeNewsYear() {
    return "analyze new data.";
  }
}
