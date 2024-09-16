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
  private String publisher;

  @Column
  private String originalLink;

  @Column
  private String naverLink;

  @Column
  private String description;

  @Column
  private Long categoryId;

  @Column
  private LocalDateTime pubDate;

  @Column
  @CreatedDate
  private LocalDateTime createDate;

  @Builder
  public Articles(Long categoryId, String publisher, String description, Long id, String originalLink, String naverLink, LocalDateTime pubDate, String title) {
    this.description = description;
    this.categoryId = categoryId;
    this.publisher = publisher;
    this.id = id;
    this.originalLink = originalLink;
    this.naverLink = naverLink;
    this.pubDate = pubDate;
    this.title = title;
  }


}
