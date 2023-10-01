package mate.academy.intro.dto.book;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BookSearchParameters {
    private String[] titles;
    private String[] authors;
}
