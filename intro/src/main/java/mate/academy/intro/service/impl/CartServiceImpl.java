package mate.academy.intro.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.CartItemMapper;
import mate.academy.intro.mapper.ShoppingCartMapper;
import mate.academy.intro.model.CartItem;
import mate.academy.intro.model.ShoppingCart;
import mate.academy.intro.repository.shoppingCart.ShoppingCartRepository;
import mate.academy.intro.service.CartService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    private final ShoppingCartRepository shoppingCartRepository;
//    private final CartItemRepository cartItemRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    public ShoppingCartResponseDto getByUserId(Long id) {
        ShoppingCart shoppingCart =
                shoppingCartRepository.findShoppingCartByUserId(id).orElseThrow(
                        () -> new EntityNotFoundException(
                                "Can't find shopping cart for user with id " + id)
        );
        return shoppingCartMapper.toDto(shoppingCart);
    }
}
