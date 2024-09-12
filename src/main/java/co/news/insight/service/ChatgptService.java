package co.news.insight.service;

import co.news.insight.model.Articles;
import co.news.insight.model.KeywordSummary;
import co.news.insight.repository.ArticlesRepository;
import co.news.insight.repository.KeywordSummaryRepository;
import co.news.insight.utils.KeywordExtractor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatgptService {

  private final ArticlesRepository articlesRepository;
  private final KeywordSummaryRepository keywordSummaryRepository;
  private final GPTResponseService gptResponseService;

  /**
   * 타이틀과 설명에서 중요 키워드를 추출하고, GPT 분석을 통해 응답 저장
   */
  @Transactional
  public void analyzeAndSaveKeywords() {
    List<Articles> articles = articlesRepository.findAll();
    articles.forEach(this::processArticleKeywords);
  }

  // 키워드 요약 저장 후 GPT 요청
  private void processArticleKeywords(Articles article) {
    String titleWord = KeywordExtractor.extract(article.getTitle());
    String descriptionWord = KeywordExtractor.extract(article.getDescription());

    int year = article.getPubDate().getYear();
    int month = article.getPubDate().getMonth().getValue();
    int day = article.getPubDate().getDayOfMonth();

    KeywordSummary summary = keywordSummaryRepository.save(KeywordSummary.builder()
        .article(article)
        .year(year)
        .month(month)
        .day(day)
        .titleWord(titleWord)
        .descriptionWord(descriptionWord)
        .build());

    gptResponseService.analyzeAndSaveGptResponse(summary, year, month, day);
  }
}
