package com.uniconnect.messaging.controller;

import com.uniconnect.messaging.dto.ConversationSummaryResponse;
import com.uniconnect.messaging.dto.DirectMessageRequest;
import com.uniconnect.messaging.dto.DirectMessageResponse;
import com.uniconnect.messaging.model.DirectMessage;
import com.uniconnect.messaging.repository.DirectMessageRepository;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class DirectMessageController {

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final MentorConnectionRepository mentorConnectionRepository;

    public DirectMessageController(
            DirectMessageRepository directMessageRepository,
            UserRepository userRepository,
            MentorConnectionRepository mentorConnectionRepository
    ) {
        this.directMessageRepository = directMessageRepository;
        this.userRepository = userRepository;
        this.mentorConnectionRepository = mentorConnectionRepository;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversations(@AuthenticationPrincipal User currentUser) {
        List<DirectMessage> messages = directMessageRepository.findBySenderIdOrRecipientIdOrderByCreatedAtDesc(
                currentUser.getId(), currentUser.getId()
        );

        Map<Long, ConversationSummaryResponse> summaries = new LinkedHashMap<>();
        for (DirectMessage message : messages) {
            Long otherUserId = currentUser.getId().equals(message.getSenderId())
                    ? message.getRecipientId()
                    : message.getSenderId();

            if (summaries.containsKey(otherUserId)) {
                continue;
            }

            User otherUser = userRepository.findById(otherUserId).orElse(null);
            if (otherUser == null) {
                continue;
            }

            summaries.put(otherUserId, new ConversationSummaryResponse(
                    otherUser.getId(),
                    otherUser.getFullName(),
                    otherUser.getEmail(),
                    otherUser.getDepartment(),
                    otherUser.getRole(),
                    message.getContent(),
                    message.getCreatedAt()
            ));
        }

        return ResponseEntity.ok(new ArrayList<>(summaries.values()));
    }

    @GetMapping("/with/{otherUserId}")
    public ResponseEntity<List<DirectMessageResponse>> getMessagesWith(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long otherUserId
    ) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        ensureMessagingAllowed(currentUser, otherUser);

        List<DirectMessageResponse> responses = directMessageRepository
                .findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByCreatedAtAsc(
                        currentUser.getId(), otherUserId, otherUserId, currentUser.getId()
                )
                .stream()
                .map(message -> mapToResponse(message, currentUser, otherUser))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<DirectMessageResponse> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody DirectMessageRequest request
    ) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found."));

        ensureMessagingAllowed(currentUser, recipient);

        DirectMessage message = new DirectMessage();
        message.setSenderId(currentUser.getId());
        message.setRecipientId(recipient.getId());
        message.setContent(request.getContent().trim());

        DirectMessage saved = directMessageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved, currentUser, recipient));
    }

    private void ensureMessagingAllowed(User currentUser, User otherUser) {
        if (currentUser.getId().equals(otherUser.getId())) {
            throw new IllegalArgumentException("You cannot message yourself.");
        }

        boolean currentIsStudent = currentUser.getRole() == Role.UNDERGRADUATE;
        boolean otherIsStudent = otherUser.getRole() == Role.UNDERGRADUATE;
        boolean currentIsMentor = currentUser.getRole() == Role.ACADEMIC_MENTOR || currentUser.getRole() == Role.INDUSTRY_MENTOR;
        boolean otherIsMentor = otherUser.getRole() == Role.ACADEMIC_MENTOR || otherUser.getRole() == Role.INDUSTRY_MENTOR;

        if (!((currentIsStudent && otherIsMentor) || (currentIsMentor && otherIsStudent))) {
            throw new IllegalArgumentException("Messaging is only allowed between students and mentors.");
        }

        Integer studentId = currentIsStudent ? currentUser.getId().intValue() : otherUser.getId().intValue();
        Integer mentorId = currentIsMentor ? currentUser.getId().intValue() : otherUser.getId().intValue();

        boolean connected = mentorConnectionRepository.existsByStudentIdAndMentorIdAndConnectionStatus(
                studentId, mentorId, ConnectionStatus.Approved
        );

        if (!connected) {
            throw new IllegalArgumentException("Messaging is only available for approved mentor-student connections.");
        }
    }

    private DirectMessageResponse mapToResponse(DirectMessage message, User currentUser, User otherUser) {
        String senderName = message.getSenderId().equals(currentUser.getId())
                ? currentUser.getFullName()
                : otherUser.getFullName();
        String recipientName = message.getRecipientId().equals(currentUser.getId())
                ? currentUser.getFullName()
                : otherUser.getFullName();

        return new DirectMessageResponse(
                message.getId(),
                message.getSenderId(),
                senderName,
                message.getRecipientId(),
                recipientName,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
