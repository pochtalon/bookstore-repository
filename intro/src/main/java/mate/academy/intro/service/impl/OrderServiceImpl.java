package mate.academy.intro.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.OrderMapper;
import mate.academy.intro.model.CartItem;
import mate.academy.intro.model.Order;
import mate.academy.intro.model.OrderItem;
import mate.academy.intro.model.ShoppingCart;
import mate.academy.intro.model.User;
import mate.academy.intro.repository.order.OrderRepository;
import mate.academy.intro.repository.orderitem.OrderItemRepository;
import mate.academy.intro.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.intro.service.OrderService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartRepository cartRepository;
    private final OrderMapper orderMapper;

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
        return orderMapper.toDto(savedOrder);
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
        ShoppingCart shoppingCartByUserId = cartRepository.findShoppingCartByUserId(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find cart for user with id " + id));
        return shoppingCartByUserId.getCartItems().stream()
                .map(this::convertToOrderItem)
                .peek(orderItem -> orderItem.setOrder(order))
                .map(orderItemRepository::save)
                .collect(Collectors.toSet());
    }

    private OrderItem convertToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBook(cartItem.getBook());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(new BigDecimal(String.valueOf(cartItem.getBook().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity())))));
        return orderItem;
    }
}
