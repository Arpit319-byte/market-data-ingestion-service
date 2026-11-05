package com.example.stockanalyzer.marketdata;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stockanalyzer.marketdata.logging.RequestLoggingFilter;
import com.example.stockanalyzer.marketdata.entities.RuntimeLeader;
import com.example.stockanalyzer.marketdata.exception.ConflictException;
import com.example.stockanalyzer.marketdata.repository.BackgroundJobRepository;
import com.example.stockanalyzer.marketdata.repository.RuntimeLeaderRepository;
import com.example.stockanalyzer.marketdata.service.CandleBackfillJobService;
import com.example.stockanalyzer.marketdata.service.ClusterLeaderService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductionReadinessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClusterLeaderService clusterLeaderService;

    @Autowired
    private RuntimeLeaderRepository runtimeLeaderRepository;

    @Autowired
    private CandleBackfillJobService candleBackfillJobService;

    @Autowired
    private BackgroundJobRepository backgroundJobRepository;

    @Test
    @Order(1)
    void apiRequiresAKeyAndLeavesHealthAndTheLoginCallbackOpen() throws Exception {
        mockMvc.perform(get("/api/v1/exchanges"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/exchanges").header("X-API-Key", "test-key"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/1/callback"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/health/market").header("X-API-Key", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kiteSession").value("absent"));
    }

    @Test
    void requestErrorsUseTheApiErrorBody() throws Exception {
        mockMvc.perform(get("/api/v1/candles").header("X-API-Key", "test-key").header("X-Request-Id", "trace-missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(RequestLoggingFilter.REQUEST_ID_HEADER, "trace-missing-param"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.requestId").value("trace-missing-param"))
                .andExpect(jsonPath("$.message").value("Required parameter 'instrumentId' is missing"));

        mockMvc.perform(get("/api/v1/candles").header("X-API-Key", "test-key").param("instrumentId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'instrumentId' has an invalid value"));

        mockMvc.perform(get("/api/v1/candles").header("X-API-Key", "test-key").param("instrumentId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Instrument not found: 99"));

        mockMvc.perform(get("/api/v1/missing").header("X-API-Key", "test-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No endpoint matched this request"));

        mockMvc.perform(post("/api/v1/candles/fetch").header("X-API-Key", "test-key").content("{").contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body could not be read"));
    }

    @Test
    @Order(2)
    void onlyOneInstanceHoldsClusterLeadership() {
        assertTrue(clusterLeaderService.claim("instance-a"));
        assertFalse(clusterLeaderService.claim("instance-b"));

        RuntimeLeader leader = runtimeLeaderRepository.findById(ClusterLeaderService.LOCK_NAME).orElseThrow();
        leader.setHeartbeatAt(Instant.now().minusSeconds(120));
        runtimeLeaderRepository.saveAndFlush(leader);

        assertTrue(clusterLeaderService.claim("instance-b"));
    }

    @Test
    @Order(3)
    void aSecondBackfillIsRejectedWhileTheFirstIsActive() {
        candleBackfillJobService.enqueue(1, 1);

        assertThrows(ConflictException.class, () -> candleBackfillJobService.enqueue(1, 1));
        backgroundJobRepository.deleteAll();
    }

    @Test
    @Order(4)
    void backfillRequestIsAcceptedWithoutWaitingForTheDownload() throws Exception {
        mockMvc.perform(post("/api/v1/candles/backfill")
                        .header("X-API-Key", "test-key")
                        .param("minuteDays", "1")
                        .param("dailyDays", "1"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }
}
