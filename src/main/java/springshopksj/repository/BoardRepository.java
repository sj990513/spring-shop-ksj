package springshopksj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

}
