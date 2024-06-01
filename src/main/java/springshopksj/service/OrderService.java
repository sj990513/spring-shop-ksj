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

    private OrderDto convertToOrderDto(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setUserID(order.getMember().getID());
        return orderDto;
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



    // 개별주문생성
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

        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setUserID(order.getMember().getID());

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

    // order캔슬
    public void cancelOrder(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        if (order.getStatus() != Order.OrderStatus.ORDERED) {
            throw new RuntimeException("주문 취소가 불가능합니다.");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // order status 업데이트
    public void updateOrderStatus(long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        order.setStatus(status);
        orderRepository.save(order);
    }

    //order-item에 추가
    public void addItemToOrder(long orderId, OrderItemDto orderItemDto) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        Item item = itemRepository.findById(orderItemDto.getItemID())
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        if (item.getStock() < orderItemDto.getCount()) {
            throw new RuntimeException("상품[" + item.getItemname() + "]의 재고가 충분하지 않습니다.");
        }

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .orderprice(item.getPrice())
                .count(orderItemDto.getCount())
                .build();

        orderItemRepository.save(orderItem);

        // 재고 감소
        item.setStock(item.getStock() - orderItemDto.getCount());
        itemRepository.save(item);
    }

    // 주문 삭제
    @Transactional
    public void removeItemFromOrder(long orderId, Long itemId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        OrderItem orderItem = orderItemRepository.findByOrderIDAndItemID(orderId, itemId)
                .orElseThrow(() -> new RuntimeException("OrderItem을 찾을수 없습니다."));

        orderItemRepository.delete(orderItem);

        // 재고 복구
        Item item = orderItem.getItem();
        item.setStock(item.getStock() + orderItem.getCount());
        itemRepository.save(item);
    }
}
