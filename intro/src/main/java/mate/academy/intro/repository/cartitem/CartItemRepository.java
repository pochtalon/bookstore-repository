package mate.academy.intro.repository.cartitem;

import mate.academy.intro.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
