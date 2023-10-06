package mate.academy.intro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import mate.academy.intro.dto.cart.CartItemAddRequestDto;
import mate.academy.intro.dto.cart.CartItemDto;
import mate.academy.intro.dto.cart.CartItemQuantityUpdateRequestDto;
import mate.academy.intro.dto.cart.ShoppingCartResponseDto;
import mate.academy.intro.security.JwtUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;
    private static final String EMAIL = "handsome_bob@mail.com";
    private static final List<CartItemDto> cartItemDtoList = new ArrayList<>();

    private static void initCartItemDtoList() {
        cartItemDtoList.add(new CartItemDto()
                .setId(1L)
                .setBookId(1L)
                .setBookTitle("Call of Cthulhu")
                .setQuantity(4));
        cartItemDtoList.add(new CartItemDto()
                .setId(2L)
                .setBookId(2L)
                .setBookTitle("The Raven")
                .setQuantity(5));
        cartItemDtoList.add(new CartItemDto()
                .setId(3L)
                .setBookId(3L)
                .setBookTitle("The Name of the Rose")
                .setQuantity(6));
    }

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/add-three-books-to-db.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/users/add-user.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/carts&cartitems/add-cart-and-three-cartitems.sql")
            );
        }
        initCartItemDtoList();
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/carts&cartitems/clear-cart_items-and-shopping_carts-tables.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/users/clear-users-table.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/clear-books-table.sql")
            );
        }
    }

    @Test
    @DisplayName("Get shopping cart for user by valid token")
    public void getShoppingCartForUser_ValidToken_ReturnValidDto() throws Exception {
        MvcResult result = mockMvc.perform(get("/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ShoppingCartResponseDto expected = new ShoppingCartResponseDto()
                .setId(1L)
                .setUserId(1L)
                .setCartItems(new HashSet<>(cartItemDtoList));
        ShoppingCartResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ShoppingCartResponseDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Sql(
            scripts = "classpath:database/books/add-leviathan-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/carts&cartitems/delete-leviathan-cartItem.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/books/delete-leviathan-book.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Add book to cart")
    public void addBookToCart_ValidCartItemAddRequestDto_ReturnCartItemDto() throws Exception {
        Long id = 4L;
        int quantity = 7;
        CartItemAddRequestDto requestDto = new CartItemAddRequestDto()
                .setBookId(id)
                .setQuantity(quantity);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        CartItemDto expected = new CartItemDto()
                .setBookId(id)
                .setBookTitle("Leviathan")
                .setQuantity(quantity);

        MvcResult result = mockMvc.perform(post("/cart")
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CartItemDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CartItemDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @Sql(
            scripts = "classpath:database/books/add-leviathan-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/carts&cartitems/delete-leviathan-cartItem.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/books/delete-leviathan-book.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Add book to cart with invalid data")
    public void addBookToCart_InvalidCartItemAddRequestDto_ReturnCartItemDto() throws Exception {
        Long id = 4L;
        int quantity = -6;
        CartItemAddRequestDto requestDto = new CartItemAddRequestDto()
                .setBookId(id)
                .setQuantity(quantity);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/cart")
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @Sql(
            scripts = "classpath:database/carts&cartitems/clear-cart_items-and-shopping_carts-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/carts&cartitems/add-cart-and-three-cartitems.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Update item quantity it the cart")
    public void updateBookQuantity_IdAndQuantity_ReturnCartItemDto() throws Exception {
        String token = "Bearer " + jwtUtil.generateToken(EMAIL);
        int expect1 = 10;
        int expect2 = 20;
        int expect3 = 30;
        String jsonRequest1 = objectMapper.writeValueAsString(new CartItemQuantityUpdateRequestDto().setQuantity(expect1));
        String jsonRequest2 = objectMapper.writeValueAsString(new CartItemQuantityUpdateRequestDto().setQuantity(expect2));
        String jsonRequest3 = objectMapper.writeValueAsString(new CartItemQuantityUpdateRequestDto().setQuantity(expect3));

        MvcResult result1 = mockMvc.perform(put("/cart/cart-items/" + 1L)
                        .content(jsonRequest1)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CartItemDto actual1 = objectMapper.readValue(result1.getResponse().getContentAsByteArray(), CartItemDto.class);
        Assertions.assertEquals(expect1, actual1.getQuantity());

        MvcResult result2 = mockMvc.perform(put("/cart/cart-items/" + 2L)
                        .content(jsonRequest2)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CartItemDto actual2 = objectMapper.readValue(result2.getResponse().getContentAsByteArray(), CartItemDto.class);
        Assertions.assertEquals(expect2, actual2.getQuantity());

        MvcResult result3 = mockMvc.perform(put("/cart/cart-items/" + 3L)
                        .content(jsonRequest3)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CartItemDto actual3 = objectMapper.readValue(result3.getResponse().getContentAsByteArray(), CartItemDto.class);
        Assertions.assertEquals(expect3, actual3.getQuantity());
    }

    @Test
    @DisplayName("Update item quantity it the cart")
    public void updateBookQuantity_IdAndInvalidQuantity_ReturnCartItemDto() throws Exception {
        Long id1 = 1L;
        int quantity1 = -10;
        String jsonRequest1 = objectMapper.writeValueAsString(new CartItemQuantityUpdateRequestDto().setQuantity(quantity1));

        MvcResult result1 = mockMvc.perform(put("/cart/cart-items/" + id1)
                        .content(jsonRequest1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @Sql(
            scripts = "classpath:database/carts&cartitems/clear-cart_items-and-shopping_carts-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/carts&cartitems/add-cart-and-three-cartitems.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Delete item from cart")
    public void deleteItemFromCart_ValidId_NoContent() throws Exception {
        String token = "Bearer " + jwtUtil.generateToken(EMAIL);
        MvcResult resultBefore = mockMvc.perform(get("/cart")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ShoppingCartResponseDto before = objectMapper.readValue(resultBefore.getResponse().getContentAsByteArray(), ShoppingCartResponseDto.class);

        MvcResult result1 = mockMvc.perform(delete("/cart/cart-items/" + 1L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
        MvcResult result2 = mockMvc.perform(delete("/cart/cart-items/" + 2L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
        MvcResult result3 = mockMvc.perform(delete("/cart/cart-items/" + 3L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        MvcResult  resultAfter = mockMvc.perform(get("/cart")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ShoppingCartResponseDto after = objectMapper.readValue(resultAfter.getResponse().getContentAsByteArray(), ShoppingCartResponseDto.class);
        Assertions.assertEquals(before.getCartItems().size() - 3, after.getCartItems().size());
    }
}