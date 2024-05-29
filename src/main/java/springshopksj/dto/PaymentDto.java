package springshopksj.dto;

import lombok.Data;

@Data
public class PaymentDto {
    private String paymentMethod;
    private int amount;
}