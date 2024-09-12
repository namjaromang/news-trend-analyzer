package co.news.insight.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.news.insight.model.Articles;
import co.news.insight.model.KeywordSummary;
import co.news.insight.repository.ArticlesRepository;
import co.news.insight.repository.KeywordSummaryRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ChatgptServiceTest {

  @Mock
  private ArticlesRepository articlesRepository;

  @Mock
  private KeywordSummaryRepository keywordSummaryRepository;

  @Mock
  private GPTResponseService gptResponseService;

  @InjectMocks
  private ChatgptService chatgptService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void analyzeAndSaveKeywords_shouldExtractAndSaveKeywordsForEachArticle() {
    // Given
    Articles article1 = Articles.builder()
        .title("First article title")
        .description("First article description")
        .pubDate(LocalDateTime.of(2024, 9, 12, 0, 0))
        .build();

    Articles article2 = Articles.builder()
        .title("Second article title")
        .description("Second article description")
        .pubDate(LocalDateTime.of(2024, 9, 13, 0, 0))
        .build();

    List<Articles> articlesList = Arrays.asList(article1, article2);

    when(articlesRepository.findAll()).thenReturn(articlesList);

    // When
    chatgptService.analyzeAndSaveKeywords();

    // Then
    // KeywordSummary 저장 확인
    ArgumentCaptor<KeywordSummary> keywordSummaryCaptor = ArgumentCaptor.forClass(KeywordSummary.class);
    verify(keywordSummaryRepository, times(2)).save(keywordSummaryCaptor.capture());

    List<KeywordSummary> capturedSummaries = keywordSummaryCaptor.getAllValues();

    // 첫 번째 기사 키워드 저장 확인
    KeywordSummary summary1 = capturedSummaries.get(0);
    assertEquals("First article title", summary1.getTitleWord());
    assertEquals("First article description", summary1.getDescriptionWord());
    assertEquals(2024, summary1.getYear());
    assertEquals(9, summary1.getMonth());
    assertEquals(12, summary1.getDay());

    // 두 번째 기사 키워드 저장 확인
    KeywordSummary summary2 = capturedSummaries.get(1);
    assertEquals("Second article title", summary2.getTitleWord());
    assertEquals("Second article description", summary2.getDescriptionWord());
    assertEquals(2024, summary2.getYear());
    assertEquals(9, summary2.getMonth());
    assertEquals(13, summary2.getDay());

    // GPTResponseService 호출 확인
    verify(gptResponseService, times(2)).analyzeAndSaveGptResponse(any(KeywordSummary.class), anyInt(), anyInt(), anyInt());
  }

  @Test
  void processArticleKeywords_shouldCallKeywordExtractorAndSaveSummary() {
    // Given
    Articles article = Articles.builder()
        .title("Sample title")
        .description("Sample description")
        .pubDate(LocalDateTime.of(2024, 9, 12, 0, 0))
        .build();

    when(keywordSummaryRepository.save(any(KeywordSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    chatgptService.analyzeAndSaveKeywords();

    // Then
    // KeywordExtractor 호출 확인
    verify(keywordSummaryRepository, times(1)).save(any(KeywordSummary.class));

    // GPTResponseService 호출 확인
    verify(gptResponseService, times(1)).analyzeAndSaveGptResponse(any(KeywordSummary.class), eq(2024), eq(9), eq(12));
  }
}
