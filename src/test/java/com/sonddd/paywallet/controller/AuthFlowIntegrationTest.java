package com.sonddd.paywallet.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonddd.paywallet.dto.request.LoginRequest;
import com.sonddd.paywallet.dto.request.RegisterRequest;
import com.sonddd.paywallet.dto.request.TopUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: chạy toàn bộ context Spring Boot với H2, kiểm thử luồng
 * đăng ký -> đăng nhập -> nạp tiền -> xem số dư qua HTTP (MockMvc).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Luồng xác thực & nạp tiền (end-to-end)")
class AuthFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("Đăng ký, đăng nhập, nạp 500.000 rồi số dư phải bằng 500.000")
    void fullFlow() throws Exception {
        // 1) Đăng ký
        RegisterRequest register = new RegisterRequest("charlie", "charlie@test.local", "charlie123", "Charlie Test");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("charlie"))
                .andExpect(jsonPath("$.data.balance").value(0));

        // 2) Đăng nhập -> lấy token
        LoginRequest login = new LoginRequest("charlie", "charlie123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String token = readToken(loginResult);
        assertThat(token).isNotBlank();

        // 3) Nạp tiền
        TopUpRequest topUp = new TopUpRequest(new BigDecimal("500000"), "Nạp test", null);
        mockMvc.perform(post("/api/wallet/topup")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topUp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("TOPUP"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        // 4) Xem số dư
        mockMvc.perform(get("/api/wallet/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(500000));

        // 5) Lịch sử: không lọc, lọc theo loại, lọc theo khoảng thời gian
        mockMvc.perform(get("/api/payments/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/payments/history").param("type", "TOPUP")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].type").value("TOPUP"));

        mockMvc.perform(get("/api/payments/history").param("type", "PAYMENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(get("/api/payments/history")
                        .param("fromDate", "2020-01-01T00:00:00Z")
                        .param("toDate", "2999-01-01T00:00:00Z")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // 6) Sắp xếp theo trường không tồn tại -> 400 (không phải 500)
        mockMvc.perform(get("/api/payments/history").param("sort", "string")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Gọi API cần đăng nhập mà không có token -> 401/403")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/wallet/me"))
                .andExpect(status().is4xxClientError());
    }

    private String readToken(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
