package mate.academy.intro.service;

import java.util.List;
import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.dto.order.OrderItemDto;
import mate.academy.intro.dto.order.StatusRequestDto;
import mate.academy.intro.model.User;

public interface OrderService {
    OrderDto createOrder(User user, String shippingAddress);

    List<OrderDto> findAllOrders(User user);

    OrderDto updateOrderStatus(Long id, StatusRequestDto statusRequest);

    List<OrderItemDto> getItemsListFromOrder(User user, Long orderId);

    OrderItemDto getItemFromOrder(User user, Long orderId, Long itemId);
}
