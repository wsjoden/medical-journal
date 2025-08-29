package org.example.messageservice.repository;

import org.example.messageservice.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IMessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId")
    List<Message> findMessagesByConversationId(@Param("conversationId") Long conversationId);
}
