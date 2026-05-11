package org.ln678090.connecthub.tracking.kafka;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.tracking.entity.UserAction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaTemplate<String, UserAction> kafkaTemplate;

    // create Producer
    public void sendAction(UserAction userAction) {
        String partitionKey = userAction.getUserId()!= null
                ? userAction.getUserId().toString() : "anonymous";
        kafkaTemplate.send("user-actions-topic", partitionKey, userAction);
    }
}
