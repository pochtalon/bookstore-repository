package mate.academy.intro.repository;

import mate.academy.intro.model.Book;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationProvider<T> {
    String getKey();

    Specification<T> getSpecification(String[] params);
}
