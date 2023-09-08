package mate.academy.intro.service;

import mate.academy.intro.dto.cart.ShoppingCartResponseDto;

public interface CartService {
    ShoppingCartResponseDto getByUserId(Long id);
}
