package mate.academy.intro.dto.cart;

import lombok.Data;

@Data
public class CartItemAddRequestDto {
    private Long bookId;
    private int quantity;
}
