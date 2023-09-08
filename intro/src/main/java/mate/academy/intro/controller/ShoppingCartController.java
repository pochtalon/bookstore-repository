package mate.academy.intro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;
import mate.academy.intro.model.User;
import mate.academy.intro.service.CartService;
import mate.academy.intro.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart management", description = "Endpoints for managing shopping carts")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/cart")
public class ShoppingCartController {
    private final CartService cartService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    @Operation(summary = "Get all items in the cart",
            description = "Get a list of all items from user's shopping cart")
    public ShoppingCartResponseDto getShoppingCartForUser(Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return cartService.getByUserId(user.getId());
    }
}
