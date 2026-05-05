package com.example.hotel_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object koji detaljno prikazuje konzumaciju dodatne usluge.
 * * <p>Ovaj DTO se koristi za prikaz stavki na računu. Za razliku od modela,
 * on uključuje i naziv usluge, što omogućava korisniku (gostu ili recepcioneru)
 * da odmah vidi opis troška bez dodatnih upita prema bazi.</p>
 * * @author Tvoje Ime
 * @version 1.1
 */
public class ServiceUsageDTO {

    /** Jedinstveni identifikator zapisa o konzumaciji. */
    private Long id;

    /** Referenca na boravak (Stay) tokom kojeg je usluga konzumirana. */
    private Long stayId;

    /** Interni ID usluge iz kataloga. */
    private Long serviceId;

    /** * Čitljiv naziv usluge (npr. "Wellness paket").
     * Ovo polje se popunjava putem JOIN operacije ili u servisnom sloju.
     */
    private String serviceName;

    /** Količina konzumirane usluge (npr. 2 kafe, 3 noćenja za ljubimca). */
    private Integer quantity;

    /** Datum kada je usluga iskorištena. */
    private LocalDate usageDate;

    /** * Ukupna cijena za ovu stavku (quantity * unitPrice).
     * Koristi se BigDecimal radi finansijske preciznosti.
     */
    private BigDecimal totalPrice;

    /** Default konstruktor za potrebe Jackson serijalizacije. */
    public ServiceUsageDTO() {
    }

    /** * Konstruktor koji se najčešće koristi za prikaz na izvještajima i računima,
     * uključujući i naziv usluge radi bolje čitljivosti.
     */
    public ServiceUsageDTO(Long id, Long stayId, String serviceName, Integer quantity, LocalDate usageDate, BigDecimal totalPrice) {
        this.id = id;
        this.stayId = stayId;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.usageDate = usageDate;
        this.totalPrice = totalPrice;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStayId() { return stayId; }
    public void setStayId(Long stayId) { this.stayId = stayId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDate getUsageDate() { return usageDate; }
    public void setUsageDate(LocalDate usageDate) { this.usageDate = usageDate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}