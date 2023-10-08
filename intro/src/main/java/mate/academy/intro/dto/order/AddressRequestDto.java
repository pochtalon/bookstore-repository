package mate.academy.intro.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AddressRequestDto {
    @NotEmpty
    private String shippingAddress;
}
