package mate.academy.intro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.book.BookDto;
import mate.academy.intro.dto.book.BookSearchParameters;
import mate.academy.intro.dto.book.CreateBookRequestDto;
import mate.academy.intro.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Book management", description = "Endpoints for managing books")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/books")
public class BookController {
    private final BookService bookService;

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping
    @Operation(summary = "Get all books", description = "Get a list of all available books")
    public List<BookDto> getAll(Authentication authentication, Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID", description = "Get a book by ID, if present")
    public BookDto getBookById(Authentication authentication, @PathVariable Long id) {
        return bookService.findById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new book", description = "Create a new book")
    public BookDto createBook(Authentication authentication, @RequestBody @Valid CreateBookRequestDto bookDto) {
        return bookService.save(bookDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book by ID", description = "Delete a book by ID from DB, if present in DB")
    public void delete(Authentication authentication, @PathVariable Long id) {
        bookService.deleteById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a book by ID", description = "Update a book by ID")
    public BookDto updateBook(Authentication authentication, @PathVariable Long id, @RequestBody @Valid CreateBookRequestDto bookDto) {
        return bookService.update(id, bookDto);
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/search")
    @Operation(summary = "Update a book by ID",
            description = "Update a book by ID if present in DB")
    public List<BookDto> search(Authentication authentication, BookSearchParameters searchParameters) {
        return bookService.search(searchParameters);
    }
}
