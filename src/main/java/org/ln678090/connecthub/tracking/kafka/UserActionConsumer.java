package org.ln678090.connecthub.tracking.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ln678090.connecthub.tracking.entity.UserAction;
import org.ln678090.connecthub.tracking.repository.UserActionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserActionConsumer {
    private final UserActionRepository userActionRepository;
@KafkaListener(
        topics = "user-actions-topic",
        groupId = "connecthub-tracking-group",
        concurrency = "3"
)
    public void consume(UserAction userAction) {
        try {
            userActionRepository.save(userAction);
            log.info("Save action :{} ,User:{}", userAction.getActionType(), userAction.getUserId());
        }catch (Exception e){
            log.error("Error save action :{} ,User:{} , error :{}", userAction.getActionType(), userAction.getUserId(), e);
        }
    }
}
