# Hotel Guest Check-Out and Invoice Generation API

## Overview
This document describes the guest check-out and invoice generation feature for the Hotel Management System. The check-out process is a transactional operation that validates the reservation, calculates costs, generates an invoice, and updates related statuses.

---

## REST API Endpoint

### Check-Out Endpoint

```
POST /api/reservations/{id}/checkout
```

**Description:** Process guest check-out, calculate invoice, apply discounts, and update reservation/room statuses.

**Path Parameters:**
- `id` (Long, required): The reservation ID

**Request Body:**
```json
{
  "reservationId": 1,
  "actualCheckOutTime": "2024-04-10T14:30:00",
  "performedByUserId": 5
}
```

**Request Field Descriptions:**
- `reservationId`: (Optional) The reservation ID. If provided in path, this field is overridden by the path parameter.
- `actualCheckOutTime`: (Optional) The actual check-out timestamp in ISO 8601 format (e.g., "2024-04-10T14:30:00"). If not provided, the current system time is used.
- `performedByUserId`: (Optional) The ID of the user (receptionist) performing the check-out. Used for audit logging.

---

## Success Response

**HTTP Status:** 200 OK

**Response Body:**
```json
{
  "reservationId": 1,
  "guestId": 10,
  "roomId": 5,
  "roomNumber": "101",
  "checkInTime": "2024-04-08T15:00:00",
  "checkOutTime": "2024-04-10T14:30:00",
  "numberOfNights": 2,
  "roomTypeName": "Deluxe Double Room",
  "pricePerNight": 150.00,
  "invoiceId": 100,
  "accommodationCost": 300.00,
  "additionalServicesCost": 75.50,
  "subtotal": 375.50,
  "discountId": 3,
  "discountName": "Spring Campaign",
  "discountPercentage": 10.00,
  "discountAmount": 37.55,
  "finalAmount": 337.95,
  "invoiceStatus": "UNPAID",
  "roomStatus": "AVAILABLE",
  "reservationStatus": "COMPLETED"
}
```

**Response Field Descriptions:**
- `reservationId`: The ID of the reservation
- `guestId`: The ID of the guest
- `roomId`: The ID of the room
- `roomNumber`: The room number (e.g., "101", "205")
- `checkInTime`: Actual check-in timestamp
- `checkOutTime`: Actual check-out timestamp
- `numberOfNights`: Calculated number of nights stayed
- `roomTypeName`: Name of the room type (e.g., "Deluxe Double Room")
- `pricePerNight`: Price per night for the room type
- `invoiceId`: The ID of the generated invoice
- `accommodationCost`: Total accommodation cost (nights × price per night)
- `additionalServicesCost`: Total cost of all used additional services
- `subtotal`: accommodationCost + additionalServicesCost
- `discountId`: The ID of the applied discount (null if no discount)
- `discountName`: The name of the applied discount
- `discountPercentage`: The discount percentage
- `discountAmount`: The calculated discount amount in currency
- `finalAmount`: Final amount to be paid (subtotal - discount)
- `invoiceStatus`: Status of the generated invoice (always "UNPAID")
- `roomStatus`: Updated room status (always "AVAILABLE")
- `reservationStatus`: Updated reservation status (always "COMPLETED")

---

## Error Responses

### 1. Reservation Not Found

**HTTP Status:** 404 Not Found

```json
{
  "errorCode": "RESERVATION_NOT_FOUND",
  "message": "Reservation not found with id: 999",
  "timestamp": 1712769000000
}
```

**Cause:** The reservation with the provided ID does not exist in the database.

---

### 2. Invalid Reservation Status

**HTTP Status:** 400 Bad Request

```json
{
  "errorCode": "INVALID_RESERVATION_STATUS",
  "message": "Only CONFIRMED reservations can be checked out. Current status: PENDING",
  "timestamp": 1712769000000
}
```

**Cause:** The reservation is not in CONFIRMED status. Reservations must be checked in (CONFIRMED status) before they can be checked out.

**Valid statuses for check-out:**
- `CONFIRMED` - Reservation is checked in and active

**Statuses that prevent check-out:**
- `PENDING` - Reservation not yet confirmed
- `CANCELLED` - Reservation was cancelled
- `COMPLETED` - Reservation already completed

---

### 3. Room Not Found

**HTTP Status:** 404 Not Found

```json
{
  "errorCode": "ROOM_NOT_FOUND",
  "message": "Room not found with id: 50",
  "timestamp": 1712769000000
}
```

**Cause:** The room associated with the reservation does not exist in the database.

---

### 4. Invoice Already Exists

**HTTP Status:** 409 Conflict

```json
{
  "errorCode": "INVOICE_ALREADY_EXISTS",
  "message": "An invoice already exists for reservation id: 1",
  "timestamp": 1712769000000
}
```

**Cause:** An invoice has already been generated for this reservation's stay. Prevent duplicate invoicing.

---

### 5. Database Error

**HTTP Status:** 500 Internal Server Error

```json
{
  "errorCode": "DATABASE_ERROR",
  "message": "An error occurred during check-out processing",
  "timestamp": 1712769000000
}
```

**Cause:** A SQL error occurred during the check-out process. Check server logs for details.

---

### 6. Internal Server Error

**HTTP Status:** 500 Internal Server Error

```json
{
  "errorCode": "INTERNAL_ERROR",
  "message": "Error message details",
  "timestamp": 1712769000000
}
```

**Cause:** An unexpected error occurred. Check server logs for details.

---

## Example Use Cases

### Use Case 1: Standard Check-Out with Discount Applied

**Request:**
```bash
curl -X POST http://localhost:8080/api/reservations/1/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "actualCheckOutTime": "2024-04-10T14:30:00",
    "performedByUserId": 5
  }'
```

**Scenario:**
- Guest checked in on 2024-04-08 at 15:00
- Room type: Deluxe Double Room, $150/night
- Additional services used: $75.50
- Active discount: Spring Campaign 10%

**Response:**
```json
{
  "reservationId": 1,
  "guestId": 10,
  "roomId": 5,
  "roomNumber": "101",
  "checkInTime": "2024-04-08T15:00:00",
  "checkOutTime": "2024-04-10T14:30:00",
  "numberOfNights": 2,
  "roomTypeName": "Deluxe Double Room",
  "pricePerNight": 150.00,
  "invoiceId": 100,
  "accommodationCost": 300.00,
  "additionalServicesCost": 75.50,
  "subtotal": 375.50,
  "discountId": 3,
  "discountName": "Spring Campaign",
  "discountPercentage": 10.00,
  "discountAmount": 37.55,
  "finalAmount": 337.95,
  "invoiceStatus": "UNPAID",
  "roomStatus": "AVAILABLE",
  "reservationStatus": "COMPLETED"
}
```

---

### Use Case 2: Check-Out Without Discount

**Request:**
```bash
curl -X POST http://localhost:8080/api/reservations/2/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "performedByUserId": 5
  }'
```

**Scenario:**
- Guest checked in on 2024-04-09 at 10:00
- Checking out at current time (system will use current timestamp)
- Room type: Standard Room, $100/night
- No additional services used
- No active discount available

**Response:**
```json
{
  "reservationId": 2,
  "guestId": 11,
  "roomId": 3,
  "roomNumber": "205",
  "checkInTime": "2024-04-09T10:00:00",
  "checkOutTime": "2024-04-10T11:45:00",
  "numberOfNights": 1,
  "roomTypeName": "Standard Room",
  "pricePerNight": 100.00,
  "invoiceId": 101,
  "accommodationCost": 100.00,
  "additionalServicesCost": 0.00,
  "subtotal": 100.00,
  "discountId": null,
  "discountName": null,
  "discountPercentage": null,
  "discountAmount": 0.00,
  "finalAmount": 100.00,
  "invoiceStatus": "UNPAID",
  "roomStatus": "AVAILABLE",
  "reservationStatus": "COMPLETED"
}
```

---

### Use Case 3: Check-Out Error - Invalid Status

**Request:**
```bash
curl -X POST http://localhost:8080/api/reservations/3/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "performedByUserId": 5
  }'
```

**Scenario:**
- Reservation ID 3 is in PENDING status (not checked in yet)

**Response (400 Bad Request):**
```json
{
  "errorCode": "INVALID_RESERVATION_STATUS",
  "message": "Only CONFIRMED reservations can be checked out. Current status: PENDING",
  "timestamp": 1712769000000
}
```

---

## Business Logic Details

### Invoice Calculation

The invoice is calculated using the following formula:

```
Accommodation Cost = Number of Nights × Price Per Night
Additional Services Cost = Sum of all service usage costs for the stay
Subtotal = Accommodation Cost + Additional Services Cost

If Active Discount exists:
  Discount Amount = Subtotal × (Discount Percentage / 100)
  Final Amount = Subtotal - Discount Amount
Else:
  Discount Amount = 0
  Final Amount = Subtotal

Final Amount = Max(0, Final Amount) // Ensure never negative
```

### Number of Nights Calculation

The number of nights is calculated as the number of complete days between check-in and check-out:

```
Number of Nights = Days between Check-In Date and Check-Out Date
If Number of Nights < 1:
  Number of Nights = 1 (minimum 1 night)
```

### Discount Application

- Discounts are applied automatically if an active discount exists (current date falls within discount's start and end dates)
- The discount with the highest percentage is selected if multiple exist
- Discount is applied as a percentage of the subtotal
- A discount amount can never exceed the subtotal

### Transaction Semantics

The check-out operation is transactional:
- All database changes are committed together
- If any step fails, the entire operation is rolled back
- No partial updates occur

### Status Updates

On successful check-out:
- **Invoice Status:** UNPAID (initial status, awaiting payment)
- **Room Status:** AVAILABLE (room becomes available for future reservations)
- **Reservation Status:** COMPLETED (reservation is closed)

---

## Data Model Requirements

### Required Entities

1. **Reservation** (NBP_RESERVATION)
   - ID, GUEST_ID, ROOM_ID, CHECK_IN_DATE, CHECK_OUT_DATE, STATUS

2. **Room** (NBP_ROOM)
   - ID, ROOM_NUMBER, ROOM_TYPE_ID, HOTEL_ID, STATUS

3. **RoomType** (NBP_ROOM_TYPE)
   - ID, NAME, PRICE_PER_NIGHT

4. **Stay** (NBP_STAY)
   - ID, RESERVATION_ID, CHECK_IN_TIME, CHECK_OUT_TIME, ACTUAL_TOTAL_PRICE

5. **ServiceUsage** (NBP_SERVICE_USAGE)
   - ID, STAY_ID, SERVICE_ID, QUANTITY, TOTAL_PRICE

6. **Invoice** (NBP_INVOICE)
   - ID, STAY_ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT

7. **Discount** (NBP_DISCOUNT)
   - ID, NAME, PERCENTAGE, START_DATE, END_DATE

---

## Implementation Notes

### Exception Handling

The service properly handles and throws custom exceptions:
- `ReservationNotFoundException` - Reservation doesn't exist
- `InvalidReservationStatusException` - Reservation not in CONFIRMED status
- `RoomNotFoundException` - Room doesn't exist
- `InvoiceAlreadyExistsException` - Invoice already created for this stay

### BigDecimal Usage

All monetary calculations use `java.math.BigDecimal` for precision:
- HALF_UP rounding mode for financial calculations
- Scale of 2 decimal places

### Null Safety

Comprehensive null checks are performed for:
- Reservation lookup
- Room lookup
- Room type lookup
- Active discount lookup
- Service usage list

---

## Testing Recommendations

### Test Cases

1. **Happy Path:** Standard check-out with discount
2. **No Discount:** Check-out when no active discount exists
3. **No Services:** Check-out with no additional services
4. **Multiple Services:** Check-out with multiple service usages
5. **Error: Not Found:** Non-existent reservation
6. **Error: Invalid Status:** PENDING, CANCELLED, or COMPLETED reservation
7. **Error: Duplicate Invoice:** Invoice already exists
8. **Edge Case:** Same-day check-in/check-out (1 night minimum)

---

## Production Checklist

- [x] Transactional operation with rollback on failure
- [x] Proper exception handling with meaningful error messages
- [x] BigDecimal for financial calculations
- [x] Input validation (reservation status, existence checks)
- [x] Null safety checks
- [x] Logging for audit trail (via DatabaseLogger)
- [x] HTTP status codes appropriate to error types
- [x] Clear API documentation
- [ ] Database backups before deployment
- [ ] Integration tests covering all error cases
- [ ] Load testing for concurrent check-outs

