package mate.academy.intro.dto.cart;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ShoppingCartResponseDto {
    private Long id;
    private Long userId;
    private Set<CartItemDto> cartItems = new HashSet<>();
}
