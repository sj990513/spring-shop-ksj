package springshopksj.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springshopksj.dto.ItemDto;
import springshopksj.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    //전체 아이템목록 조회
    @GetMapping("/items")
    public ResponseEntity<?> allItems() {

        List<ItemDto> allItemList = itemService.findAllItems();

        return new ResponseEntity<>(allItemList, HttpStatus.OK);
    }


    //상의
    @GetMapping("/items/top")
    public ResponseEntity<?> itemTop() {

        List<ItemDto> topItemList = itemService.findByCategory("상의");

        return new ResponseEntity<>(topItemList, HttpStatus.OK);
    }


    //하의
    @GetMapping("/items/pants")
    public ResponseEntity<?> itemPants() {

        List<ItemDto> pantsItemList = itemService.findByCategory("하의");

        return new ResponseEntity<>(pantsItemList, HttpStatus.OK);
    }

    //모자
    @GetMapping("/items/cap")
    public ResponseEntity<?> itemCap() {

        List<ItemDto> capItemList = itemService.findByCategory("모자");

        return new ResponseEntity<>(capItemList, HttpStatus.OK);
    }

    //신발
    @GetMapping("/items/shoes")
    public ResponseEntity<?> itemShoes() {

        List<ItemDto> shoeItemsList = itemService.findByCategory("신발");

        return new ResponseEntity<>(shoeItemsList, HttpStatus.OK);
    }

    //등등 etc
}













