package mate.academy.intro.service;

import mate.academy.intro.dto.cart.CartItemAddRequestDto;
import mate.academy.intro.dto.cart.CartItemDto;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.CartItemMapper;
import mate.academy.intro.mapper.ShoppingCartMapper;
import mate.academy.intro.model.Book;
import mate.academy.intro.model.CartItem;
import mate.academy.intro.model.ShoppingCart;
import mate.academy.intro.model.User;
import mate.academy.intro.repository.book.BookRepository;
import mate.academy.intro.repository.cartitem.CartItemRepository;
import mate.academy.intro.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.intro.service.impl.CartServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @InjectMocks
    CartServiceImpl cartService;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private BookRepository bookRepository;
    private static final Long USER_ID = 15L;
    private static final Long CART_ID = 10L;
    private static final Long ITEM_ID = 9L;
    private static final Long BOOK_ID = 5L;
    private static final int QUANTITY = 6;
    private static final String TITLE = "Necronomicon";


    @Test
    @DisplayName("Add book to cart")
    public void addBookToCart_CartIdAndRequestDto_ReturnCartItemDto() {
        ShoppingCartResponseDto expected = new ShoppingCartResponseDto()
                .setId(CART_ID)
                .setUserId(USER_ID)
                .setCartItems(new HashSet<>());
        ShoppingCart shoppingCart = createShoppingCart();

        when(shoppingCartRepository.findShoppingCartByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        ShoppingCartResponseDto actual = cartService.getByUserId(USER_ID);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Add book to cart, invalid user id")
    public void addBookToCart_InvalidUserId_ThrowException() {
        when(shoppingCartRepository.findShoppingCartByUserId(USER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> cartService.getByUserId(USER_ID));
        String expected = "Can't find shopping cart for user with id " + USER_ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Add book with valid dto")
    public void addBookToCart_ValidDto_ReturnCartItemDto() {
        CartItemAddRequestDto requestDto = createAddRequestDto();
        CartItem cartItem = new CartItem()
                .setQuantity(QUANTITY);
        Book book = new Book()
                .setId(BOOK_ID)
                .setTitle(TITLE);
        CartItemDto expected = createCartItemDto();
        ShoppingCart shoppingCart = createShoppingCart();

        when(cartItemMapper.toModel(requestDto)).thenReturn(cartItem);
        when(bookRepository.findById(requestDto.getBookId())).thenReturn(Optional.of(book));
        when(shoppingCartRepository.findShoppingCartByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem.setId(ITEM_ID));
        when(cartItemMapper.toDto(cartItem)).thenReturn(expected);

        CartItemDto actual = cartService.addBookToCart(USER_ID, requestDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Add book with invalid book id")
    public void addBookToCart_InvalidBookId_ThrowException() {
        CartItemAddRequestDto requestDto = createAddRequestDto();
        CartItem cartItem = new CartItem()
                .setQuantity(QUANTITY);
        ShoppingCart shoppingCart = createShoppingCart();

        when(cartItemMapper.toModel(requestDto)).thenReturn(cartItem);
        when(shoppingCartRepository.findShoppingCartByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(bookRepository.findById(requestDto.getBookId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> cartService.addBookToCart(USER_ID, requestDto));
        String expected = "Can't find book with id " + BOOK_ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update book quantity")
    public void updateBookQuantity_ItemIdAndQuantity_ReturnCartItemDto() {
        int quantity = 8;
        ShoppingCart shoppingCart = createShoppingCart();
        CartItem cartItem = new CartItem()
                .setId(ITEM_ID)
                .setShoppingCart(shoppingCart)
                .setQuantity(QUANTITY);
        CartItemDto expected = createCartItemDto();

        when(shoppingCartRepository.findShoppingCartByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem.setQuantity(quantity));
        when(cartItemMapper.toDto(cartItem)).thenReturn(expected);

        CartItemDto actual = cartService.updateBookQuantity(USER_ID, ITEM_ID, quantity);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update book quantity by invalid item id")
    public void updateBookQuantity_InvalidItemId_ThrowException() {
        ShoppingCart shoppingCart = createShoppingCart();

        when(shoppingCartRepository.findShoppingCartByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> cartService.updateBookQuantity(USER_ID, ITEM_ID, QUANTITY));
        String expected = "Can't find cart item with id " + ITEM_ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update book quantity, item doesn't belong to user's cart")
    public void updateBookQuantity_ItemDoesntBelongUser_ThrowException() {
        ShoppingCart userCart = createShoppingCart();
        ShoppingCart itemCart = new ShoppingCart()
                .setId(2L)
                .setUser(new User().setId(100L))
                .setCartItems(new HashSet<>());
        CartItem cartItem = new CartItem()
                .setId(ITEM_ID)
                .setShoppingCart(itemCart)
                .setQuantity(1);

        when(shoppingCartRepository.findShoppingCartByUserId(USER_ID)).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(cartItem));

        Exception exception = assertThrows(RuntimeException.class,
                () -> cartService.updateBookQuantity(USER_ID, ITEM_ID, QUANTITY));
        String expected = "Can't find cart item with id " + ITEM_ID + " in your cart";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    private ShoppingCart createShoppingCart() {
        return new ShoppingCart()
                .setId(CART_ID)
                .setUser(new User().setId(USER_ID))
                .setCartItems(new HashSet<>());
    }

    private CartItemAddRequestDto createAddRequestDto() {
        return new CartItemAddRequestDto()
                .setBookId(BOOK_ID)
                .setQuantity(QUANTITY);
    }

    private CartItemDto createCartItemDto() {
        return new CartItemDto()
                .setId(ITEM_ID)
                .setBookId(BOOK_ID)
                .setBookTitle(TITLE)
                .setQuantity(QUANTITY);
    }
}
