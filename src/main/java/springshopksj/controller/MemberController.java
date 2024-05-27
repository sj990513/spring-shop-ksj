package springshopksj.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.MemberDto;
import springshopksj.entity.Member;
import springshopksj.service.MemberService;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody MemberDto memberDto){

        String message = memberService.joinProcess(memberDto);

        if (message.equals("회원가입 성공")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
        }
    }


    @GetMapping("/admin")
    public ResponseEntity<?> admin() {

        //현재 사용자 권한 불러오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();
        String role = auth.getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            List<MemberDto> allMembers = memberService.findAllMembers();

            //나중에 관리자 페이지에 필요할것들 전달
            return new ResponseEntity<>(allMembers, HttpStatus.OK);
        }


        return new ResponseEntity<>("권한이 없습니다.", HttpStatus.UNAUTHORIZED);
    }

    //마이페이지
    @GetMapping("/userInfo/{userId}")
    public ResponseEntity<?> myPage(@PathVariable(name = "userId") long userId) {

        MemberDto memberDto = memberService.findById(userId);

        return new ResponseEntity<>(memberDto, HttpStatus.OK);
    }

    //마이페이지 수정
    @PatchMapping("/userInfo/{userId}/update")
    public ResponseEntity<?> updateInfo(@PathVariable(name = "userId") long userId,
                                        @RequestBody MemberDto memberDto) {
        
        //memberDto값은 바꾸든 바꾸지않든 기존의 모든값들이 전부 담기게 프론트에서 처리해주어야한다. (username, password, nickname, email, phone, address값들)
        String message = memberService.updateUserInfo(userId, memberDto);

        if (message.equals("업데이트 성공")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
        }
    }
}
