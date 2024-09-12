package co.news.insight.schedule;

import co.news.insight.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ScheduledTasks {

  private final NewsService newsService;

  //  @Scheduled(cron = "*/1 * * * * *")
  public void fetchDailyNews() {
    int start = 1;
    int totalRequests = 250; // 25,000개를 100개씩 250번 요청

    for (int i = 0; i < totalRequests; i++) {
      newsService.fetchAndProcessNews(start);
      start += 100;
    }
  }
}
