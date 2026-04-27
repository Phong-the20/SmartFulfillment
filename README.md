# 🚀 SmartTech Fulfillment System (E-Commerce & Logistics)

Dự án Hệ thống Quản lý Chuỗi cung ứng và Bán lẻ thiết bị công nghệ - **FPT University Can Tho**.

## 🌟 Tính năng nổi bật (Key Features)

Dự án không chỉ dừng lại ở một trang bán hàng thông thường mà tập trung vào xử lý các logic phức tạp:

* **📍 Quản lý kho đa điểm trên Bản đồ:** Tích hợp **Leaflet API** để quản lý danh sách kho hàng theo tọa độ thực tế. Hỗ trợ điều phối hàng hóa (Transfer Stock) giữa các kho trực quan ngay trên bản đồ.
* **🛡️ Logic Đánh giá nghiêm ngặt (Strict Review Logic):** Chống spam đánh giá ảo bằng thuật toán: `Số đơn hàng đã nhận > Số lượt đã đánh giá`. Chỉ khi khách hàng mua và nhận hàng thành công mới được phép để lại bình luận.
* **🚫 Bộ lọc ngôn từ (Bad Words Filter):** Backend tự động quét và che chắn các từ ngữ không phù hợp trước khi lưu vào Database.
* **💳 Thanh toán đa phương thức:** Tích hợp cổng thanh toán trực tuyến **MoMo API** và thanh toán khi nhận hàng (COD).
* **🔐 Bảo mật & Phân quyền:** Sử dụng **Spring Security** để phân quyền Admin/User chặt chẽ. Hỗ trợ đăng nhập truyền thống và **Google OAuth2**.

## 🛠 Tech Stack

* **Backend:** Java 21, Spring Boot 3.2.4, Spring Security, Spring Data JPA.
* **Database:** SQL Server.
* **Frontend:** Thymeleaf, Bootstrap 5, Javascript, Leaflet JS.
* **Hỗ trợ:** Cloudinary (Quản lý hình ảnh), Gmail API (Gửi mail đơn hàng & OTP), Maven.

## 📦 Hướng dẫn cài đặt (Installation)

1.  **Cơ sở dữ liệu:** * Import file script SQL trong thư mục `/database/database.sql` vào SQL Server của bạn.
2.  **Cấu hình:**
    * Tìm file `src/main/resources/application.properties.example`.
    * Đổi tên thành `application.properties` và điền các thông tin cá nhân của bạn (DB Password, Mail App Password, Google ClientID/Secret...).
3.  **Chạy ứng dụng:**
    * Mở dự án bằng IntelliJ IDEA hoặc Eclipse.
    * Chạy class `Main.java` hoặc sử dụng lệnh `mvn spring-boot:run`.

## 👤 Thông tin tác giả
* **Họ tên:** Trần Thế Phong (PhongTT)
* **MSSV:** CE190157
* **Trường:** FPT University Can Tho 

---
*Dự án này được xây dựng với mục đích học tập và áp dụng các kỹ thuật Backend Java chuyên sâu.*
