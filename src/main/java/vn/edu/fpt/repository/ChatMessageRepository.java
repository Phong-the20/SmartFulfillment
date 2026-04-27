package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. Lấy tất cả tin nhắn của MỘT phòng cụ thể
    List<ChatMessage> findByRoomIdOrderByIdAsc(String roomId);

    // 2. Lấy danh sách các Mã phòng (roomId) duy nhất để Admin in ra menu bên trái
    @Query("SELECT DISTINCT c.roomId FROM ChatMessage c WHERE c.roomId IS NOT NULL")
    List<String> findAllActiveRooms();
}