package com.example.hotel_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Unique identifier of the payment")
    private Long id;
    @NotNull(message = "Payment date is required")
    @Schema(description = "Date and time when the payment was made", example = "2026-04-12T15:00:00")
    private LocalDateTime paymentDate;
    @Positive(message = "Amount must be greater than zero")
    @Schema(description = "The amount of money paid", example = "150.50")
    private Double amount;
    @NotBlank(message = "Payment method is required")
    @Schema(
            description = "The method used for payment",
            allowableValues = {"Cash", "Debit Card", "Credit Card", "Bank transfer"},
            example = "Cash"
    )
    private String paymentMethod;
    @NotNull(message = "Invoice ID is required")
    @Schema(description = "The ID of the associated invoice", example = "1")
    private Long invoiceId;
}
