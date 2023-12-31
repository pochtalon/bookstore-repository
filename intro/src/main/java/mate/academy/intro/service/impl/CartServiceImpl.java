package mate.academy.intro.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.cart.CartItemAddRequestDto;
import mate.academy.intro.dto.cart.CartItemDto;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.CartItemMapper;
import mate.academy.intro.mapper.ShoppingCartMapper;
import mate.academy.intro.model.CartItem;
import mate.academy.intro.model.ShoppingCart;
import mate.academy.intro.repository.book.BookRepository;
import mate.academy.intro.repository.cartitem.CartItemRepository;
import mate.academy.intro.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.intro.service.CartService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemMapper cartItemMapper;
    private final BookRepository bookRepository;

    @Override
    public ShoppingCartResponseDto getByUserId(Long userId) {
        ShoppingCart shoppingCart = findCartByUserId(userId);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public CartItemDto addBookToCart(Long userId, CartItemAddRequestDto requestDto) {
        ShoppingCart shoppingCart = findCartByUserId(userId);
        CartItem cartItem = cartItemMapper.toModel(requestDto);
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setBook(bookRepository.findById(requestDto.getBookId()).orElseThrow(() ->
                new EntityNotFoundException("Can't find book with id " + requestDto.getBookId())));
        cartItem.setId(cartItemRepository.save(cartItem).getId());
        return cartItemMapper.toDto(cartItem);
    }

    @Override
    public CartItemDto updateBookQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem cartItem = getItemFromCart(userId, cartItemId);
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return cartItemMapper.toDto(cartItem);
    }

    @Override
    public void deleteItem(Long userId, Long cartItemId) {
        CartItem cartItem = getItemFromCart(userId, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    private CartItem getItemFromCart(Long id, Long cartItemId) {
        ShoppingCart shoppingCart = findCartByUserId(id);
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() ->
                new EntityNotFoundException("Can't find cart item with id " + cartItemId));
        if (shoppingCart.equals(cartItem.getShoppingCart())) {
            return cartItem;
        }
        throw new RuntimeException("Can't find cart item with id " + cartItemId + " in your cart");
    }

    private ShoppingCart findCartByUserId(Long id) {
        return shoppingCartRepository.findShoppingCartByUserId(id).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find shopping cart for user with id " + id));
    }
}
