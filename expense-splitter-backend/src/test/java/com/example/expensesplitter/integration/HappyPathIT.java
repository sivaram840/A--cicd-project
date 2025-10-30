package com.example.expensesplitter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A small end-to-end happy path using in-memory DB (H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class HappyPathIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void fullFlow_registerLoginCreateGroupAddMemberExpenseBalances() throws Exception {
        // 1) register user A
        var regA = Map.of("name", "IT Alice", "email", "it.alice@example.com", "password", "pass123");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(regA)))
                .andExpect(status().isOk());

        // 2) register user B
        var regB = Map.of("name", "IT Bob", "email", "it.bob@example.com", "password", "pass123");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(regB)))
                .andExpect(status().isOk());

        // 3) login A and extract token
        var loginA = Map.of("email", "it.alice@example.com", "password", "pass123");
        var loginResp = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn();
        String token = mapper.readTree(loginResp.getResponse().getContentAsString()).get("token").asText();
        String auth = "Bearer " + token;

        // 4) create group
        var groupBody = Map.of("name", "IT Trip");
        var groupResp = mvc.perform(post("/api/groups").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(groupBody)))
                .andExpect(status().isOk())
                .andReturn();
        Long groupId = mapper.readTree(groupResp.getResponse().getContentAsString()).get("id").asLong();

        // 5) add Bob by email
        var addBody = Map.of("email", "it.bob@example.com", "role", "MEMBER");
        mvc.perform(post("/api/groups/" + groupId + "/members").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(addBody)))
                .andExpect(status().isOk());

        // 6) create expense (Alice pays 100 split equal)
        var expenseBody = Map.of("amount", 100.00, "payerId", 1, "splitType", "EQUAL"); // payerId will be 1 in H2
        mvc.perform(post("/api/groups/" + groupId + "/expenses").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(expenseBody)))
                .andExpect(status().isOk());

        // 7) get balances
        var balancesResp = mvc.perform(get("/api/groups/" + groupId + "/expenses/balances").header("Authorization", auth))
                .andExpect(status().isOk()).andReturn();

        var arr = mapper.readTree(balancesResp.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        // Expect two balances, total sum to zero
        double sum = 0;
        for (var node : arr) {
            sum += node.get("netBalance").asDouble();
        }
        assertThat(Math.abs(sum) < 0.01).isTrue();
    }
}

