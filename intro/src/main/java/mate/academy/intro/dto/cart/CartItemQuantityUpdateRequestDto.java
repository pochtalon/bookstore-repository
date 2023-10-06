package mate.academy.intro.dto.cart;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CartItemQuantityUpdateRequestDto {
    @Min(0)
    private int quantity;
}
