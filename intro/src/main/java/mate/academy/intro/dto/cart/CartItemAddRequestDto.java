package mate.academy.intro.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemAddRequestDto {
    @NotNull
    private Long bookId;
    @Min(0)
    private int quantity;
}
