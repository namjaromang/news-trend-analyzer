package co.news.insight.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "articles")
@RequiredArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Articles {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String title;

  @Column
  private String link;

  @Column
  private String description;

  @Column
  private LocalDateTime pubDate;

  @Column
  @CreatedDate
  private LocalDateTime createDate;

  @Builder
  public Articles(String description, Long id, String link, LocalDateTime pubDate, String title) {
    this.description = description;
    this.id = id;
    this.link = link;
    this.pubDate = pubDate;
    this.title = title;
  }


}
