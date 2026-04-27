package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.ChatMessage;
import vn.edu.fpt.repository.ChatMessageRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageRepository chatRepo;

    // 1. NHẬN TIN NHẮN THEO PHÒNG VÀ PHÁT LẠI ĐÚNG VÀO PHÒNG ĐÓ
    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/messages/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        chatMessage.setRoomId(roomId);
        chatMessage.setTimestamp(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        try {
            chatRepo.save(chatMessage);
        } catch (Exception e) {
            System.err.println("LỖI LƯU DB (Chat): " + e.getMessage());
        }
        return chatMessage;
    }

    // 2. API LẤY LỊCH SỬ CỦA 1 PHÒNG CỤ THỂ
    @GetMapping("/api/chat/history/{roomId}")
    @ResponseBody
    public List<ChatMessage> getChatHistoryByRoom(@PathVariable String roomId) {
        try {
            return chatRepo.findByRoomIdOrderByIdAsc(roomId);
        } catch (Exception e) {
            return List.of();
        }
    }

    // 3. API LẤY DANH SÁCH KHÁCH HÀNG ĐANG CHAT CHO ADMIN
    @GetMapping("/api/chat/rooms")
    @ResponseBody
    public List<String> getActiveRooms() {
        return chatRepo.findAllActiveRooms();
    }
}