package co.news.insight.repository;

import co.news.insight.model.Articles;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticlesRepository extends JpaRepository<Articles, Long> {

  List<Articles> findAllByCreateDateGreaterThanEqual(LocalDateTime today);

}
