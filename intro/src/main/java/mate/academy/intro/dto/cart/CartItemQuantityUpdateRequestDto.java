package mate.academy.intro.dto.cart;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CartItemQuantityUpdateRequestDto {
    @Min(0)
    private int quantity;
}
