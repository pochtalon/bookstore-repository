package mate.academy.intro.service;

import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.model.User;

public interface OrderService {
    OrderDto createOrder(User user, String shippingAddress);
}
