package mate.academy.intro.service;

import mate.academy.intro.mapper.CartItemMapper;
import mate.academy.intro.mapper.ShoppingCartMapper;
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

    @Test
    @DisplayName("Find shopping cart by user id")
    public void addBookToCart_CartIdAndRequestDto_ReturnCartItemDto() {

    }
}