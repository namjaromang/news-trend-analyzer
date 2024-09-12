package co.news.insight.service;

import co.news.insight.model.Articles;
import co.news.insight.repository.ArticlesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class NewsService {

  private static final int DISPLAY_COUNT = 100;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient webClient;
  private final ArticlesRepository articlesRepository;
  private final NewsProcessingHelper newsProcessingHelper;
  @Value("${news.api.client}")
  private String CLIENT_ID;
  @Value("${news.api.key}")
  private String SECRET_KEY;

  // 트랜잭션 적용
  @Transactional
  public void fetchAndProcessNews(int start) {
    try {
      String response = callNewsApi(start);
      List<JsonNode> items = parseNewsItems(response);

      List<String> savedTitles = new ArrayList<>();

      for (JsonNode item : items) {
        String title = item.path("title").asText();
        String originallink = item.path("originallink").asText();
        String description = item.path("description").asText();
        String pubDate = item.path("pubDate").asText();

        boolean isSimilar = newsProcessingHelper.isTitleSimilar(title, savedTitles);

        if (!isSimilar) {
          savedTitles.add(title);

          LocalDateTime localDateTime = newsProcessingHelper.parseToLocalDateTime(pubDate);
          Articles articles = createArticlesEntity(title, originallink, description, localDateTime);

          articlesRepository.save(articles);
          System.out.println("저장된 뉴스: " + title);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String callNewsApi(int start) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .scheme("https")
            .host("openapi.naver.com")
            .path("/v1/search/news.json")
            .queryParam("query", "query")
            .queryParam("display", DISPLAY_COUNT)
            .queryParam("start", start)
            .queryParam("sort", "date")
            .build())
        .header("X-Naver-Client-Id", CLIENT_ID)
        .header("X-Naver-Client-Secret", SECRET_KEY)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  private List<JsonNode> parseNewsItems(String response) throws Exception {
    JsonNode rootNode = objectMapper.readTree(response);
    return rootNode.path("items").findValues("item");
  }

  private Articles createArticlesEntity(String title, String originallink, String description, LocalDateTime pubDate) {
    return Articles.builder()
        .title(title)
        .link(originallink)
        .pubDate(pubDate)
        .description(description)
        .build();
  }
}
