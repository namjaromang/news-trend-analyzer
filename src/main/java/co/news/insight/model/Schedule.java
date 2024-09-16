package co.news.insight.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "news_schedule")
@RequiredArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String categoryTitle;

  @Column
  private int totalRequests;

  @Column
  private int currentStart;

  @Column
  private LocalDate currentStartDate;

  @Column
  private int totalProcessed;

  @Column
  @Enumerated(EnumType.STRING)
  private ScheduleStatus status;

  @Column
  private LocalDateTime lastProcessed;

  @Builder
  public Schedule(LocalDate currentStartDate, String categoryTitle, int currentStart, Long id, LocalDateTime lastProcessed, ScheduleStatus status,
      int totalProcessed, int totalRequests) {
    this.categoryTitle = categoryTitle;
    this.currentStartDate = currentStartDate;
    this.currentStart = currentStart;
    this.id = id;
    this.lastProcessed = lastProcessed;
    this.status = status;
    this.totalProcessed = totalProcessed;
    this.totalRequests = totalRequests;
  }

  public void updateProgress(LocalDate currentStartDate, int currentStart, int totalProcessed) {
    this.currentStartDate = currentStartDate;
    this.currentStart = currentStart;
    this.totalProcessed = totalProcessed;
    this.status = ScheduleStatus.PROGRESS;
    this.lastProcessed = LocalDateTime.now();
  }

  public void markAsCompleted() {
    this.status = ScheduleStatus.COMPLETED;
    this.lastProcessed = LocalDateTime.now();
  }

}
