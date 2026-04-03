package com.schoolmgmt.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Formatter;

@Service
@Slf4j
public class RazorpayService {

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(apiKey, apiSecret);
            log.info("Razorpay client initialized successfully");
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
        }
    }

    public String createOrder(BigDecimal amount, String receipt, JSONObject notes) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);
        String orderId = order.get("id");
        log.info("Razorpay order created: {} for receipt: {} amount: {}", orderId, receipt, amount);
        return orderId;
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes());
            String computedSignature = toHexString(hash);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes());
            String computedSignature = toHexString(hash);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Payment signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String hex = formatter.toString();
        formatter.close();
        return hex;
    }
}
