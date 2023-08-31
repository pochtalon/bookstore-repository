package mate.academy.intro.repository.book;

import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.book.BookSearchParameters;
import mate.academy.intro.model.Book;
import mate.academy.intro.repository.SpecificationBuilder;
import mate.academy.intro.repository.SpecificationProviderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final SpecificationProviderManager<Book> manager;

    @Override
    public Specification<Book> build(BookSearchParameters parameters) {
        Specification<Book> specification = Specification.where(null);
        if (parameters.titles() != null && parameters.titles().length > 0) {
            specification = specification.and(manager.getSpecificationProvider("title")
                    .getSpecification(parameters.titles()));
        }
        if (parameters.authors() != null && parameters.authors().length > 0) {
            specification = specification.and(manager.getSpecificationProvider("author")
                    .getSpecification(parameters.authors()));
        }
        return specification;
    }
}
