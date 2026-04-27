# 🚀 SmartTech Fulfillment System (E-Commerce & Logistics)
<p align="center">
  <img src="https://github.com/user-attachments/assets/1bd6806f-a3dc-44d2-acaf-3fd2f8fe173c" width="80%" alt="Demo Map Feature">
  <br>
  <i>Hình 1: Hệ thống quản lý kho đa chi nhánh trên bản đồ thực tế</i>
</p>
Dự án Hệ thống Quản lý Chuỗi cung ứng và Bán lẻ thiết bị công nghệ - **FPT University Can Tho**.

## 🌟 Tính năng nổi bật (Key Features)

Dự án không chỉ dừng lại ở một trang bán hàng thông thường mà tập trung vào xử lý các logic phức tạp:

* **📍 Quản lý kho đa điểm trên Bản đồ:** Tích hợp **Leaflet API** để quản lý danh sách kho hàng theo tọa độ thực tế. Hỗ trợ điều phối hàng hóa (Transfer Stock) giữa các kho trực quan ngay trên bản đồ.
* **🛡️ Logic Đánh giá nghiêm ngặt (Strict Review Logic):** Chống spam đánh giá ảo bằng thuật toán: `Số đơn hàng đã nhận > Số lượt đã đánh giá`. Chỉ khi khách hàng mua và nhận hàng thành công mới được phép để lại bình luận.

<p align="center">
  <img src="https://github.com/user-attachments/assets/821d8422-dbdc-4144-9e57-014cd9b835f0" width="85%" alt="Review Form UI">
  <br>
  <i>Hình 2: Giao diện gửi đánh giá (chỉ hiển thị khi thỏa mãn điều kiện mua hàng)</i>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/7c017ece-188c-41f3-a2c3-adbc74cc21d4" width="85%" alt="Review List UI">
  <br>
  <i>Hình 3: Danh sách các đánh giá thực tế từ khách hàng</i>
</p>
* **🚫 Bộ lọc ngôn từ (Bad Words Filter):** Backend tự động quét và che chắn các từ ngữ không phù hợp trước khi lưu vào Database.
* **💳 Thanh toán đa phương thức:** Tích hợp cổng thanh toán trực tuyến **MoMo API** và thanh toán khi nhận hàng (COD).

<p align="center">
  <img src="https://github.com/user-attachments/assets/020c4bfe-d766-45a5-b02a-c476963f46fa" width="85%" alt="Payment Integration UI">
  <br>
  <i>Hình 4: Giao diện quản lý đơn hàng và tích hợp trạng thái thanh toán trực tuyến</i>
</p>

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
