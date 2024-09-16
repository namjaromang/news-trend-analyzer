package co.news.insight.service;

import co.news.insight.model.Schedule;
import co.news.insight.model.ScheduleStatus;
import co.news.insight.repository.ScheduleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;

  public Schedule getCurrentSchedule(String categoryTitle) {
    return scheduleRepository.findByStatusAndCategoryTitle(ScheduleStatus.PROGRESS, categoryTitle);
  }

  public void updateSchedule(LocalDate currentStartDate, int currentStart, int totalProcessed, String categoryTitle) {
    Schedule schedule = getCurrentSchedule(categoryTitle);
    schedule.updateProgress(currentStartDate, currentStart, totalProcessed); // 세터 대신 메서드 사용
    scheduleRepository.save(schedule);
  }

  public Schedule getScheduleByCategory(String categoryTitle) {
    return scheduleRepository.findByCategoryTitle(categoryTitle);
  }

  public void completeScheduleForCategory(String categoryTitle) {
    Schedule schedule = getScheduleByCategory(categoryTitle);
    if (schedule != null) {
      schedule.markAsCompleted(); // 세터 대신 메서드 사용
      scheduleRepository.save(schedule);
    }
  }

  public void createScheduleForCategory(String categoryTitle, int categoryLimit) {
    Schedule newSchedule = Schedule.builder()
        .categoryTitle(categoryTitle)
        .totalRequests(categoryLimit)
        .currentStart(1)
        .totalProcessed(0)
        .status(ScheduleStatus.PROGRESS)
        .lastProcessed(LocalDateTime.now())
        .build();

    scheduleRepository.save(newSchedule);
  }
}

