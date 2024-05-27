package springshopksj.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private long id;

    private String itemName;

    private int price;

    private int stock;

    private String category;

    private String description;

    private String imageUrl;

    private LocalDateTime createDate;

    private Long memberId;

}