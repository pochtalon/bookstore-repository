package mate.academy.intro.repository.book;

import java.util.List;
import mate.academy.intro.model.Book;
import mate.academy.intro.repository.SpecificationProvider;
import mate.academy.intro.repository.SpecificationProviderManager;
import org.springframework.stereotype.Component;

@Component
public class BookSpecificationProviderManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> bookSpecificationProvider;

    public BookSpecificationProviderManager(
            List<SpecificationProvider<Book>> bookSpecificationProvider) {
        this.bookSpecificationProvider = bookSpecificationProvider;
    }

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return bookSpecificationProvider.stream()
                .filter(b -> b.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cant find corect cpec provider for key "
                        + key));
    }
}
