package mate.academy.intro.repository.shoppingCart;

import java.util.Optional;
import mate.academy.intro.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
//    @Query(value = "FROM ShoppingCart sc "
//            + "LEFT JOIN FETCH sc.user u "
//            + "LEFT JOIN FETCH sc.cartItems i "
//            + "WHERE u.id = :id")
    Optional<ShoppingCart> findShoppingCartByUserId(Long id);
}
