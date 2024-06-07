package springshopksj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import springshopksj.dto.ItemDto;
import springshopksj.dto.MemberDto;
import springshopksj.entity.Item;
import springshopksj.entity.Member;
import springshopksj.repository.ItemRepository;
import springshopksj.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;


    //itemDto맵핑위한  메소드(userId부분)
    private ItemDto convertToItemDto(Item item) {
        ItemDto itemDto = modelMapper.map(item, ItemDto.class);
        itemDto.setUserID(item.getMember().getID());
        return itemDto;
    }

    //모든 아이템 조회 (페이징 처리)
    public Page<ItemDto> findAllItems(Pageable pageable) {

        Page<Item> findItems = itemRepository.findAll(pageable);

        return findItems.map(this::convertToItemDto);
    }

    // 카테고리별 조회 (페이징 처리)
    public Page<ItemDto> findByCategory(String category, Pageable pageable) {

        Page<Item> findByCategory = itemRepository.findByCategory(category, pageable);

        return findByCategory.map(this::convertToItemDto);
    }

    // 아이템 검색
    public Page<ItemDto> findBySearch(String keyword, Pageable pageable) {

        Page<Item> findBySearch = itemRepository.findByItemnameContaining(keyword, pageable);

        return findBySearch.map(this::convertToItemDto);
    }

    // 카테고리내에서 아이템 검색
    public Page<ItemDto> searchItemsByCategoryAndKeyword(String category, String keyword, Pageable pageable) {

        Page<Item> findByCategoryAndSearch = itemRepository.findByCategoryAndItemnameContaining(category, keyword, pageable);

        return findByCategoryAndSearch.map(this::convertToItemDto);
    }

    // itemId로 아이템값 찾기
    public ItemDto findById(long itemId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        return convertToItemDto(item);
    }

    public ItemDto updateItem(long itemId, ItemDto updateItemDto, MemberDto memberDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        // 작성자 본인만 아이템 디테일 수정가능
        if ( item.getMember().getID() == memberDto.getID() ) {

            item.setItemname(updateItemDto.getItemname());
            item.setPrice(updateItemDto.getPrice());
            item.setStock(updateItemDto.getStock());
            item.setCategory(updateItemDto.getCategory());
            item.setDescription(updateItemDto.getDescription());
            item.setImageUrl(updateItemDto.getImageUrl());

            itemRepository.save(item);
            return convertToItemDto(item);
        } else {
            throw new RuntimeException("상품을 수정할 권한이 없습니다.");
        }
    }

    //아이템 추가
    public String addItem(ItemDto itemDto, MemberDto memberDto) {

        Member member = memberRepository.findByUsername(memberDto.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        Item item = Item.builder()
                .itemname(itemDto.getItemname())
                .price(itemDto.getPrice())
                .stock(itemDto.getStock())
                .category(itemDto.getCategory())
                .description(itemDto.getDescription())
                .imageUrl(itemDto.getImageUrl())
                .createDate(LocalDateTime.now())
                .member(member)
                .build();

        itemRepository.save(item);

        return "아이템 추가 성공";
    }

    // 아이템삭제
    public String deleteItem(long itemId, MemberDto memberDto) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        String role = String.valueOf(memberDto.getRole());

        //글작성자나 관리자일경우 허용
        if ( item.getMember().getID() == memberDto.getID() || role.equals("ROLE_ADMIN") ) {
            itemRepository.delete(item);
            return "삭제성공";
        }

        return "삭제할 권한이 없습니다.";
    }
}
