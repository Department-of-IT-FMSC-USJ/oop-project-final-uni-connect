package com.uniconnect.messaging.repository;

import com.uniconnect.messaging.model.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    List<DirectMessage> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByCreatedAtAsc(
            Long senderId,
            Long recipientId,
            Long reverseSenderId,
            Long reverseRecipientId
    );

    List<DirectMessage> findBySenderIdOrRecipientIdOrderByCreatedAtDesc(Long senderId, Long recipientId);
}
