package com.teambiund.bander.auth_server.auth.service.update;

import com.teambiund.bander.auth_server.auth.dto.request.HistoryRequest;
import com.teambiund.bander.auth_server.auth.entity.History;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class HistoryService {
  private final AuthRepository authRepository;
  private final KeyProvider keyProvider;

  /** 이력 생성 - Auth 엔티티의 편의 메서드를 사용하여 양방향 연관관계 설정 - Cascade 설정으로 History 엔티티 자동 저장 */
  public void createHistory(HistoryRequest req) {
    History history =
        History.builder()
            .id(keyProvider.generateKey())
            .beforeColumnValue(req.getBeforeValue())
            .afterColumnValue(req.getAfterValue())
            .updatedColumn(req.getUpdatedColumn())
            .updatedAt(LocalDateTime.now())
            .build();

    // 편의 메서드 사용 - 양방향 연관관계 설정
    req.getAuth().addHistory(history);

    // CascadeType.ALL로 인해 auth만 save하면 history도 자동 저장됨
    // 단, Auth가 이미 영속 상태라면 별도 save 불필요 (변경 감지로 자동 반영)
    authRepository.save(req.getAuth());
  }
}
