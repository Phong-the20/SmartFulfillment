package vn.edu.fpt.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // Phân loại lỗi và gắn đuôi URL tương ứng
        if (exception instanceof DisabledException) {
            response.sendRedirect("/login?error=disabled"); // Lỗi chưa kích hoạt
        } else if (exception instanceof LockedException) {
            response.sendRedirect("/login?error=locked"); // Lỗi bị khóa
        } else {
            response.sendRedirect("/login?error=bad_credentials"); // Sai pass/username
        }
    }
}