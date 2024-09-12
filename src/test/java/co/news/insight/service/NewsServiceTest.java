package co.news.insight.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.news.insight.model.Articles;
import co.news.insight.repository.ArticlesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

@SpringBootTest
class NewsServiceTest {

  @Mock
  private WebClient webClient;

  @Mock
  private ArticlesRepository articlesRepository;

  @Mock
  private NewsProcessingHelper newsProcessingHelper;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private NewsService newsService;

  @Value("${news.api.client}")
  private String CLIENT_ID;

  @Value("${news.api.key}")
  private String SECRET_KEY;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void fetchAndProcessNews_ShouldSaveArticlesWhenNoSimilarTitleExists() throws Exception {
    // Arrange
    WebClient.RequestHeadersUriSpec request = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    ResponseSpec responseSpec = mock(ResponseSpec.class);

    String apiResponse = "{ \"items\": [{\"title\": \"Test Title\", \"originallink\": \"http://testlink.com\", \"description\": \"Test Description\", \"pubDate\": \"Mon, 12 Sep 2024 10:15:30 +0900\"}]}";
    List<String> savedTitles = new ArrayList<>();

    when(webClient.get()).thenReturn(request);
    when(request.uri(any(Function.class))).thenReturn(headersSpec);
    when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(apiResponse));

    JsonNode jsonNode = mock(JsonNode.class);
    when(objectMapper.readTree(apiResponse)).thenReturn(jsonNode);
    when(jsonNode.path("items")).thenReturn(jsonNode);
    when(jsonNode.findValues("item")).thenReturn(List.of(jsonNode));
    when(jsonNode.path("title")).thenReturn(jsonNode);
    when(jsonNode.asText()).thenReturn("Test Title");
    when(jsonNode.path("originallink")).thenReturn(jsonNode);
    when(jsonNode.path("description")).thenReturn(jsonNode);
    when(jsonNode.path("pubDate")).thenReturn(jsonNode);

    when(newsProcessingHelper.isTitleSimilar(anyString(), anyList())).thenReturn(false);
    when(newsProcessingHelper.parseToLocalDateTime(anyString())).thenReturn(LocalDateTime.of(2024, 9, 12, 10, 15, 30));

    // Act & Assert
    assertDoesNotThrow(() -> newsService.fetchAndProcessNews(1));

    // Verify that article is saved
    verify(articlesRepository, times(1)).save(any(Articles.class));
  }

  @Test
  void fetchAndProcessNews_ShouldNotSaveArticlesWhenSimilarTitleExists() throws Exception {
    // Arrange
    WebClient.RequestHeadersUriSpec request = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    ResponseSpec responseSpec = mock(ResponseSpec.class);

    String apiResponse = "{ \"items\": [{\"title\": \"Test Title\", \"originallink\": \"http://testlink.com\", \"description\": \"Test Description\", \"pubDate\": \"Mon, 12 Sep 2024 10:15:30 +0900\"}]}";
    List<String> savedTitles = new ArrayList<>();

    when(webClient.get()).thenReturn(request);
    when(request.uri(any(Function.class))).thenReturn(headersSpec);
    when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(apiResponse));

    JsonNode jsonNode = mock(JsonNode.class);
    when(objectMapper.readTree(apiResponse)).thenReturn(jsonNode);
    when(jsonNode.path("items")).thenReturn(jsonNode);
    when(jsonNode.findValues("item")).thenReturn(List.of(jsonNode));
    when(jsonNode.path("title")).thenReturn(jsonNode);
    when(jsonNode.asText()).thenReturn("Test Title");
    when(jsonNode.path("originallink")).thenReturn(jsonNode);
    when(jsonNode.path("description")).thenReturn(jsonNode);
    when(jsonNode.path("pubDate")).thenReturn(jsonNode);

    when(newsProcessingHelper.isTitleSimilar(anyString(), anyList())).thenReturn(true);

    assertDoesNotThrow(() -> newsService.fetchAndProcessNews(1));

    verify(articlesRepository, times(0)).save(any(Articles.class));
  }
}
