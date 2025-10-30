package com.example.expensesplitter.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ExpenseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectExpenseWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/groups/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100, \"payerId\":1, \"splitType\":\"EQUAL\"}"))
                .andExpect(status().isForbidden());
    }
}
