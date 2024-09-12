package co.news.insight.repository;

import co.news.insight.model.KeywordSummary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordSummaryRepository extends JpaRepository<KeywordSummary, Long> {

  List<KeywordSummary> findAllByYearEquals(int year);

  List<KeywordSummary> findAllByMonthEquals(int month);

  List<KeywordSummary> findAllByDayEquals(int day);

}
