package co.news.insight.service;

import co.news.insight.model.Articles;
import co.news.insight.repository.ArticlesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  private final CategoryService categoryService;
  private static final Set<String> TLD_IGNORE = new HashSet<>(Arrays.asList(
      "www","com", "co", "kr", "net", "org", "gov", "edu", "mil", "biz", "info", "name", "pro", "int", "eu", "asia"
  ));


  private final NewsProcessingHelper newsProcessingHelper;
  @Value("${news.api.client}")
  private String CLIENT_ID;
  @Value("${news.api.key}")
  private String SECRET_KEY;

  @Transactional
  public void fetchAndProcessNews(int start, String query) {
    fetchNewsAndSave(start, query, false);
  }

  @Transactional
  public void fetchAndProcessTodayNews(int start, String query) {
    fetchNewsAndSave(start, query, true);
  }

  // 공통 메서드: 뉴스 데이터를 가져오고 저장하는 로직
  private void fetchNewsAndSave(int start, String query, boolean onlyToday) {
    try {
      String response = callNewsApi(start, query);
      System.out.println(response);
      ArrayNode items = parseNewsItems(response);

      List<String> savedTitles = new ArrayList<>();
      LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();  // 오늘 날짜

      for (JsonNode item : items) {
        String title = item.path("title").asText();
        String originallink = item.path("originallink").asText();
        String naverLink = item.path("link").asText();
        String description = item.path("description").asText();
        String pubDate = item.path("pubDate").asText();
        String publisher = extractPublisher(originallink);

        LocalDateTime newsPubDate = newsProcessingHelper.parseToLocalDateTime(pubDate);

        // 오늘 뉴스만 저장하는 경우, 오늘 날짜와 비교하여 필터링
        if (!onlyToday || newsPubDate.toLocalDate().isEqual(today.toLocalDate())) {
          boolean isSimilar = newsProcessingHelper.isTitleSimilar(title, savedTitles);

          if (!isSimilar) {
            savedTitles.add(title);

            Long categoryId = categoryService.getCategoryIdFromUrl(naverLink);
            Articles articles = createArticlesEntity(categoryId, title, originallink, naverLink, description, newsPubDate, publisher);

            articlesRepository.save(articles);
            System.out.println("저장된 뉴스: " + title);
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String callNewsApi(int start, String query) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .scheme("https")
            .host("openapi.naver.com")
            .path("/v1/search/news.json")
            .queryParam("query", query)
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

  private Articles createArticlesEntity(Long categoryId, String title, String originalLink, String naverLink, String description, LocalDateTime pubDate,
      String publisher) {
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

  private String extractPublisher(String url) {
    try {
      // URI 클래스를 사용하여 URL에서 호스트(도메인) 추출
      URI uri = new URI(url);
      String host = uri.getHost();

      // 호스트가 null이 아닌지 확인
      if (host != null) {
        // 도메인을 점(.)으로 나누어 배열로 저장
        String[] domainParts = host.split("\\.");

        // 도메인 중에서 TLD(최상위 도메인)를 제외한 주 도메인을 추출
        String domain = null;
        for (int i = 0; i < domainParts.length - 1; i++) {
          if (!TLD_IGNORE.contains(domainParts[i])) {
            domain = domainParts[i];
            break;
          }
        }

        // 결과 출력
        System.out.println("URL: " + url);
        System.out.println("도메인 추출: " + domain);
        System.out.println();
        return domain;
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }
}
