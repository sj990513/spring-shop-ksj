package springshopksj.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.*;
import springshopksj.entity.Member;
import springshopksj.entity.Order;
import springshopksj.entity.OrderItem;
import springshopksj.service.MemberService;
import springshopksj.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;


    // 로그인한 사용자 장바구니 조회
    @GetMapping("/orders/cart")
    public ResponseEntity<?> userCart() {
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderItemDto> orderItemDtos  = orderService.findAllCart(memberDto);

        return new ResponseEntity<>(orderItemDtos, HttpStatus.OK);

    }

    // 장바구니 품목 추가(생성)
    /** orderRequest
     * {
     *     "orderItems": [
     *         {
     *             "itemId": 3,
     *             "count": 3
     *         }
     *     ]
     */
    @PostMapping("/orders/cart/add-cart")
    public ResponseEntity<?> createCart(@RequestBody OrderRequest orderRequest) {
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderItemDto> orderItemDtos  = orderService.addCart(memberDto, orderRequest.getOrderItems());

        return new ResponseEntity<>(orderItemDtos, HttpStatus.OK);
    }


    // 장바구니 품목 삭제
    /** 
     * order_item의 ID값 전달
     */
    @DeleteMapping("/orders/cart/{order_itemID}/delete-item")
    public ResponseEntity<?> deleteCartItem(@PathVariable(name="order_itemID") long order_itemID) {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderItemDto> orderItemDtos  = orderService.deleteCartItem(memberDto, order_itemID);
        
        return new ResponseEntity<>(orderItemDtos, HttpStatus.OK);
    }

    //장바구니 전체 삭제
    @DeleteMapping("/orders/cart/delete-cart")
    public ResponseEntity<?> deleteCart() {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        orderService.deleteCart(memberDto);

        return new ResponseEntity<>("장바구니 삭제성공", HttpStatus.OK);
    }

    // 장바구니에서 주문
    @PostMapping("/orders/cart/create")
    public ResponseEntity<?> orderFromCart(@RequestBody OrderRequest orderRequest) {
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        OrderDto orderDto  = orderService.createOrderFromCart(memberDto, orderRequest.getDeliveryDto(), orderRequest.getPaymentDto());

        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }


    // 아이템 디테일에서 즉시 주문

    /**
     *
     * {
     *     "orderItems": [
     *         {
     *             "itemID": 3,
     *             "count": 3
     *         },
     *     ],
     *     "deliveryDto": {
     *         "address": "123 Main St, Anytown, USA"
     *     },
     *     "paymentDto": {
     *         "paymentMethod": "CREDIT_CARD",
     *         "amount": 300
     *     }
     * }
     */
    @PostMapping("/orders/order-now")
    public ResponseEntity<?> orderNow(@RequestBody OrderRequest orderRequest) {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        OrderDto orderDto = orderService.createOrder(memberDto, orderRequest.getOrderItems(), orderRequest.getDeliveryDto(), orderRequest.getPaymentDto());

        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }

    // 로그인한 사용자 주문내역 조회
    @GetMapping("/orders/user")
    public ResponseEntity<?> getUserOrders() {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderDto> orders = orderService.getOrdersByUserId(memberDto);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }


    // 특정 주문 조회 (관리자페이지에서 사용)
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable(name="orderId") Long orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }


    // 주문취소 (사용자가 요청)
    @PatchMapping("/orders/{orderId}/cancle")
    public ResponseEntity<?> cancelOrder(@PathVariable(name="orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>("주문취소", HttpStatus.OK);
    }


    // (관리자가) 캔슬된 오더 허용후 삭제처리
    @DeleteMapping("/orders/{orderId}/accept-cancle")
    public ResponseEntity<?> acceptCancleOrder(@PathVariable(name="orderId") Long orderId) {
        orderService.acceptCancelOrder(orderId);

        return new ResponseEntity<>("주문삭제완료", HttpStatus.OK);

    }


    // 주문상태 업데이트 관리자가 주문 상태를 업데이트 ex) 배송중 -> 배송완료 변경)
    @PatchMapping("/orders/{orderId}/update-status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable(name="orderId") Long orderId,
                                               @RequestBody OrderStatusUpdateRequest request) {
        orderService.updateOrderStatus(orderId, request.getStatus());
        return new ResponseEntity<>("변경완료", HttpStatus.OK);
    }



    //주문 검증 및 예외처리
}
