package co.news.insight.schedule;

import co.news.insight.model.Category;
import co.news.insight.model.Schedule;
import co.news.insight.service.CategoryService;
import co.news.insight.service.NewsService;
import co.news.insight.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ScheduledTasks {

  private static final int REQUEST_BATCH_SIZE = 100;  // 한 번에 100개씩 처리

  private final ScheduleService scheduleService;
  private final NewsService newsService;
  private final CategoryService categoryService;

  @Scheduled(cron = "0 21 15 * * *", zone = "Asia/Seoul")
  public void fetchDailyNews() {
    List<Category> categories = categoryService.getAllCategories().stream()
        .filter(category -> !"기타".equals(category.getTitle()))  // "기타" 카테고리 제외
        .toList();

    List<CompletableFuture<Void>> futures = categories.stream()
        .map(this::processCategoryNewsAsync)  // 비동기 실행
        .collect(Collectors.toList());

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }

  @Async
  public CompletableFuture<Void> processCategoryNewsAsync(Category category) {
    return CompletableFuture.runAsync(() -> {
      Schedule schedule = scheduleService.getScheduleByCategory(category.getTitle());

      if (schedule == null) {
        scheduleService.createScheduleForCategory(category.getTitle(), category.getLimit() / REQUEST_BATCH_SIZE);
        schedule = scheduleService.getScheduleByCategory(category.getTitle());
      }

      int start = schedule.getCurrentStart();
      int totalProcessed = schedule.getTotalProcessed();
      int maxRequests = category.getLimit();

      while (totalProcessed < maxRequests) {
        try {
          newsService.fetchAndProcessNews(start, category.getTitle());
          System.out.println("Processed batch for category: " + category.getTitle() + " starting at: " + start);

          start += REQUEST_BATCH_SIZE;
          totalProcessed += REQUEST_BATCH_SIZE;

          scheduleService.updateSchedule(LocalDate.now(), start, totalProcessed, category.getTitle());

          Thread.sleep(10000);

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          System.out.println("Execution interrupted: " + e.getMessage());
          break;
        }

        if (totalProcessed >= maxRequests) {
          System.out.println("Limit for category " + category.getTitle() + " reached: " + maxRequests);
          break;
        }
      }

      if (totalProcessed >= maxRequests) {
        scheduleService.completeScheduleForCategory(category.getTitle());
      }
    });
  }
}
