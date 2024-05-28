package springshopksj.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name ="basket")
public class Basket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;

    @Column(name = "quantity")
    private int quantity;

    @CreatedDate
    @Column(name = "createdate")
    private LocalDateTime createdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", referencedColumnName = "ID")
    private Member member;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemID", referencedColumnName = "ID")
    private Item item;

}
