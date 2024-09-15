package co.news.insight.service;

import co.news.insight.model.Articles;
import co.news.insight.model.Category;
import co.news.insight.repository.ArticlesRepository;
import co.news.insight.repository.CategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final CategoryRepository categoryRepository;
  private Map<Integer, Long> categoryCache = new HashMap<>();  // naverCode -> Category ID

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
      ArrayNode items = parseNewsItems(response);

      List<String> savedTitles = new ArrayList<>();

      for (JsonNode item : items) {
        String title = item.path("title").asText();
        String originallink = item.path("originallink").asText();
        String naverLink = item.path("link").asText();
        String description = item.path("description").asText();
        String pubDate = item.path("pubDate").asText();

        boolean isSimilar = newsProcessingHelper.isTitleSimilar(title, savedTitles);

        if (!isSimilar) {
          savedTitles.add(title);

          LocalDateTime localDateTime = newsProcessingHelper.parseToLocalDateTime(pubDate);

          Long categoryId = getCategoryIdFromUrl(naverLink);
          Articles articles = createArticlesEntity(categoryId, title, originallink, naverLink, description, localDateTime);

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

  private ArrayNode parseNewsItems(String response) throws Exception {
    JsonNode rootNode = objectMapper.readTree(response);
    if (rootNode.path("items").isArray()) {
      return (ArrayNode) rootNode.path("items");
    } else {
      return objectMapper.createArrayNode(); // Returning an empty array if "items" is not an array
    }
  }

  private Articles createArticlesEntity(Long categoryId, String title, String originalLink, String naverLink, String description, LocalDateTime pubDate) {
    return Articles.builder()
        .title(title)
        .categoryId(categoryId)
        .originalLink(originalLink)
        .naverLink(naverLink)
        .pubDate(pubDate)
        .description(description)
        .build();
  }


  @PostConstruct
  public void loadCategoryCache() {
    List<Category> categories = categoryRepository.findAll();  // 모든 카테고리 로드
    for (Category category : categories) {
      categoryCache.put(category.getNaverCode(), category.getId());  // naverCode -> Category ID
    }
    System.out.println("카테고리 캐시가 초기화되었습니다.");
  }


  // naverCode로부터 카테고리 ID 반환, 없으면 99 반환
  public Long getCategoryByNaverCode(int naverCode) {
    return categoryCache.getOrDefault(naverCode, 99L);  // 99L을 기본값으로 설정
  }

  // URL에서 sid1 값을 추출하여 naverCode로 변환 후, 해당 카테고리 ID 반환
  public Long getCategoryIdFromUrl(String url) {
    String sid = null;
    int sid1Index = url.indexOf("sid=");

    if (sid1Index != -1) {
      int endIndex = url.indexOf('&', sid1Index); // Find the next parameter (or end of the string)
      if (endIndex == -1) {
        sid = url.substring(sid1Index + 4);
      } else {
        sid = url.substring(sid1Index + 4, endIndex);
      }
    }

    if (sid == null || sid.isEmpty()) {
      return 99L;
    }

    try {
      int naverCode = Integer.parseInt(sid);
      return getCategoryByNaverCode(naverCode);
    } catch (NumberFormatException e) {
      return 99L;
    }
  }
}
