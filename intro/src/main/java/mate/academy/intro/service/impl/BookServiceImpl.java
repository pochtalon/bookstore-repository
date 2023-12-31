package mate.academy.intro.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.book.BookDto;
import mate.academy.intro.dto.book.BookDtoWithoutCategoriesIds;
import mate.academy.intro.dto.book.BookSearchParameters;
import mate.academy.intro.dto.book.CreateBookRequestDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.BookMapper;
import mate.academy.intro.model.Book;
import mate.academy.intro.model.Category;
import mate.academy.intro.repository.book.BookRepository;
import mate.academy.intro.repository.book.BookSpecificationBuilder;
import mate.academy.intro.repository.category.CategoryRepository;
import mate.academy.intro.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder builder;

    @Override
    public BookDto save(CreateBookRequestDto requestDto) {
        Book book = bookMapper.toModel(requestDto);
        if (requestDto.getCategories() != null && requestDto.getCategories().size() > 0) {
            for (Long categoryId : requestDto.getCategories()) {
                Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                        new EntityNotFoundException("Can't find category with id " + categoryId));
                book.getCategories().add(category);
            }
        }
        book.setId(bookRepository.save(book).getId());
        return bookMapper.toDto(book);
    }

    @Override
    public BookDto findById(Long id) {
        return bookMapper.toDto(bookRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't get book with id: " + id)));
    }

    @Override
    public List<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<BookDto> search(List<String> title, List<String> author) {
        BookSearchParameters params = new BookSearchParameters()
                .setTitles(title.toArray(new String[0]))
                .setAuthors(author.toArray(new String[0]));
        Specification<Book> bookSpecification = builder.build(params);
        return bookRepository.findAll(bookSpecification).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto bookDto) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = bookMapper.toModel(bookDto);
            book.setId(id);
            bookRepository.save(book);
            return bookMapper.toDto(book);
        }
        throw new EntityNotFoundException("The book with id " + id + " was not found");
    }

    @Override
    public List<BookDtoWithoutCategoriesIds> findAllByCategoryId(Long categoryId) {
        return bookRepository.getAllByCategoriesId(categoryId).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }
}
