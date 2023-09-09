package mate.academy.intro.service;

import mate.academy.intro.dto.cart.CartItemAddRequestDto;
import mate.academy.intro.dto.cart.CartItemDto;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;

public interface CartService {
    ShoppingCartResponseDto getByUserId(Long id);

    CartItemDto addBookToCart(Long id, CartItemAddRequestDto requestDto);

    CartItemDto updateBookQuantity(Long id, Long cartItemId, int quantity);

    void deleteItem(Long id, Long cartItemId);
}
