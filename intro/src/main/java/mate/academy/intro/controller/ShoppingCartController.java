package mate.academy.intro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.cart.CartItemAddRequestDto;
import mate.academy.intro.dto.cart.CartItemDto;
import mate.academy.intro.dto.cart.CartItemQuantityUpdateRequestDto;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;
import mate.academy.intro.model.User;
import mate.academy.intro.service.CartService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public ShoppingCartResponseDto getShoppingCartForUser(
            Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return cartService.getByUserId(user.getId());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    @Operation(summary = "Add book to the cart",
            description = "Add book to user's shopping cart")
    public CartItemDto addBookToCart(Authentication authentication,
                                     @RequestBody @Valid CartItemAddRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return cartService.addBookToCart(user.getId(), requestDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/cart-items/{cartItemId}")
    @Operation(summary = "Update book quantity",
            description = "Update book quantity in YOUR shopping cart")
    public CartItemDto updateBookQuantity(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @RequestBody @Valid CartItemQuantityUpdateRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return cartService.updateBookQuantity(user.getId(), cartItemId, requestDto.getQuantity());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/cart-items/{cartItemId}")
    @Operation(summary = "Delete book",
            description = "Delete book from YOUR shopping cart")
    public void deleteItemFromCart(Authentication authentication, @PathVariable Long cartItemId) {
        User user = (User) authentication.getPrincipal();
        cartService.deleteItem(user.getId(), cartItemId);
    }
}
