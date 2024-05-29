package springshopksj.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Basket;
import springshopksj.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
