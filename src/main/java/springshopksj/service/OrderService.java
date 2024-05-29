package springshopksj.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springshopksj.dto.DeliveryDto;
import springshopksj.dto.OrderItemDto;
import springshopksj.dto.PaymentDto;
import springshopksj.entity.*;
import springshopksj.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Order createOrder(long memberId, List<OrderItemDto> orderItems, DeliveryDto deliveryDto, PaymentDto paymentDto) {
        Member member = memberRepository.findById(memberId);

        // 2. 주문 데이터 생성
        Order order = new Order();
        order.setMember(member);
        order.setStatus(Order.OrderStatus.ORDERED);
        order.setOrderdate(LocalDateTime.now());

        orderRepository.save(order);


        // 3. 주문 항목 생성 및 재고 업데이트
        for (OrderItemDto orderItemDto : orderItems) {
            Item item = itemRepository.findById(orderItemDto.getItemId());

            if (item.getStock() < orderItemDto.getCount()) {
                throw new RuntimeException("해당 상품의 재고가 남아있지 않습니다.: " + item.getItemname());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setOrderprice(item.getPrice());
            orderItem.setCount(orderItemDto.getCount());

            orderItemRepository.save(orderItem);

            // 재고 감소
            item.setStock(item.getStock() - orderItemDto.getCount());
            itemRepository.save(item);
        }

        // 4. 배송 정보 생성
        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setStatus(Delivery.DeliveryStatus.READY);
        delivery.setAddress(deliveryDto.getAddress());

        deliveryRepository.save(delivery);

        // 5. 결제 정보 생성
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentmethod(paymentDto.getPaymentMethod());
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentdate(LocalDateTime.now());

        paymentRepository.save(payment);

        return order;
    }
}
