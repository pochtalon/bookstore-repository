package mate.academy.intro.dto.book;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateBookRequestDto {
    @NotNull
    private String title;
    @NotNull
    private String author;
    @NotNull
    private String isbn;
    @NotNull
    @Min(0)
    private BigDecimal price;
    private String description;
    private String coverImage;
}
