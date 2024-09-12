package co.news.insight.service;

import co.news.insight.model.GPTResponse;
import co.news.insight.model.KeywordSummary;
import co.news.insight.repository.GPTResponseRepository;
import co.news.insight.request.Message;
import co.news.insight.request.ModelRequest;
import co.news.insight.response.GptApiResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GPTResponseService {

  private final GPTResponseRepository gptResponseRepository;
  private final WebClient webClient;

  @Value("${openai.api.url}")
  private String apiUrl;

  @Value("${openai.api.token}")
  private String apiToken;

  // GPT 응답 처리 및 저장
  public void analyzeAndSaveGptResponse(KeywordSummary summary, int year, int month, int day) {
    String keywords = formatKeywords(summary);
    String word = buildQueryTerm(year, month, day);

    GptApiResponse gptApiResponse = analyzeTrends(keywords, word);
    if (gptApiResponse != null) {
      saveGptResponse(gptApiResponse, word);
    }
  }

  // GPT API 요청 생성 및 호출
  protected GptApiResponse analyzeTrends(String keywords, String word) {
    if (keywords.isEmpty()) {
      return null;
    }

    ModelRequest request = ModelRequest.builder()
        .model("gpt-4o")
        .messages(buildMessages(keywords, word))
        .build();

    return webClient.post()
        .uri(apiUrl)
        .header("Authorization", apiToken)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(GptApiResponse.class)
        .block();
  }

  // GPT 응답 저장
  private void saveGptResponse(GptApiResponse response, String word) {
    GPTResponse gptResponse = GPTResponse.builder()
        .word(word)
        .gptId(response.getId())
        .model(response.getModel())
        .content(response.getChoices().get(0).getMessage().getContent())
        .createdDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(response.getCreated()), ZoneId.systemDefault()))
        .promptTokens(response.getUsage().getPromptTokens())
        .completionTokens(response.getUsage().getCompletionTokens())
        .totalTokens(response.getUsage().getTotalTokens())
        .build();

    gptResponseRepository.save(gptResponse);
  }

  // 키워드 포맷
  private String formatKeywords(KeywordSummary summary) {
    return String.format("title: %s, description: %s",
        summary.getTitleWord(), summary.getDescriptionWord());
  }

  // GPT API에 보낼 메시지 생성
  private List<Message> buildMessages(String keywords, String word) {
    return Arrays.asList(
        Message.builder()
            .role("system")
            .content(String.format("%s 에 맞는 타이틀과 description이야 경향성을 파악해서 한국어로 설명해줘", word))
            .build(),
        Message.builder()
            .role("user")
            .content(keywords)
            .build()
    );
  }

  // 년, 월, 일에 맞춘 검색어 생성
  private String buildQueryTerm(int year, int month, int day) {
    return year + "년도 " + month + "월 " + day + "일";
  }
}
