package springshopksj.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.DeliveryDto;
import springshopksj.dto.ItemDto;
import springshopksj.dto.MemberDto;
import springshopksj.dto.OrderDto;
import springshopksj.service.DeliveryService;
import springshopksj.service.ItemService;
import springshopksj.service.MemberService;
import springshopksj.service.OrderService;
import springshopksj.utils.Constants;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final ItemService itemService;
    private final DeliveryService deliveryService;

    //모든 멤버조회
    @GetMapping("/member-list")
    public ResponseEntity<?> allMemberList(@RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "search", required = false) String search) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        //검색어 존재할시
        if (search != null) {
            Page<MemberDto> findBySearch = memberService.findAllBySearch(search, pageable);
            return new ResponseEntity<>(findBySearch, HttpStatus.OK);
        }

        Page<MemberDto> allMembers = memberService.findAllMembers(pageable);

        return new ResponseEntity<>(allMembers, HttpStatus.OK);
    }

    // 회원 권한 변경 - ROLE_USER, ROLE_ADMIN
    /**
     * memberDto
     *{
     *     "role": "ROLE_ADMIN"
     * }
     */
    @PatchMapping("/member-list/{userId}/update-status")
    public ResponseEntity<?> updateMemberStatus(@PathVariable(name = "userId") long userId, @RequestBody MemberDto memberDto) {

        MemberDto updateMemberDto = memberService.updateRole(userId, memberDto);

        return new ResponseEntity<>(updateMemberDto, HttpStatus.OK);
    }


    // 전체 아이템조회
    /**
     * http://localhost:8080/admin/item-list?page=3&search=example
     */
    @GetMapping("/item-list")
    public ResponseEntity<?> allItems(@RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "search", required = false) String search) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        //검색어 존재할시
        if (search != null) {
            Page<ItemDto> findBySearch = itemService.findBySearch(search, pageable);
            return new ResponseEntity<>(findBySearch, HttpStatus.OK);
        }

        Page<ItemDto> allItemList = itemService.findAllItems(pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(allItemList, HttpStatus.OK);
    }

    //아이템삭제
    @DeleteMapping("/item-list/{itemId}/delete-item")
    public ResponseEntity<?> deleteItem(@PathVariable(name = "itemId") long itemId) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        String message = itemService.deleteItem(itemId, memberDto);

        if(message.equals("삭제성공"))
            return new ResponseEntity<>(message, HttpStatus.OK);

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }


    // 전체 주문조회
    @GetMapping("/order-list")
    public ResponseEntity<?> allOrder(@RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<OrderDto> allOrderList = orderService.findAllOrders(pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(allOrderList, HttpStatus.OK);
    }

    // status별 주문조회
    /**
     * http://localhost:8080/admin/order-list/ordered
     *
     * status : ordered, paid, cancelled, shipped, delivered
     */
    @GetMapping("/order-list/category/{status}")
    public ResponseEntity<?> orderByStatus(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @PathVariable(name="status") String status) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<OrderDto> statusOrderList = orderService.findByStatus(status, pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(statusOrderList, HttpStatus.OK);
    }

    // 특정 주문 조회
    /**
     * http://localhost:8080/admin/order-list/3
     */
    @GetMapping("/order-list/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable(name="orderId") Long orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }


    // 캔슬된 오더 허용 - 관리자
    /**
     * http://localhost:8080/admin/order-list/3/accept-cancle
     */
    @PatchMapping("/order-list/{orderId}/accept-cancle")
    public ResponseEntity<?> acceptCancleOrder(@PathVariable(name="orderId") Long orderId) {
        orderService.acceptCancelOrder(orderId);

        return new ResponseEntity<>("주문 취소 완료", HttpStatus.OK);

    }

    // 주문상태 업데이트 ex) 배송중 -> 배송완료 - 관리자
    /**
     * http://localhost:8080/admin/orders/3/update-status
     * orderDto
     * {
     *  "status" : "ORDERED"
     * }
     *
     * status : ORDERED, PAID, CANCLE, CANCELLED, SHIPPED, DELIVERED
     */
    @PatchMapping("/order-list/{orderId}/update-status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable(name="orderId") Long orderId,
                                               @RequestBody OrderDto orderDto) {
        orderService.updateOrderStatus(orderId, orderDto.getStatus());
        return new ResponseEntity<>("변경완료", HttpStatus.OK);
    }

    // 전체 배달정보 조회
    @GetMapping("/delivery-list")
    public ResponseEntity<?> allDelivery(@RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<DeliveryDto> allDelivery = deliveryService.findAllDelivery(pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(allDelivery, HttpStatus.OK);
    }

    // status별 배달정보 조회
    /**
     * http://localhost:8080/admin/delivery-list/ready
     *
     * status : ready, shipped, delivered, cancelled
     */
    @GetMapping("/delivery-list/category/{status}")
    public ResponseEntity<?> deliveryByStatus(@RequestParam(value = "page", defaultValue = "1") int page,
                                              @PathVariable(name="status") String status) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<DeliveryDto> statusDeliveryList = deliveryService.findByStatus(status, pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(statusDeliveryList, HttpStatus.OK);
    }

    // 특정 배달정보 조회
    /**
     * http://localhost:8080/admin/delivery-list/3
     */
    @GetMapping("/delivery-list/{deliveryId}")
    public ResponseEntity<?> getDelivery(@PathVariable(name="deliveryId") Long deliveryId) {
        DeliveryDto deliveryDto = deliveryService.getDeliveryById(deliveryId);

        return new ResponseEntity<>(deliveryDto, HttpStatus.OK);
    }

    // 배달 정보 업데이트 ex) 배송중 -> 배송완료 - 관리자
    /**
     * http://localhost:8080/admin/orders/3/update-status
     * DeliveryDto
     * {
     *  "status" : "READY"
     * }
     *
     * status : READY, SHIPPED, DELIVERED, CANCLLED
     */
    @PatchMapping("/delivery-list/{deliveryId}/update-status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable(name="deliveryId") Long deliveryId,
                                               @RequestBody DeliveryDto deliveryDto) {

        deliveryService.updateDeliveryStatus(deliveryId, deliveryDto.getStatus());

        return new ResponseEntity<>("변경완료", HttpStatus.OK);
    }

}
