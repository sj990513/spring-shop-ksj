package springshopksj.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import springshopksj.dto.*;
import springshopksj.entity.*;
import springshopksj.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    // OrderItemDto맵핑 메소드
    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDto orderItemDto = modelMapper.map(orderItem, OrderItemDto.class);
        orderItemDto.setOrderID(orderItem.getOrder().getID());
        orderItemDto.setItemID(orderItem.getItem().getID());
        return orderItemDto;
    }

    // OrderDto맵핑 메소드
    private OrderDto convertToOrderDto(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setUserID(order.getMember().getID());
        return orderDto;
    }

    private DeliveryDto convertToDeliveryDto(Delivery delivery) {
        DeliveryDto deliveryDto = modelMapper.map(delivery, DeliveryDto.class);
        deliveryDto.setOrderID(delivery.getOrder().getID());
        return deliveryDto;
    }

    private PaymentDto convertToPaymentDto(Payment payment) {
        PaymentDto paymentDto = modelMapper.map(payment, PaymentDto.class);
        paymentDto.setOrderID(payment.getOrder().getID());
        return paymentDto;
    }


    // 장바구니 조회
    public List<OrderItemDto> findAllCart(MemberDto memberDto) {
        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        // 기존 장바구니(PENDING 상태)를 찾음
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            // 기존의 장바구니가 없을 시 장바구니 생성 (PENDING)
            order = Order.builder()
                    .member(member)
                    .status(Order.OrderStatus.PENDING)
                    .orderdate(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }


        List<OrderItem> cart = orderItemRepository.findByOrderID(order.getID());

        List<OrderItemDto> cartDto = cart.stream().map(this::convertToOrderItemDto).collect(Collectors.toList());

        return cartDto;
    }

    // 장바구니 품목 추가(생성)
    public List<OrderItemDto> addCart(MemberDto memberDto, List<OrderItemDto> orderItems) {

        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        // 기존 장바구니(PENDING 상태)를 찾음
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            // 기존의 장바구니가 없을 시 장바구니 생성 (PENDING)
            order = Order.builder()
                    .member(member)
                    .status(Order.OrderStatus.PENDING)
                    .orderdate(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }

        // 장바구니에 아이템 추가
        for (OrderItemDto orderItemDto : orderItems) {
            Item item = itemRepository.findById(orderItemDto.getItemID())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

            if (item.getStock() < orderItemDto.getCount()) {
                throw new RuntimeException("해당 상품의 재고가 남아있지 않습니다.: " + item.getItemname());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .orderprice(item.getPrice())
                    .count(orderItemDto.getCount())
                    .build();

            orderItemRepository.save(orderItem);
        }

        List<OrderItem> cart = orderItemRepository.findByOrderID(order.getID());
        List<OrderItemDto> cartDto = cart.stream().map(this::convertToOrderItemDto).collect(Collectors.toList());

        return cartDto;
    }

    // 장바구니 품목 삭제
    public List<OrderItemDto> deleteCartItem(MemberDto memberDto, long order_itemID) {


        // 장바구니 탐색
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            throw new RuntimeException("삭제할 장바구니가 존재하지 않습니다.");
        }

        List<OrderItem> findByOrderId = orderItemRepository.findByOrderID(order.getID());

        // 본인의 장바구니안에 존재하는 품목만 삭제가능
        if (! findByOrderId.stream()
                .anyMatch( orderItem -> orderItem.getID() == order_itemID))
            throw new RuntimeException("해당 장바구니 상품을 삭제할 권한이 없습니다.");

        
        // 장바구니 상품 삭제
        OrderItem orderItem = orderItemRepository.findById(order_itemID)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        orderItemRepository.delete(orderItem);
        
        List<OrderItem> cart = orderItemRepository.findByOrderID(order.getID());
        List<OrderItemDto> cartDto = cart.stream().map(this::convertToOrderItemDto).collect(Collectors.toList());

        return cartDto;
    }

    // 장바구니 삭제
    public void deleteCart(MemberDto memberDto) {

        // 장바구니 탐색
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            throw new RuntimeException("삭제할 장바구니가 존재하지 않습니다.");
        }

        orderRepository.delete(order);
    }

    
    // 장바구니에서 결제
    public OrderDto createOrderFromCart(MemberDto memberDto, DeliveryDto deliveryDto, PaymentDto paymentDto) {
        
        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        // 장바구니 불러오기
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("장바구니가 존재하지 않습니다."));

        // 장바구니안에 존재하는 상품들
        List<OrderItem> orderItems = orderItemRepository.findByOrderID(order.getID());

        if (orderItems.isEmpty()) {
            throw new RuntimeException("장바구니에 상품이 존재하지 않습니다.");
        } else {
            for (OrderItem orderItem : orderItems) {
                
                Item item = itemRepository.findById(orderItem.getItem().getID())
                        .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

                if (item.getStock() < orderItem.getCount()) {
                    throw new RuntimeException("해당 상품의 재고가 남아있지 않습니다.: " + item.getItemname());
                }

                // 주문개수만큼 재고 삭제
                item.setStock(item.getStock() - orderItem.getCount());
                itemRepository.save(item);
            }
        }

        // 배송 정보 생성
        Delivery delivery = Delivery.builder()
                .order(order)
                .status(Delivery.DeliveryStatus.READY)
                .address(deliveryDto.getAddress())
                .build();
        deliveryRepository.save(delivery);

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .order(order)
                .paymentmethod(paymentDto.getPaymentMethod())
                .amount(paymentDto.getAmount())
                .paymentdate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // 주문완료로 상태 업데이트
        order.setStatus(Order.OrderStatus.ORDERED);
        orderRepository.save(order);

        OrderDto orderDto = convertToOrderDto(order);

        return orderDto;
    }



    // 아이템 디테일에서 개별주문(즉시주문) 생성
    public OrderDto createOrder(MemberDto memberDto, List<OrderItemDto> orderItems, DeliveryDto deliveryDto, PaymentDto paymentDto) {


        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        // 주문 데이터 생성
        Order order = Order.builder()
                .member(member)
                .status(Order.OrderStatus.ORDERED)
                .orderdate(LocalDateTime.now())
                .build();

        orderRepository.save(order);


        // 주문 항목 생성 및 재고 업데이트
        for (OrderItemDto orderItemDto : orderItems) {
            Item item = itemRepository.findById(orderItemDto.getItemID())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

            if (item.getStock() < orderItemDto.getCount()) {
                throw new RuntimeException("해당 상품의 재고가 남아있지 않습니다.: " + item.getItemname());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .orderprice(item.getPrice())
                    .count(orderItemDto.getCount())
                    .build();

            orderItemRepository.save(orderItem);

            item.setStock(item.getStock() - orderItemDto.getCount());
            itemRepository.save(item);
        }

        // 배송 정보 생성
        Delivery delivery = Delivery.builder()
                .order(order)
                .status(Delivery.DeliveryStatus.READY)
                .address(deliveryDto.getAddress())
                .build();

        deliveryRepository.save(delivery);

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .order(order)
                .paymentmethod(paymentDto.getPaymentMethod())
                .amount(paymentDto.getAmount())
                .paymentdate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        OrderDto orderDto = convertToOrderDto(order);

        return orderDto;
    }


    // 특정 주문 조회 (관리자페이지에서 사용)
    public OrderDto getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        OrderDto orderDto = convertToOrderDto(order);

        return  orderDto;
    }

    // userId로 order찾기
    public List<OrderDto> getOrdersByUserId(MemberDto memberDto) {

        List<Order> orders = orderRepository.findByMemberID(memberDto.getID());

        List<OrderDto> orderDtos = orders.stream().map(order -> {
                    OrderDto orderDto = modelMapper.map(order, OrderDto.class);
                    orderDto.setUserID(order.getMember().getID());
                    return orderDto;
                })
                .collect(Collectors.toList());


        return orderDtos;
    }

    // orderId로 모든 order 디테일 찾기 (order, orderItems, payment, delivery)
    public OrderRequest findOrderDetailByOrderId(MemberDto memberDto, long orderId) {

        String message = "주문에 해당하는 상세정보를 찾을수 없습니다.";

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(message));

        if (order.getMember().getID() != memberDto.getID())
            throw new RuntimeException("해당 주문조회의 권한이 없습니다.");

        List<OrderItem> orderItems = orderItemRepository.findByOrderID(orderId);


        Payment payment = paymentRepository.findByOrderID(orderId)
                .orElseThrow(() -> new RuntimeException(message));

        Delivery delivery = deliveryRepository.findByOrderID(orderId)
                .orElseThrow(() -> new RuntimeException(message));

        OrderRequest orderRequest = OrderRequest.builder()
                .orderDto(convertToOrderDto(order))
                .orderItems(orderItems.stream().map(this::convertToOrderItemDto).collect(Collectors.toList()))
                .paymentDto(convertToPaymentDto(payment))
                .deliveryDto(convertToDeliveryDto(delivery))
                .build();

        return orderRequest;
    }





    // order캔슬 (사용자가 요청)
    public void cancelOrder(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        if (order.getStatus() != Order.OrderStatus.ORDERED) {
            throw new RuntimeException("주문 취소가 불가능합니다.");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // (관리자가) 캔슬된 오더 허용후 삭제처리
    public void acceptCancelOrder(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        Delivery delivery = deliveryRepository.findByOrderID(orderId)
                .orElseThrow(() -> new RuntimeException("주문에 해당하는 배송정보를 찾을수 없습니다."));

        // 취소된 주문이랑, 배송전 상품만 취소허용 가능
        if (order.getStatus() != Order.OrderStatus.CANCELLED && delivery.getStatus() != Delivery.DeliveryStatus.READY) {
            throw new RuntimeException("주문 취소가 불가능합니다.");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderID(orderId);


        for(OrderItem orderItem : orderItems) {
            Item item = itemRepository.findById(orderItem.getItem().getID())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

            // 재고복구
            item.setStock(item.getStock() + orderItem.getCount());
            itemRepository.save(item);
        }

        // 주문삭제
        orderRepository.delete(order);

    }

    // order status 업데이트
    public void updateOrderStatus(long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        Delivery delivery = deliveryRepository.findByOrderID(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 배송정보를 찾을수 없습니다."));

        // 배송중으로 들어오면 delivery도 같이 배송중으로 변경
        if(status.equals(Order.OrderStatus.SHIPPED)) {
            delivery.setStatus(Delivery.DeliveryStatus.SHIPPED);
            deliveryRepository.save(delivery);
        }

        // 배송완료로 들어오면 delivery도 같이 배송완료로 변경
        if(status.equals(Order.OrderStatus.DELIVERED)) {
            delivery.setStatus(Delivery.DeliveryStatus.DELIVERED);
            deliveryRepository.save(delivery);
        }

        order.setStatus(status);
        orderRepository.save(order);
    }
}
