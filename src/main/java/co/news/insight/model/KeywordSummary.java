package co.news.insight.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "keyword_summary")
@RequiredArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class KeywordSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "article_id", referencedColumnName = "id")
  private Articles article;  // Articles 객체와 연결

  @Column(name = "article_id", insertable = false, updatable = false)
  private Long articleId;  // 외래 키 필드로 articleId 추가

  @Column
  private String titleWord;

  @Column
  private String descriptionWord;

  @Column
  private int year;

  @Column
  private int month;

  @Column
  private int day;

  @Column
  @CreatedDate
  private LocalDateTime createDate;

  @Builder
  public KeywordSummary(Articles article, String titleWord, String descriptionWord, int day, Long id, int month, String word, int year) {
    this.article = article;
    this.day = day;
    this.id = id;
    this.month = month;
    this.titleWord = titleWord;
    this.descriptionWord = descriptionWord;
    this.year = year;
  }
}
