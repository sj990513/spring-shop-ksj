package springshopksj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
}
