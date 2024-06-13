package springshopksj.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardDto {
    private long ID;
    private String title;
    private String content;
    private LocalDateTime createdate;
    private LocalDateTime modifydate;
    private long userID;
}