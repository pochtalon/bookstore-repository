package mate.academy.intro.service;

import mate.academy.intro.dto.cart.CartItemAddRequestDto;
import mate.academy.intro.dto.cart.CartItemDto;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;

public interface CartService {
    ShoppingCartResponseDto getByUserId(Long userId);

    CartItemDto addBookToCart(Long userId, CartItemAddRequestDto requestDto);

    CartItemDto updateBookQuantity(Long userId, Long cartItemId, int quantity);

    void deleteItem(Long userId, Long cartItemId);
}
