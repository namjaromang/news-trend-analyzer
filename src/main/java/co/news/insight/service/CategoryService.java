package co.news.insight.service;

import co.news.insight.model.Category;
import co.news.insight.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private Map<Integer, Long> categoryCache = new HashMap<>();

  private final CategoryRepository categoryRepository;

  @PostConstruct
  public void loadCategoryCache() {
    List<Category> categories = categoryRepository.findAll();
    for (Category category : categories) {
      categoryCache.put(category.getNaverCode(), category.getId());
    }
    System.out.println("카테고리 캐시가 초기화되었습니다.");
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public Long getCategoryByNaverCode(int naverCode) {
    return categoryCache.getOrDefault(naverCode, 99L);
  }

  public Long getCategoryIdFromUrl(String url) {
    String sid = null;

    // sid 또는 sid1 값을 찾기 위한 로직
    int sid1Index = url.indexOf("sid1=");
    if (sid1Index == -1) {
      sid1Index = url.indexOf("sid=");  // sid1이 없을 경우 sid를 찾음
    }

    // sid 또는 sid1이 있는 경우 해당 값을 추출
    if (sid1Index != -1) {
      int endIndex = url.indexOf('&', sid1Index);
      if (endIndex == -1) {
        sid = url.substring(sid1Index + 4);  // sid1= 또는 sid= 뒤의 값을 추출
      } else {
        sid = url.substring(sid1Index + 4, endIndex);
      }
    }

    // sid가 없거나 비어있으면 기본 카테고리 ID 반환
    if (sid == null || sid.isEmpty()) {
      return 99L;  // 기본 카테고리 ID
    }

    // sid 값을 정수로 변환하여 카테고리 ID 반환
    try {
      int naverCode = Integer.parseInt(sid);
      return getCategoryByNaverCode(naverCode);
    } catch (NumberFormatException e) {
      return 99L;  // 변환 실패 시 기본 카테고리 ID 반환
    }
  }

}
