package springshopksj.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Table(name ="member")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)   //KEY값과 자동증가되는값
    private long ID;

    @Column(name = "username")
    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;

    @Column(name = "password")
    @NotBlank(message = "패스워드는 필수입니다.")
    @Size(min = 8, message = "패스워드는 최소 8자 이상입니다.")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    @Email(message = "Email은 중복되면 안됩니다.")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @CreatedDate
    @Column(name = "createdate")
    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "varchar(255)")
    private Role role;

    public enum Role {
        ROLE_ADMIN, ROLE_USER
    }

}
