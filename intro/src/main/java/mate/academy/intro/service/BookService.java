package mate.academy.intro.service;

import java.util.List;
import mate.academy.intro.dto.BookDto;
import mate.academy.intro.dto.BookSearchParameters;
import mate.academy.intro.dto.CreateBookRequestDto;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto requestDto);

    BookDto findById(Long id);

    List<BookDto> findAll(Pageable pageable);

    void deleteById(Long id);

    List<BookDto> search(BookSearchParameters params);

    BookDto update(Long id, CreateBookRequestDto bookDto);
}
