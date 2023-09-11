package mate.academy.intro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.order.AddressRequestDto;
import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.dto.order.OrderItemDto;
import mate.academy.intro.dto.order.StatusRequestDto;
import mate.academy.intro.model.User;
import mate.academy.intro.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints for managing orders")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/orders")
public class OrderController {
    private final OrderService orderService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public OrderDto createOrder(Authentication authentication,
                                @RequestBody @Valid AddressRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return orderService.createOrder(user, requestDto.getShippingAddress());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public List<OrderDto> getAllOrders(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return orderService.findAllOrders(user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public OrderDto updateOrderStatus(Authentication authentication,
                                      @PathVariable Long id,
                                      @RequestBody @Valid StatusRequestDto statusRequest) {
        return orderService.updateOrderStatus(id, statusRequest);
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{orderId}/items")
    public List<OrderItemDto> getAllItemsFromOrder(Authentication authentication,
                                                   @PathVariable Long orderId
    ) {
        User user = (User) authentication.getPrincipal();
        return orderService.getItemsListFromOrder(user, orderId);
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItemDto getItemFromOrder(Authentication authentication,
                                         @PathVariable Long orderId,
                                         @PathVariable Long itemId
    ) {
        User user = (User) authentication.getPrincipal();
        return orderService.getItemFromOrder(user, orderId, itemId);
    }
}
