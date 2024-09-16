package co.news.insight.repository;

import co.news.insight.model.Schedule;
import co.news.insight.model.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  Schedule findByStatusAndCategoryTitle(ScheduleStatus inProgress, String categoryTitle);

  Schedule findByCategoryTitle(String categoryTitle);
}
