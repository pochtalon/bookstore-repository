package mate.academy.intro.repository.orderitem;

import java.util.List;
import mate.academy.intro.model.Order;
import mate.academy.intro.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> getAllByOrder(Order order);
}
