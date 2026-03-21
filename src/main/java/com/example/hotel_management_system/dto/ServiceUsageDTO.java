package com.example.hotel_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServiceUsageDTO {

    private Long id;
    private Long stayId;
    private Long serviceId;
    private Integer quantity;
    private LocalDate usageDate;
    private BigDecimal totalPrice;

    public ServiceUsageDTO() {
    }

    public ServiceUsageDTO(Long id, Long stayId, Long serviceId, Integer quantity, LocalDate usageDate, BigDecimal totalPrice) {
        this.id = id;
        this.stayId = stayId;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.usageDate = usageDate;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDate usageDate) {
        this.usageDate = usageDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}