package mate.academy.intro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import lombok.SneakyThrows;
import mate.academy.intro.dto.order.AddressRequestDto;
import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.dto.order.OrderItemDto;
import mate.academy.intro.dto.order.StatusRequestDto;
import mate.academy.intro.model.Order;
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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;
    private static final String EMAIL = "handsome_bob@mail.com";
    private static final String ADMIN_EMAIL = "admin@mail.com";
    private static final List<OrderItemDto> ORDER_ITEM_DTOS = new ArrayList<>();
    private static final List<OrderDto> ORDER_DTO_LIST = new ArrayList<>();
    private static final Long ORDER_ID = 1L;
    private static final Long ITEM_ID = 2L;

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
                    new ClassPathResource("database/users/add-default-user-and-admin.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/carts&cartitems/add-cart-and-three-cartitems.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/orders&orderItems/add-two-orders-with-items.sql")
            );
        }
        initOrderItemDtoList();
        initOrderDtoList();
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
                    new ClassPathResource("database/orders&orderItems/clear-orders-and-order_items-tables.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/carts&cartitems/clear-cart_items-and-shopping_carts-tables.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/clear-books-table.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/users/clear-users-table.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/orders&orderItems/clear-orders-and-order_items-tables.sql")
            );
        }
    }

    @Test
    @Sql(
            scripts = "classpath:database/orders&orderItems/clear-orders-and-order_items-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/orders&orderItems/add-two-orders-with-items.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create order")
    public void createOrder_AddressRequestDto_() throws Exception {
        AddressRequestDto requestDto = new AddressRequestDto().setShippingAddress("Address");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        OrderDto expected = new OrderDto()
                .setOrderItems(new HashSet<>(ORDER_ITEM_DTOS))
                .setTotal(new BigDecimal("2881.7"))
                .setStatus("PENDING");

        MvcResult result = mockMvc.perform(post("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        OrderDto actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrderDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id", "orderDate");
    }

    @Test
    @DisplayName("Get all orders for user")
    public void getAllOrders_Token_ListOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<OrderDto> actual = List.of(objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto[].class));
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(ORDER_DTO_LIST, actual);
    }

    @Test
    @Sql(
            scripts = "classpath:database/orders&orderItems/clear-orders-and-order_items-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/orders&orderItems/add-two-orders-with-items.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Update order status")
    public void updateOrderStatus_OrderIdAndStatusRequest_ReturnOrderDto() throws Exception {
        StatusRequestDto requestDto = new StatusRequestDto()
                .setStatus(Order.Status.DELIVERED);
        String expected = "DELIVERED";
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(patch("/orders/" + ORDER_ID)
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(ADMIN_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        OrderDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertEquals(expected, actual.getStatus());
    }

    @Test
    @DisplayName("Get all items from order ")
    public void getAllItemsFromOrder_OrderId_ReturnOrderItemDtosList() throws Exception {
        Set<OrderItemDto> expected = ORDER_DTO_LIST.get(0).getOrderItems();

        MvcResult resultUser = mockMvc.perform(get("/orders/" + ORDER_ID + "/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Set<OrderItemDto> actualUser = Set.of(objectMapper.readValue(resultUser.getResponse().getContentAsString(), OrderItemDto[].class));
        assertEquals(expected, actualUser);

        MvcResult resultAdmin = mockMvc.perform(get("/orders/" + ORDER_ID + "/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(ADMIN_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Set<OrderItemDto> actualAdmin = Set.of(objectMapper.readValue(resultAdmin.getResponse().getContentAsString(), OrderItemDto[].class));
        assertEquals(expected, actualAdmin);
    }

    @Test

    @DisplayName("Get all items from order of another user")
    public void getAllItemsFromOrder_OrderIdNotBelongsToUser_ThrowException() throws Exception {
        Exception exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/orders/" + ORDER_ID + "/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken("some_user@mail.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn());

        String expected = "Request processing failed: mate.academy.intro.exception.EntityNotFoundException: "
                + "Can't find order with id " + ORDER_ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get item from order")
    public void getItemFromOrder_OrderIdAndId_ReturnOrderItemDto() throws Exception {
        OrderItemDto expected = new OrderItemDto()
                .setId(2L)
                .setBookId(2L)
                .setQuantity(2);

        MvcResult resultUser = mockMvc.perform(get("/orders/" + ORDER_ID + "/items/" + ITEM_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        OrderItemDto actualUser = objectMapper.readValue(resultUser.getResponse().getContentAsString(), OrderItemDto.class);
        assertEquals(expected, actualUser);

        MvcResult resultAdmin = mockMvc.perform(get("/orders/" + ORDER_ID + "/items/" + ITEM_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(ADMIN_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        OrderItemDto actualAdmin = objectMapper.readValue(resultAdmin.getResponse().getContentAsString(), OrderItemDto.class);
        assertEquals(expected, actualAdmin);
    }

    @Test
    @DisplayName("Get order item from order, not belong to user")
    public void getItemFromOrder_OrderIdNotBelongsToUser_ThrowException() throws Exception {
        Exception exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/orders/" + ORDER_ID + "/items/" + ITEM_ID)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                        + jwtUtil.generateToken("some_user@mail.com"))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn());

        String expected = "Request processing failed: mate.academy.intro.exception.EntityNotFoundException: "
                + "Can't find order with id " + ORDER_ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get item from order, invalid item id")
    public void getItemFromOrder_InvalidItemId_ThrowException() throws Exception {
        Long itemId = 15L;
        Exception exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/orders/" + ORDER_ID + "/items/" + itemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "
                                + jwtUtil.generateToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn());
        String expected = "Request processing failed: mate.academy.intro.exception.EntityNotFoundException: "
                + "Can't find order item with id " + itemId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    private static void initOrderItemDtoList() {
        ORDER_ITEM_DTOS.add(new OrderItemDto()
                .setId(1L)
                .setBookId(1L)
                .setQuantity(4));
        ORDER_ITEM_DTOS.add(new OrderItemDto()
                .setId(2L)
                .setBookId(2L)
                .setQuantity(5));
        ORDER_ITEM_DTOS.add(new OrderItemDto()
                .setId(3L)
                .setBookId(3L)
                .setQuantity(6));
    }

    private static void initOrderDtoList() {
        ORDER_DTO_LIST.add(new OrderDto()
                .setId(1L)
                .setUserId(1L)
                .setOrderItems(Set.of(
                        new OrderItemDto()
                                .setId(1L)
                                .setBookId(1L)
                                .setQuantity(1),
                        new OrderItemDto()
                                .setId(2L)
                                .setBookId(2L)
                                .setQuantity(2)))
                .setOrderDate(LocalDateTime.of(2020, 8, 7, 19, 34, 20))
                .setTotal(BigDecimal.valueOf(561.8))
                .setStatus("COMPLETED"));
        ORDER_DTO_LIST.add(new OrderDto()
                .setId(2L)
                .setUserId(1L)
                .setOrderItems(Set.of(
                        new OrderItemDto()
                                .setId(3L)
                                .setBookId(2L)
                                .setQuantity(3),
                        new OrderItemDto()
                                .setId(4L)
                                .setBookId(3L)
                                .setQuantity(4)))
                .setOrderDate(LocalDateTime.of(2021, 6, 7, 19, 34, 20))
                .setTotal(BigDecimal.valueOf(1345.5))
                .setStatus("COMPLETED"));
    }
}