package springshopksj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Member;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> { //extends JpaRepository<엔티티, pk타입>

    List<Member> findFirst2ByUsernameLikeOrderByIDDesc(String username);

    //username, nickname, email, phone 중복확인
    Boolean existsByUsername(String username);
    Boolean existsByNickname(String nickname);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);

    Member findByUsername(String username);

    Member findById(long userId);

}
