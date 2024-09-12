package co.news.insight.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

@Component
public class NewsProcessingHelper {

  private static final double SIMILARITY_THRESHOLD = 0.90;

  public boolean isTitleSimilar(String title, List<String> savedTitles) {
    for (String savedTitle : savedTitles) {
      if (calculateSimilarity(title, savedTitle) >= SIMILARITY_THRESHOLD) {
        return true;
      }
    }
    return false;
  }

  public double calculateSimilarity(String title1, String title2) {
    int distance = LevenshteinDistance.getDefaultInstance().apply(title1, title2);
    int maxLength = Math.max(title1.length(), title2.length());
    return 1.0 - (double) distance / maxLength;
  }

  public LocalDateTime parseToLocalDateTime(String dateStr) {
    String cleanDateStr = dateStr.replaceFirst(" [+-]\\d{4}", "");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
    return LocalDateTime.parse(cleanDateStr, formatter);
  }
}
