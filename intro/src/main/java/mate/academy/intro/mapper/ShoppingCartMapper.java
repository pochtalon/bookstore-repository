package mate.academy.intro.mapper;

import java.util.stream.Collectors;
import mate.academy.intro.config.MapperConfig;
import mate.academy.intro.dto.cart.CartItemDto;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;
import mate.academy.intro.model.CartItem;
import mate.academy.intro.model.ShoppingCart;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ShoppingCartMapper {

    ShoppingCartResponseDto toDto(ShoppingCart shoppingCart);

    @AfterMapping
    default void setCartItems(
            @MappingTarget ShoppingCartResponseDto shoppingCartResponseDto,
            ShoppingCart shoppingCart
    ) {
        CartItemMapper cartItemMapper = new CartItemMapper() {
            @Override
            public CartItemDto toDto(CartItem cartItem) {
                CartItemDto cartItemDto = new CartItemDto();
                cartItemDto.setId(cartItem.getId());
                cartItemDto.setBookId(cartItem.getBook().getId());
                cartItemDto.setBookTitle(cartItem.getBook().getTitle());
                cartItemDto.setQuantity(cartItem.getQuantity());
                return cartItemDto;
            }
        };
        shoppingCartResponseDto.setCartItems(shoppingCart.getCartItems().stream()
                .map(cartItemMapper::toDto)
                .collect(Collectors.toSet()));
    }

    @AfterMapping
    default void setUserId(
            @MappingTarget ShoppingCartResponseDto shoppingCartResponseDto,
            ShoppingCart shoppingCart
    ) {
        shoppingCartResponseDto.setUserId(shoppingCart.getUser().getId());
    }
}
