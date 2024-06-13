package springshopksj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springshopksj.repository.BoardRepository;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;


}
