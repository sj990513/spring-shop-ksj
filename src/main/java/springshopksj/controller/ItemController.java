package springshopksj.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.ItemDto;
import springshopksj.dto.MemberDto;
import springshopksj.entity.Item;
import springshopksj.service.ItemService;
import springshopksj.service.MemberService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final MemberService memberService;
    private static final int PAGE_SIZE = 10; // 페이지당 아이템개수 = 10

    //전체 아이템목록 조회 (페이징 처리), 검색어 존재할시 검색된 아이템들만 전달
    @GetMapping("/items")
    public ResponseEntity<?> allItems(@RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "search", required = false) String search) {

        PageRequest pageable = PageRequest.of(page-1 , PAGE_SIZE);

        //검색어 존재할시
        if (search != null) {
            Page<ItemDto> findBySearch = itemService.searchItems(search, pageable);
            return new ResponseEntity<>(findBySearch, HttpStatus.OK);
        }

        Page<ItemDto> allItemList = itemService.findAllItems(pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(allItemList, HttpStatus.OK);
    }

    //카테고리별 아이템 조회 (페이징 처리)
    @GetMapping("/items/{category}")
    public ResponseEntity<?> findByCategoryItemList(@PathVariable(name = "category") String category,
                                                    @RequestParam(value = "search", required = false) String search,
                                                    @RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , PAGE_SIZE);

        //검색어 존재할시
        if (search != null) {
            Page<ItemDto> findByCategoryAndSearch = itemService.searchItemsByCategoryAndKeyword(category, search, pageable);
            return new ResponseEntity<>(findByCategoryAndSearch, HttpStatus.OK);
        }

        Page<ItemDto> findByCategory = itemService.findByCategory(category, pageable);

        return new ResponseEntity<>(findByCategory, HttpStatus.OK);
    }


    //아이템 상세보기
    @GetMapping("/items/item/{itemId}")
    public ResponseEntity<?> itemDetail(@PathVariable(name = "itemId") long itemId) {

        ItemDto findItem = itemService.findById(itemId);

        return new ResponseEntity<>(findItem, HttpStatus.OK);
    }

    //아이템 추가, 권한관련해서 나중에 한번더 체크해보자
    @PostMapping("/items/item/add")
    public ResponseEntity<?> addItem(@RequestBody ItemDto itemDto) {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (memberDto == null )
            return new ResponseEntity<>("권한이 없습니다.", HttpStatus.UNAUTHORIZED);

        String message = itemService.addItem(itemDto, memberDto.getUsername());

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}













