package vn.edu.fpt.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId; // Mã phòng chat (Ví dụ: PhongTT hoặc GUEST_1234)

    private String sender; // Tên người gửi hiển thị

    @Nationalized // Ép Hibernate dùng kiểu N (Unicode) khi truy vấn
    @Column(columnDefinition = "NVARCHAR(MAX)") // Ép Database tạo cột kiểu NVARCHAR
    private String content;

    private String timestamp;

    // --- Constructors ---
    public ChatMessage() {}

    public ChatMessage(String roomId, String sender, String content) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
    }

    // --- Getters và Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}