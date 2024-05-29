package springshopksj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Delivery;
import springshopksj.entity.Order;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
