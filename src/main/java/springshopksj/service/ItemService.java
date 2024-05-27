package springshopksj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import springshopksj.dto.ItemDto;
import springshopksj.entity.Item;
import springshopksj.repository.ItemRepository;
import springshopksj.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;


    //모든 아이템 조회
    public List<ItemDto> findAllItems() {

        List<Item> findItems = itemRepository.findAll();
        
        List<ItemDto> allItemsDto = modelMapper.map(findItems, new TypeToken<List<ItemDto>>() {}.getType());

        return allItemsDto;
    }

    //카테고리별 조회
    public List<ItemDto> findByCategory(String category) {

        List<Item> findByCategoryItemList = itemRepository.findByCategory(category);

        List<ItemDto> itemListDto = modelMapper.map(findByCategoryItemList, new TypeToken<List<ItemDto>>() {}.getType());

        return itemListDto;
    }
}
