package springshopksj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
   List<Item> findByCategory(String category);

}
