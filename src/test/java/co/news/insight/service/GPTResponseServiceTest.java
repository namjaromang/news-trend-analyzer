package co.news.insight.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.news.insight.model.GPTResponse;
import co.news.insight.model.KeywordSummary;
import co.news.insight.repository.GPTResponseRepository;
import co.news.insight.request.ModelRequest;
import co.news.insight.response.GptApiResponse;
import co.news.insight.response.GptApiResponse.Choice;
import co.news.insight.response.GptApiResponse.Message;
import co.news.insight.response.GptApiResponse.Usage;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

class GPTResponseServiceTest {

  @Mock
  private GPTResponseRepository gptResponseRepository;

  @Mock
  private WebClient webClient;

  @Mock
  private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private WebClient.RequestBodySpec requestBodySpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  @InjectMocks
  private GPTResponseService gptResponseService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void analyzeAndSaveGptResponse_shouldCallGptApiAndSaveResponse() {
    // Given
    KeywordSummary summary = KeywordSummary.builder()
        .titleWord("AI in 2024")
        .descriptionWord("Artificial intelligence is evolving")
        .build();
    int year = 2024;
    int month = 9;
    int day = 12;

    GptApiResponse mockApiResponse = createMockGptApiResponse();

    // Mock WebClient chain
    when(webClient.post()).thenReturn((RequestBodyUriSpec) requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any(ModelRequest.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(GptApiResponse.class)).thenReturn(Mono.just(mockApiResponse));

    // When
    gptResponseService.analyzeAndSaveGptResponse(summary, year, month, day);

    // Then
    // GPTResponse 저장 확인
    ArgumentCaptor<GPTResponse> responseCaptor = ArgumentCaptor.forClass(GPTResponse.class);
    verify(gptResponseRepository).save(responseCaptor.capture());

    GPTResponse savedResponse = responseCaptor.getValue();
    assertNotNull(savedResponse);
    assertEquals("2024년도 9월 12일", savedResponse.getWord());
    assertEquals("gpt-4o", savedResponse.getModel());
    assertEquals("GPT response content", savedResponse.getContent());
    assertEquals(123, savedResponse.getPromptTokens());
    assertEquals(456, savedResponse.getCompletionTokens());
    assertEquals(579, savedResponse.getTotalTokens());
  }

  @Test
  void analyzeAndSaveGptResponse_shouldHandleEmptyKeywords() {
    // Given
    KeywordSummary summary = KeywordSummary.builder()
        .titleWord("")
        .descriptionWord("")
        .build();
    int year = 2024;
    int month = 9;
    int day = 12;

    // When
    gptResponseService.analyzeAndSaveGptResponse(summary, year, month, day);

    // Then
    // GPT API 호출 없이 저장이 이뤄지지 않는지 확인
    verify(gptResponseRepository, never()).save(any());
  }

  @Test
  void analyzeTrends_shouldReturnNullForEmptyKeywords() {
    // When
    GptApiResponse result = gptResponseService.analyzeTrends("", "test");

    // Then
    assertNull(result);
    verifyNoInteractions(webClient);
  }

  @Test
  void analyzeAndSaveGptResponse_shouldHandleWebClientError() {
    // Given
    KeywordSummary summary = KeywordSummary.builder()
        .titleWord("AI in 2024")
        .descriptionWord("Artificial intelligence is evolving")
        .build();
    int year = 2024;
    int month = 9;
    int day = 12;

    // Mock WebClient chain to throw error
    when(webClient.post()).thenReturn((RequestBodyUriSpec) requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any(ModelRequest.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(GptApiResponse.class))
        .thenThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null));

    // When
    gptResponseService.analyzeAndSaveGptResponse(summary, year, month, day);

    // Then
    verify(gptResponseRepository, never()).save(any());
  }

  private GptApiResponse createMockGptApiResponse() {
    Message message = new Message();
    message.setContent("GPT response content");

    Choice choice = new Choice();
    choice.setMessage(message);

    Usage usage = new Usage();
    usage.setPromptTokens(123);
    usage.setCompletionTokens(456);
    usage.setTotalTokens(579);

    GptApiResponse response = new GptApiResponse();
    response.setId("gpt-response-id");
    response.setModel("gpt-4o");
    response.setCreated((int) Instant.now().getEpochSecond());
    response.setChoices(Collections.singletonList(choice));
    response.setUsage(usage);

    return response;
  }
}
