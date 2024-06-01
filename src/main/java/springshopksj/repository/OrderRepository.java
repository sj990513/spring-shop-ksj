package springshopksj.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Basket;
import springshopksj.entity.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMemberID(long userID);

    Optional<Order> findByMemberIDAndStatus(long userID, Order.OrderStatus status);
}
