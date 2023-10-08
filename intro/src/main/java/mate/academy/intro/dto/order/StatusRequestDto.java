package mate.academy.intro.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import mate.academy.intro.model.Order;

@Data
@Accessors(chain = true)
public class StatusRequestDto {
    @NotNull
    private Order.Status status;
}
