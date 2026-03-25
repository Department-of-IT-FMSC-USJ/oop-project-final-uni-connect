package com.uniconnect.messaging.repository;

import com.uniconnect.messaging.model.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    List<DirectMessage> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByCreatedAtAsc(
            Long senderId,
            Long recipientId,
            Long reverseSenderId,
            Long reverseRecipientId
    );

    List<DirectMessage> findBySenderIdOrRecipientIdOrderByCreatedAtDesc(Long senderId, Long recipientId);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);

    long countBySenderIdAndRecipientIdAndReadAtIsNull(Long senderId, Long recipientId);

    @Modifying
    @Query("update DirectMessage m set m.readAt = CURRENT_TIMESTAMP where m.senderId = :senderId and m.recipientId = :recipientId and m.readAt is null")
    int markConversationAsRead(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);
}
