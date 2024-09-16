package co.news.insight.schedule;

import co.news.insight.model.Category;
import co.news.insight.model.Schedule;
import co.news.insight.service.CategoryService;
import co.news.insight.service.DeepSerchService;
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
public class DeepSearchScheduledTasks {

  private static final int REQUEST_BATCH_SIZE = 100;  // 한 번에 100개씩 처리
  private static final LocalDate START_DATE = LocalDate.of(2024, 9, 2);  // 시작 날짜
  private static final LocalDate END_DATE = LocalDate.of(2020, 1, 1);    // 종료 날짜

  private final ScheduleService scheduleService;
  private final DeepSerchService deepSerchService;
  private final CategoryService categoryService;

  //@Scheduled(cron = "0 03 2 * * *", zone = "Asia/Seoul")
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
        scheduleService.createScheduleForCategory(category.getTitle(), REQUEST_BATCH_SIZE);
        schedule = scheduleService.getScheduleByCategory(category.getTitle());
      }

      LocalDate currentStartDate = schedule.getCurrentStartDate();  // 스케줄에서 현재 날짜 가져오기
      if (currentStartDate == null) {
        currentStartDate = START_DATE;  // 스케줄이 없으면 시작 날짜 설정
      }

      int totalProcessed = schedule.getTotalProcessed();

      while (currentStartDate.isAfter(END_DATE)) {
        try {
          // 하루치 데이터를 처리 (start와 end는 동일한 날짜)
          deepSerchService.fetchAndProcessNews(currentStartDate, currentStartDate, category.getTitle());
          System.out.println("Processed batch for category: " + category.getTitle() + " on: " + currentStartDate);

          // 다음 날로 이동
          currentStartDate = currentStartDate.minusDays(1);  // 날짜를 하루 줄임
          totalProcessed += 1;

          // 스케줄 정보 업데이트
          scheduleService.updateSchedule(currentStartDate, 1, totalProcessed, category.getTitle());

          Thread.sleep(10000);  // 10초 대기

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          System.out.println("Execution interrupted: " + e.getMessage());
          break;
        }

        if (currentStartDate.isBefore(END_DATE)) {
          System.out.println("Reached the end date for category " + category.getTitle());
          break;
        }
      }

      if (currentStartDate.isBefore(END_DATE)) {
        scheduleService.completeScheduleForCategory(category.getTitle());
      }
    });
  }
}
