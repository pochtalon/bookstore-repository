package mate.academy.intro.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddressRequestDto {
    @NotEmpty
    private String shippingAddress;
}
