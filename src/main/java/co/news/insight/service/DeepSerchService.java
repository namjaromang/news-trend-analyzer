package co.news.insight.service;

import co.news.insight.model.Articles;
import co.news.insight.repository.ArticlesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class DeepSerchService {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient webClient;
  private final ArticlesRepository articlesRepository;
  private final CategoryService categoryService;

  private final NewsProcessingHelper newsProcessingHelper;

  @Value("${deep.api.key}")
  private String API_KEY;

  @Transactional
  public void fetchAndProcessNews(LocalDate start, LocalDate end, String query) {
    fetchNewsAndSave(start, end, query, false);
  }

  @Transactional
  public void fetchAndProcessTodayNews(LocalDate start, LocalDate end, String query) {
    fetchNewsAndSave(start, end, query, true);
  }

  // 공통 메서드: 뉴스 데이터를 가져오고 저장하는 로직
  private void fetchNewsAndSave(LocalDate start, LocalDate end, String query, boolean onlyToday) {
    try {
      String response = callNewsApi(start, end, query);
      System.out.println(response);

      JsonNode rootNode = objectMapper.readTree(response);  // 전체 응답을 파싱
      ArrayNode items = (ArrayNode) rootNode.path("data");  // "data" 필드 안에 있는 뉴스 아이템들

      List<String> savedTitles = new ArrayList<>();
      LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();  // 오늘 날짜

      for (JsonNode item : items) {
        // 각 아이템에서 필요한 필드들을 추출
        String title = item.path("title").asText();
        String contentUrl = item.path("content_url").asText();  // content_url 필드로 대체
        String publisher = item.path("publisher").asText();
        String summary = item.path("summary").asText();
        String pubDate = item.path("published_at").asText();  // published_at 필드로 대체

        LocalDateTime newsPubDate = newsProcessingHelper.deepParseToLocalDateTime(pubDate);  // 발행일 파싱

        // 오늘 뉴스만 저장하는 경우, 오늘 날짜와 비교하여 필터링
        if (!onlyToday || newsPubDate.toLocalDate().isEqual(today.toLocalDate())) {
          boolean isSimilar = newsProcessingHelper.isTitleSimilar(title, savedTitles);

          if (!isSimilar) {
            savedTitles.add(title);

            // 적절한 카테고리 ID 설정 (기존과 동일한 방식으로 처리)
            Long categoryId = categoryService.getCategoryIdFromUrl(contentUrl);

            // Articles 엔티티 생성
            Articles articles = createArticlesEntity(publisher, categoryId, title, contentUrl, contentUrl, summary, newsPubDate);

            // 데이터베이스에 저장
            articlesRepository.save(articles);
            System.out.println("저장된 뉴스: " + title);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @param start
   * @param end
   * @param query
   * @return
   */
  private String callNewsApi(LocalDate start, LocalDate end, String query) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .scheme("https")
            .host("api-v2.deepsearch.com")
            .path("/v1/articles")
            .queryParam("symbols", "KRX:005380")
            .queryParam("keyword", query)
            .queryParam("page_size", 100)
            .queryParam("date_from", start)
            .queryParam("date_to", end)
            .queryParam("api_key", API_KEY)
            .build())
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }


  private Articles createArticlesEntity(String publisher, Long categoryId, String title, String originalLink, String naverLink, String description,
      LocalDateTime pubDate) {
    return Articles.builder()
        .title(title)
        .publisher(publisher)
        .categoryId(categoryId)
        .originalLink(originalLink)
        .naverLink(naverLink)
        .pubDate(pubDate)
        .description(description)
        .build();
  }
}
