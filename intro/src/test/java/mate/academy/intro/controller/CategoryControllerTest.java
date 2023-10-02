package mate.academy.intro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import mate.academy.intro.dto.book.BookDto;
import mate.academy.intro.dto.category.CategoryDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static List<CategoryDto> categoriesCatalog = new ArrayList<>();
    private static List<BookDto> booksCatalog = new ArrayList<>();

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/add-three-categories-and-four-books-to-db.sql")
            );
        }
        categoriesCatalogInit();
        booksCatalogInit();
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/clear-books-and-categories-tables.sql")
            );
        }
    }


    private static void categoriesCatalogInit() {
        categoriesCatalog.add(new CategoryDto()
                .setId(1L)
                .setName("Horror")
                .setDescription("Something scary"));
        categoriesCatalog.add(new CategoryDto()
                .setId(2L)
                .setName("Detective")
                .setDescription("Something enigmatic"));
        categoriesCatalog.add(new CategoryDto()
                .setId(3L)
                .setName("Fantasy")
                .setDescription("Something faibled"));
    }

    private static void booksCatalogInit() {
        booksCatalog.add(new BookDto()
                .setId(1L)
                .setTitle("Call of Cthulhu")
                .setAuthor("Howard Lovecraft")
                .setIsbn("978-966-2355-82-6")
                .setPrice(BigDecimal.valueOf(192.8))
                .setDescription("Book about Cthulhu")
                .setCoverImage("Cthulhu_cover")
                .setCategoriesId(Set.of(1L, 3L)));
        booksCatalog.add(new BookDto()
                .setId(2L)
                .setTitle("The Black Cat")
                .setAuthor("Edgar Poe")
                .setIsbn("978-0-8154-1038-6")
                .setPrice(BigDecimal.valueOf(184.3))
                .setDescription("Scary black cat")
                .setCoverImage("Cat_cover")
                .setCategoriesId(Set.of(1L, 2L)));
        booksCatalog.add(new BookDto()
                .setId(3L)
                .setTitle("The Name of the Rose")
                .setAuthor("Umberto Eco")
                .setIsbn("978-0-15-144647-6")
                .setPrice(BigDecimal.valueOf(198.0))
                .setDescription("Murderer is Jorge")
                .setCoverImage("Monastery_cover")
                .setCategoriesId(Set.of(2L)));
        booksCatalog.add(new BookDto()
                .setId(4L)
                .setTitle("Guards! Guards!")
                .setAuthor("Terry Pratchett")
                .setIsbn("0-575-04606-6")
                .setPrice(BigDecimal.valueOf(198.9))
                .setDescription("Night life in Ankh-Morpork")
                .setCoverImage("Samuel_Vimes_cover")
                .setCategoriesId(Set.of(2L, 3L)));
    }
}