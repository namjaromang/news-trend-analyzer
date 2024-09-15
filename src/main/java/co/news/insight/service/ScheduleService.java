package co.news.insight.service;

import co.news.insight.model.Schedule;
import co.news.insight.model.ScheduleStatus;
import co.news.insight.repository.ScheduleRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;

  public Schedule getCurrentSchedule() {
    return scheduleRepository.findByStatus(ScheduleStatus.PROGRESS);
  }

  // 새로운 스케줄 생성
  public void createSchedule(int totalRequests) {
    Schedule newSchedule = Schedule.builder()
        .status(ScheduleStatus.PROGRESS)
        .currentStart(1)
        .lastProcessed(LocalDateTime.now())
        .totalRequests(totalRequests)
        .totalProcessed(0)
        .build();
    scheduleRepository.save(newSchedule);
  }

  public void updateSchedule(int currentStart, int totalProcessed) {
    Schedule schedule = getCurrentSchedule();
    schedule.updateProgress(currentStart, totalProcessed); // 세터 대신 메서드 사용
    scheduleRepository.save(schedule);
  }

  public void completeSchedule() {
    Schedule schedule = getCurrentSchedule();
    schedule.markAsCompleted(); // 세터 대신 메서드 사용
    scheduleRepository.save(schedule);
  }

  public void failSchedule() {
    Schedule schedule = getCurrentSchedule();
    schedule.markAsFailed(); // 실패 시 상태 변경
    scheduleRepository.save(schedule);
  }
}

