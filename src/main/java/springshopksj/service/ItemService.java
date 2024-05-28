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

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;


    //모든 아이템 조회 (페이징 처리)
    public Page<ItemDto> findAllItems(Pageable pageable) {

        Page<Item> findItems = itemRepository.findAll(pageable);

        return findItems.map(item -> modelMapper.map(item, ItemDto.class));
    }

    //카테고리별 조회 (페이징 처리)
    public Page<ItemDto> findByCategory(String category, Pageable pageable) {

        Page<Item> findByCategory = itemRepository.findByCategory(category, pageable);

        return findByCategory.map(item -> modelMapper.map(item, ItemDto.class));
    }

    //아이템 검색
    public Page<ItemDto> searchItems(String keyword, Pageable pageable) {

        Page<Item> findBySearch = itemRepository.findByItemnameContaining(keyword, pageable);

        return findBySearch.map(item -> modelMapper.map(item, ItemDto.class));
    }

    //카테고리내에서 아이템검색
    public Page<ItemDto> searchItemsByCategoryAndKeyword(String category, String keyword, Pageable pageable) {

        Page<Item> findByCategoryAndSearch = itemRepository.findByCategoryAndItemnameContaining(category, keyword, pageable);

        return findByCategoryAndSearch.map(item -> modelMapper.map(item, ItemDto.class));
    }


    //itemId로 아이템값 찾기
    public ItemDto findById(long itemId) {

        Item item = itemRepository.findById(itemId);

        ItemDto itemDto = modelMapper.map(item, ItemDto.class);

        return itemDto;
    }

    //아이템 추가
    public String addItem(ItemDto itemDto, MemberDto memberDto) {

        Member member = memberRepository.findByUsername(memberDto.getUsername());

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

    public String deleteItem(long itemId, MemberDto memberDto) {

        Item item = itemRepository.findById(itemId);
        String role = String.valueOf(memberDto.getRole());

        //글작성자나 관리자일경우 허용
        if ( item.getMember().getID() == memberDto.getID() || role.equals("ROLE_ADMIN") ) {
            itemRepository.delete(item);
            return "삭제성공";
        }

        return "삭제할 권한이 없습니다.";
    }
}
