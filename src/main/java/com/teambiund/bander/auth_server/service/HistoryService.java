package com.teambiund.bander.auth_server.service;

import com.teambiund.bander.auth_server.dto.request.HistoryRequest;
import com.teambiund.bander.auth_server.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
@Transactional
public class HistoryService {
    private final HistoryRepository historyRepository;

    public void createHistory(HistoryRequest req) {
        historyRepository.save(HistoryRequest.toHistory(req));
    }
}
