package com.example.hotel_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceDTO {

    private Long id;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private String status;
    private Long stayId;

    public InvoiceDTO() {
    }

    public InvoiceDTO(Long id, LocalDate issueDate, BigDecimal totalAmount, String status, Long stayId) {
        this.id = id;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.stayId = stayId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }
}