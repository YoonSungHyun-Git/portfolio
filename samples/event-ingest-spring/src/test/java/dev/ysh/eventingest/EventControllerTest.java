package dev.ysh.eventingest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void 정상_이벤트는_201로_저장된다() throws Exception {
        mvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceCode":"DAS","channelNumber":1,"distanceM":1200.0,
                                 "value":0.82,"eventTime":"2026-01-15T09:00:00+09:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.channelNumber").value(1));
    }

    @Test
    void 잘못된_값은_필드별_사유와_함께_400() throws Exception {
        mvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceCode":"das","channelNumber":9,"distanceM":-1,
                                 "eventTime":"2999-01-01T00:00:00+09:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.deviceCode").exists())
                .andExpect(jsonPath("$.errors.channelNumber").exists())
                .andExpect(jsonPath("$.errors.distanceM").exists())
                .andExpect(jsonPath("$.errors.value").exists())
                .andExpect(jsonPath("$.errors.eventTime").exists());
    }

    @Test
    void 읽을_수_없는_본문은_400() throws Exception {
        mvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }
}
