package vn.edu.fpt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Nơi Client (Khách và Admin) kết nối tới để mở đường hầm
        // Dùng SockJS để dự phòng nếu trình duyệt cũ không hỗ trợ WebSocket thuần
        registry.addEndpoint("/ws-smarttech").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tiền tố cho các tin nhắn từ Server gửi về Client (Khách / Admin)
        registry.enableSimpleBroker("/topic", "/queue");

        // Tiền tố cho các tin nhắn từ Client gửi lên Server xử lý
        registry.setApplicationDestinationPrefixes("/app");
    }
}