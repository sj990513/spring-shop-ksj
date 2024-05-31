package springshopksj.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.*;
import springshopksj.entity.Member;
import springshopksj.entity.Order;
import springshopksj.service.MemberService;
import springshopksj.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;

    // 주문
    @PostMapping("/orders/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest ) {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        Order order = orderService.createOrder(memberDto, orderRequest.getOrderItems(), orderRequest.getDeliveryDto(), orderRequest.getPaymentDto());

        return new ResponseEntity<>(order, HttpStatus.OK);
    }


    //order-item이 주문하나
    //order-item들이 모여서 order(전체주문)


    // 특정 주문 조회
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable(name="orderId") Long orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }

    // 사용자별 주문내역 조회
    @GetMapping("/orders/user")
    public ResponseEntity<?> getUserOrders() {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<OrderDto> orders = orderService.getOrdersByUserId(memberDto);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // 주문취소
    @DeleteMapping("/orders/{orderId}/cancle")
    public ResponseEntity<?> cancelOrder(@PathVariable(name="orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>("주문취소", HttpStatus.OK);
    }

    // 주문상태 업데이트 (관리자가 주문 상태를 업데이트 ex) 주문 -> 배송 중으로 변경)
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable(name="orderId") Long orderId,
                                               @RequestBody OrderStatusUpdateRequest request) {
        orderService.updateOrderStatus(orderId, request.getStatus());
        return new ResponseEntity<>( HttpStatus.OK);
    }

    // 주문아이템 추가
    @PostMapping("/orders/{orderId}/items/add")
    public ResponseEntity<?> addItemToOrder(@PathVariable(name="orderId") Long orderId,
                                            @RequestBody OrderItemDto orderItemDto) {

        orderService.addItemToOrder(orderId, orderItemDto);

        return new ResponseEntity<>("추가 성공", HttpStatus.OK);
    }

    // 주문아이템 삭제
    @DeleteMapping("/orders/{orderId}/items/{itemId}/delete")
    public ResponseEntity<?> removeItemFromOrder(@PathVariable(name="orderId") Long orderId,
                                                 @PathVariable(name="itemId") Long itemId) {
        orderService.removeItemFromOrder(orderId, itemId);
        return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
    }

    //주문 검증 및 예외처리
}
