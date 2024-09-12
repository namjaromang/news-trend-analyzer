package co.news.insight.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gpt_response")
@Getter
@NoArgsConstructor
public class GPTResponse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String word; //요청값

  @Column
  private String gptId;  // GPT 응답 ID

  @Column
  private String model;  // 사용된 GPT 모델

  @Column(columnDefinition = "TEXT")
  private String content;  // GPT 응답 내용

  @Column
  private LocalDateTime createdDate;  // 생성 시간

  @Column
  private int promptTokens;  // 프롬프트에 사용된 토큰 수

  @Column
  private int completionTokens;  // 응답에 사용된 토큰 수

  @Column
  private int totalTokens;  // 총 토큰 수

  @Builder
  public GPTResponse(String word, String gptId, String model, String content, LocalDateTime createdDate, int promptTokens, int completionTokens,
      int totalTokens) {
    this.word = word;
    this.gptId = gptId;
    this.model = model;
    this.content = content;
    this.createdDate = createdDate;
    this.promptTokens = promptTokens;
    this.completionTokens = completionTokens;
    this.totalTokens = totalTokens;
  }
}
