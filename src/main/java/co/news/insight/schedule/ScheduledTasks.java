package co.news.insight.schedule;

import co.news.insight.model.Schedule;
import co.news.insight.service.NewsService;
import co.news.insight.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ScheduledTasks {

  private final ScheduleService scheduleService;
  private final NewsService newsService;

  @Scheduled(cron = "0 0 * * * *")
  public void fetchDailyNews() {
    Schedule schedule = scheduleService.getCurrentSchedule();

    if (schedule == null) {
      scheduleService.createSchedule(250);  // totalRequests를 250으로 설정
      schedule = scheduleService.getCurrentSchedule();
    }

    int start = schedule.getCurrentStart();
    int totalProcessed = schedule.getTotalProcessed();

    for (int i = 0; i < 200; i++) {
      newsService.fetchAndProcessNews(start);
      start += 100;
      totalProcessed += 100;

      scheduleService.updateSchedule(start, totalProcessed);
    }

    scheduleService.completeSchedule();
  }

}
