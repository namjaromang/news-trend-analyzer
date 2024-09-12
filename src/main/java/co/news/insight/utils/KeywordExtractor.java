package co.news.insight.utils;

import com.hankcs.hanlp.HanLP;
import java.util.List;
import java.util.stream.Collectors;

public class KeywordExtractor {

  public static String extract(String text) {
    List<String> keywords = HanLP.extractKeyword(text, 3);
    return keywords.stream()
        .map(keyword -> keyword.replaceAll("<[^>]*>", ""))
        .map(keyword -> keyword.replaceAll("[^\\p{L}\\p{N}\\s]", ""))
        .collect(Collectors.joining(","));
  }
}

