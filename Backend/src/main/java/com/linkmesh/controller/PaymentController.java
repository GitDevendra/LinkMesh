package com.linkmesh.controller;

import com.linkmesh.entity.User;
import com.linkmesh.repository.UserRepository;
import com.linkmesh.service.CustomOAuth2UserService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${razorpay.key.id}") private String keyId;
    @Value("${razorpay.key.secret}") private String keySecret;
    @Value("${app.premium.amount}") private int amount;

    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;

    // Step 1: Frontend calls this to get an order_id
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder() throws Exception {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject opts = new JSONObject();
        opts.put("amount", amount);
        opts.put("currency", "INR");
        opts.put("receipt", "receipt_" + System.currentTimeMillis());

        Order order = client.orders.create(opts);
        return ResponseEntity.ok(Map.of(
                "orderId", order.get("id"),
                "amount",  amount,
                "currency", "INR",
                "keyId",   keyId
        ));
    }

    // Step 2: After payment, frontend calls this to verify and upgrade
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) throws Exception {
        String orderId   = body.get("razorpay_order_id");
        String paymentId = body.get("razorpay_payment_id");
        String signature = body.get("razorpay_signature");

        // HMAC-SHA256 verification
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
        String expected = bytesToHex(mac.doFinal(payload.getBytes()));

        if (!expected.equals(signature)) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid signature"));
        }

        // Safe to upgrade now
        User user = customOAuth2UserService.getCurrentUser();
        user.setRole(User.Role.PREMIUM);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Upgraded to PREMIUM", "role", "PREMIUM"));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}