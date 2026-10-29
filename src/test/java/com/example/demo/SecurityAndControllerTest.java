package com.example.demo;

import com.example.demo.controller.VehicleRestController;
import com.example.demo.dto.VehicleDto;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SecurityAndControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleRestController vehicleRestController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Trường hợp 14a: Kiểm tra tính hợp lệ dữ liệu đầu vào (Validation)
     * Biển số rỗng -> Trả về HTTP 400 Bad Request
     */
    @Test
    @DisplayName("14a. Kiểm tra dữ liệu đầu vào: Biển số rỗng trả về HTTP 400 Bad Request")
    void testValidation_EmptyLicensePlate_ReturnsBadRequest() throws Exception {
        VehicleDto invalidDto = new VehicleDto();
        invalidDto.setBienSoXe(""); // Biển số rỗng vi phạm @NotBlank
        invalidDto.setLoaiXeId(1L);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    /**
     * Trường hợp 14b: Xử lý lỗi không tìm thấy tài nguyên (Resource Not Found)
     * Tra cứu phương tiện không tồn tại -> Trả về HTTP 404 NOT_FOUND
     */
    @Test
    @DisplayName("14b. Xử lý lỗi nghiệp vụ: Phương tiện không tồn tại trả về HTTP 404 NOT_FOUND")
    void testResourceNotFound_Vehicle_ReturnsNotFound() throws Exception {
        when(vehicleService.getVehicleById(99999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy phương tiện với ID: 99999"));

        mockMvc.perform(get("/api/vehicles/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy phương tiện với ID: 99999"));
    }
}
