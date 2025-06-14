package dev.visitingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.visitingservice.dto.VisitCreateRequest;
import dev.visitingservice.model.Status;
import dev.visitingservice.model.Visit;
import dev.visitingservice.service.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitController.class)
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VisitService visitService;

    @Autowired
    private ObjectMapper objectMapper;

    private Visit sampleVisit;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public VisitService visitService() {
            return Mockito.mock(VisitService.class);
        }
    }

    @BeforeEach
    void setUp() {
        sampleVisit = new Visit();
        sampleVisit.setId(UUID.randomUUID());
        sampleVisit.setPropertyId(UUID.randomUUID());
        sampleVisit.setVisitorId(UUID.randomUUID());
        sampleVisit.setLandlordId(UUID.randomUUID());
        sampleVisit.setScheduledAt(OffsetDateTime.now().plusDays(1));
        sampleVisit.setDurationMinutes(60);
        sampleVisit.setStatus(Status.PENDING);

        OffsetDateTime now = OffsetDateTime.now();
        sampleVisit.setCreatedAt(now);
        sampleVisit.setUpdatedAt(now);
    }

    @Test
    void requestVisit_ReturnsVisitResponse() throws Exception {
        Mockito.when(visitService.requestVisit(any(Visit.class))).thenReturn(sampleVisit);

        VisitCreateRequest req = new VisitCreateRequest();
        req.setPropertyId(sampleVisit.getPropertyId());
        req.setVisitorId(sampleVisit.getVisitorId());
        req.setLandlordId(sampleVisit.getLandlordId());
        req.setScheduledAt(sampleVisit.getScheduledAt().atZoneSameInstant(java.time.ZoneOffset.UTC));
        req.setNotes("Test note");
        req.setSlotId(UUID.randomUUID());

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleVisit.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void updateStatus_ReturnsUpdatedVisit() throws Exception {
        UUID id = sampleVisit.getId();
        sampleVisit.setStatus(Status.APPROVED);
        Mockito.when(visitService.updateVisitStatus(Mockito.eq(id), Mockito.eq(Status.APPROVED))).thenReturn(sampleVisit);

        mockMvc.perform(put("/api/visits/" + id + "/status")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}