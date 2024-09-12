package co.news.insight.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewsProcessingHelperTest {

  private NewsProcessingHelper newsProcessingHelper;

  @BeforeEach
  void setUp() {
    newsProcessingHelper = new NewsProcessingHelper();
  }

  @Test
  void isTitleSimilar_ShouldReturnTrueForSimilarTitles() {
    // Given
    String title = "Breaking News on AI";
    List<String> savedTitles = Arrays.asList("Breaking News on Artificial Intelligence", "AI Breakthroughs in 2024");

    // When
    boolean result = newsProcessingHelper.isTitleSimilar(title, savedTitles);

    // Then
    assertTrue(result, "The titles should be considered similar.");
  }

  @Test
  void isTitleSimilar_ShouldReturnFalseForDissimilarTitles() {
    // Given
    String title = "Breaking News on AI";
    List<String> savedTitles = Arrays.asList("New Discoveries in Space", "Health Benefits of Yoga");

    // When
    boolean result = newsProcessingHelper.isTitleSimilar(title, savedTitles);

    // Then
    assertFalse(result, "The titles should not be considered similar.");
  }

  @Test
  void calculateSimilarity_ShouldReturnHighSimilarityForSimilarTitles() {
    // Given
    String title1 = "Artificial Intelligence in 2024";
    String title2 = "AI in 2024";

    // When
    double similarity = newsProcessingHelper.calculateSimilarity(title1, title2);

    // Then
    assertTrue(similarity >= 0.90, "The similarity should be higher than or equal to 90%.");
  }

  @Test
  void calculateSimilarity_ShouldReturnLowSimilarityForDissimilarTitles() {
    // Given
    String title1 = "Artificial Intelligence in 2024";
    String title2 = "Health Benefits of Yoga";

    // When
    double similarity = newsProcessingHelper.calculateSimilarity(title1, title2);

    // Then
    assertTrue(similarity < 0.90, "The similarity should be lower than 90%.");
  }

  @Test
  void parseToLocalDateTime_ShouldParseValidDate() {
    // Given
    String dateStr = "Mon, 12 Sep 2024 10:15:30 +0900";

    // When
    LocalDateTime result = newsProcessingHelper.parseToLocalDateTime(dateStr);

    // Then
    assertEquals(LocalDateTime.of(2024, 9, 12, 10, 15, 30), result, "The parsed date and time should match the input.");
  }

  @Test
  void parseToLocalDateTime_ShouldThrowExceptionForInvalidDateFormat() {
    // Given
    String invalidDateStr = "Invalid Date Format";

    // When & Then
    assertThrows(Exception.class, () -> newsProcessingHelper.parseToLocalDateTime(invalidDateStr),
        "An exception should be thrown for an invalid date format.");
  }
}
