package mate.academy.intro.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.dto.order.OrderItemDto;
import mate.academy.intro.dto.order.StatusRequestDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.OrderItemMapper;
import mate.academy.intro.mapper.OrderMapper;
import mate.academy.intro.model.CartItem;
import mate.academy.intro.model.Order;
import mate.academy.intro.model.OrderItem;
import mate.academy.intro.model.Role;
import mate.academy.intro.model.ShoppingCart;
import mate.academy.intro.model.User;
import mate.academy.intro.repository.order.OrderRepository;
import mate.academy.intro.repository.orderitem.OrderItemRepository;
import mate.academy.intro.repository.role.RoleRepository;
import mate.academy.intro.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.intro.service.OrderService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartRepository cartRepository;
    private final RoleRepository roleRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderDto createOrder(User user, String shippingAddress) {
        Order order = initOrder(user, shippingAddress);
        Order savedOrder = orderRepository.save(order);
        savedOrder.setOrderItems(getOrderItemsFromCart(user.getId(), order));
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem orderItem : savedOrder.getOrderItems()) {
            total = total.add(orderItem.getPrice());
        }
        savedOrder.setTotal(total);
        orderRepository.save(savedOrder);
        //todo clear the cart from cartItems
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public List<OrderDto> findAllOrders(User user) {
        List<Order> listOrders = orderRepository.getAllByUser(user);
        return listOrders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public OrderDto updateOrderStatus(Long id, StatusRequestDto statusRequest) {
        Order order = getOrderById(id);
        order.setStatus(statusRequest.getStatus());
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public List<OrderItemDto> getItemsListFromOrder(User user, Long orderId) {
        Order order = getOrderById(orderId);
        checkAccess(user, order);
        List<OrderItem> orderItems = orderItemRepository.getAllByOrder(order);
        return orderItems.stream()
                .map(orderItemMapper::toDto)
                .toList();
    }

    @Override
    public OrderItemDto getItemFromOrder(User user, Long orderId, Long itemId) {
        Order order = getOrderById(orderId);
        checkAccess(user, order);
        OrderItem orderItem = orderItemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException("Can't find order item with id " + itemId));
        if (!orderItem.getOrder().getId().equals(orderId)) {
            throw new EntityNotFoundException("Can't find order item with id " + itemId);
        }
        return orderItemMapper.toDto(orderItem);
    }

    private void checkAccess(User user, Order order) {
        Role roleAdmin = roleRepository.findRoleByName(Role.RoleName.ROLE_ADMIN).get();
        if (!(user.getRoles().contains(roleAdmin) || order.getUser().equals(user))) {
            throw new EntityNotFoundException("Can't find order with id " + order.getId());
        }
    }

    private Order initOrder(User user, String address) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);
        order.setShippingAddress(address);
        order.setTotal(BigDecimal.ZERO);
        return order;
    }

    private Set<OrderItem> getOrderItemsFromCart(Long id, Order order) {
        ShoppingCart shoppingCartByUserId = getShoppingCartById(id);
        return shoppingCartByUserId.getCartItems().stream()
                .map(this::convertToOrderItem)
                .peek(orderItem -> orderItem.setOrder(order))
                .map(orderItemRepository::save)
                .collect(Collectors.toSet());
    }

    private Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find order with id " + id));
    }

    private ShoppingCart getShoppingCartById(Long id) {
        return cartRepository.findShoppingCartByUserId(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find cart for user with id " + id));
    }

    private OrderItem convertToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBook(cartItem.getBook());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getBook().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return orderItem;
    }
}
