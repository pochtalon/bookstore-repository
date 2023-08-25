package mate.academy.intro.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.intro.model.Book;

public interface BookRepository {
    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();
}
