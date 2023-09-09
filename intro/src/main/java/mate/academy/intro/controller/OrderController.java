package mate.academy.intro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.awt.print.Pageable;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.order.AddressRequestDto;
import mate.academy.intro.dto.order.OrderDto;
import mate.academy.intro.model.User;
import mate.academy.intro.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
}
