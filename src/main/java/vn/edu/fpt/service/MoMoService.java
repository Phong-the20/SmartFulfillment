package vn.edu.fpt.service;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class MoMoService {

    private final String partnerCode = "MOMOLRJZ20181206";
    private final String accessKey = "mTCKt9W3eU1m39TW";
    private final String secretKey = "SetA5RDnLHvt51AULf51DyauxUo3kDU6";

    private final String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
    private final String returnUrl = "http://localhost:8080/checkout/momo-return";
    private final String notifyUrl = "http://localhost:8080/checkout/momo-notify";

    public String createMoMoPayment(String orderId, long amount) {


        try {
            String requestId = String.valueOf(System.currentTimeMillis());

            // Xóa hết dấu cách cho an toàn tuyệt đối
            String orderInfo = "ThanhToanDonHang_" + orderId;

            // 2. MƯỢN REQUEST TYPE CỦA BẠN CỦA ÔNG
            String requestType = "payWithATM";
            String extraData = "";

            // TẠO CHUỖI KÝ (Tuyệt đối chuẩn xác)
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            // MÃ HÓA CHỮ KÝ (HMAC-SHA256)
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hashBytes = sha256_HMAC.doFinal(rawHash.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String signature = hexString.toString();

            // TẠO JSON BODY
            JSONObject json = new JSONObject();
            json.put("partnerCode", partnerCode);
            json.put("requestId", requestId);
            json.put("amount", amount);
            json.put("orderId", orderId);
            json.put("orderInfo", orderInfo);
            json.put("redirectUrl", returnUrl);
            json.put("ipnUrl", notifyUrl);
            json.put("lang", "vi");
            json.put("requestType", requestType);
            json.put("extraData", extraData);
            json.put("signature", signature);

            // GỬI REQUEST BẰNG RestTemplate CỦA ANH EM MÌNH
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(endpoint, entity, String.class);

            // TRẢ VỀ LINK QUÉT MÃ
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("payUrl")) {
                return jsonResponse.getString("payUrl");
            } else {
                throw new RuntimeException("MoMo từ chối: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=momo_failed";
        }
    }
}