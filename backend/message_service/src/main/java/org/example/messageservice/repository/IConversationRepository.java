package org.example.messageservice.repository;

import org.example.messageservice.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId2")
    List<Conversation> findConversationsByUserId(@Param("userId") String userId, @Param("userId2") String userId2);

    Conversation findById(long id);
}