package mate.academy.intro.service;

import java.util.List;
import mate.academy.intro.dto.book.BookDto;
import mate.academy.intro.dto.book.BookDtoWithoutCategoriesIds;
import mate.academy.intro.dto.book.CreateBookRequestDto;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto requestDto);

    BookDto findById(Long id);

    List<BookDto> findAll(Pageable pageable);

    void deleteById(Long id);

    List<BookDto> search(List<String> title, List<String> author);

    BookDto update(Long id, CreateBookRequestDto bookDto);

    List<BookDtoWithoutCategoriesIds> findAllByCategoryId(Long categoryId);

}
