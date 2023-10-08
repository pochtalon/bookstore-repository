package mate.academy.intro.service;

import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.dto.order.OrderItemDto;
import mate.academy.intro.dto.order.StatusRequestDto;
import mate.academy.intro.mapper.OrderItemMapper;
import mate.academy.intro.mapper.OrderMapper;
import mate.academy.intro.model.Order;
import mate.academy.intro.model.OrderItem;
import mate.academy.intro.model.Role;
import mate.academy.intro.model.User;
import mate.academy.intro.repository.order.OrderRepository;
import mate.academy.intro.repository.orderitem.OrderItemRepository;
import mate.academy.intro.repository.role.RoleRepository;
import mate.academy.intro.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @InjectMocks
    OrderServiceImpl orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    private static final Long ORDER_ID = 15L;
    private static final Long ITEM_ID = 10L;
    private static final Long USER_ID = 12L;
    private static final String EMAIL = "user@mail.com";
    private static final String SAVED_PASS = "EnCoDeDpAsSwOrD";
    private static final String FIRST_NAME = "Bob";
    private static final String LAST_NAME = "Marley";
    private static final String ADDRESS = "Jamaica";

    @Test
    @DisplayName("Find all orders for user")
    public void findAllOrders_User_ReturnOrderDtoList() {
        User user = getUser();
        List<OrderDto> expected = new ArrayList<>();

        when(orderRepository.getAllByUser(user)).thenReturn(new ArrayList<>());
        List<OrderDto> actual = orderService.findAllOrders(user);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update order status")
    public void updateOrderStatus_OrderIdAndStatusRequest() {
        StatusRequestDto status = new StatusRequestDto().setStatus(Order.Status.COMPLETED);
        Order order = new Order()
                .setId(ORDER_ID)
                .setUser(new User())
                .setStatus(Order.Status.PENDING);
        OrderDto expected = new OrderDto()
                .setId(ORDER_ID)
                .setStatus("COMPLETED");

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order.setStatus(Order.Status.COMPLETED));
        when(orderMapper.toDto(order)).thenReturn(expected);

        OrderDto actual = orderService.updateOrderStatus(ORDER_ID, status);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get item list from order")
    public void getItemsListFromOrder_UserAndOrderId_ReturnOrderItemDtoList() {
        Role role = new Role().setName(Role.RoleName.ROLE_ADMIN);
        User user = getUser().setRoles(Set.of(role));
        Order order = new Order()
                .setId(ORDER_ID)
                .setUser(user);
        OrderItem item = new OrderItem()
                .setId(1L)
                .setOrder(order);
        List<OrderItem> orderItems = List.of(item);
        order.setOrderItems(Set.of(item));
        OrderItemDto itemDto = new OrderItemDto()
                .setId(1L);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(roleRepository.findRoleByName(Role.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(role));
        when(orderItemRepository.getAllByOrder(order)).thenReturn(orderItems);
        when(orderItemMapper.toDto(item)).thenReturn(itemDto);

        List<OrderItemDto> expected = List.of(itemDto);
        List<OrderItemDto> actual = orderService.getItemsListFromOrder(user, ORDER_ID);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get item from order")
    public void getItemFromOrder_UserAndIdsOfOrderAndItem_ReturnOrderItemDto() {
        Role role = new Role().setName(Role.RoleName.ROLE_ADMIN);
        User user = getUser().setRoles(Set.of(role));
        Order order = new Order()
                .setId(ORDER_ID)
                .setUser(user);
        OrderItem item = new OrderItem()
                .setId(ITEM_ID)
                .setOrder(order);
        order.setOrderItems(Set.of(item));
        OrderItemDto expected = new OrderItemDto()
                .setId(1L);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(roleRepository.findRoleByName(Role.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(role));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderItemMapper.toDto(item)).thenReturn(expected);

        OrderItemDto actual = orderService.getItemFromOrder(user, ORDER_ID, ITEM_ID);
        assertEquals(expected, actual);
    }

    private User getUser() {
        return new User()
                .setId(USER_ID)
                .setEmail(EMAIL)
                .setPassword(SAVED_PASS)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setShippingAddress(ADDRESS);
    }

    private Order initOrder(User user, String address, LocalDateTime time) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(time);
        order.setStatus(Order.Status.PENDING);
        order.setShippingAddress(address);
        order.setTotal(BigDecimal.ZERO);
        return order;
    }
}