package co.news.insight.repository;

import co.news.insight.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByNaverCode(int naverCode);

}
