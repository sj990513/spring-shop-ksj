package springshopksj.dto;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import springshopksj.entity.Member;
import springshopksj.entity.Order;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusUpdateRequest {

    private Order.OrderStatus status;
}
