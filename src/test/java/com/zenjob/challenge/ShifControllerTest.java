package com.zenjob.challenge;

import com.zenjob.challenge.controller.ShiftController;
import com.zenjob.challenge.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShifControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    private UUID shiftId;

    @BeforeEach
    public void setup() {
        shiftId = UUID.randomUUID();
    }

    @Test
    public void testCancelShift() throws Exception {
        mockMvc.perform(patch("/shift/" + shiftId + "/cancel"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCancelAndReplaceShift() throws Exception {
       UUID talentId = UUID.randomUUID();
    mockMvc.perform(patch("/shift/cancelAndReplaceForTalent/" + talentId))
            .andExpect(status().isNoContent());
    }
}
