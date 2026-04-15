package com.example.hotel_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Unique identifier of the discount")
    private Long id;
    @NotBlank(message = "Discount name is required")
    @Schema(description = "Name of the discount (e.g., Summer Sale, Early Bird)", example = "Summer Sale 2026")
    private String name;
    @NotNull(message = "Percentage is required")
    @Min(value = 0, message = "Percentage cannot be less than 0")
    @Max(value = 100, message = "Percentage cannot be greater than 100")
    @Schema(description = "Discount percentage (0.0 to 100.0)", example = "15.0")
    private Double percentage;
    @NotNull(message = "Start date is required")
    @Schema(description = "Date when the discount becomes active", example = "2026-06-01")
    private LocalDate startDate;
    @NotNull(message = "End date is required")
    @Schema(description = "Date when the discount expires", example = "2026-08-31")
    private LocalDate endDate;
    @Schema(description = "Additional details about the discount", example = "Available for all bookings during summer season.")
    private String description;
}
