package mate.academy.intro.repository.order;

import java.util.List;
import mate.academy.intro.model.Order;
import mate.academy.intro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> getAllByUser(User user);
}
