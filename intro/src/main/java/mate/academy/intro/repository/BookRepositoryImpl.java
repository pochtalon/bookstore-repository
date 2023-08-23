package mate.academy.intro.repository;

import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.model.Book;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepositoryImpl implements BookRepository {
    private final SessionFactory sessionFactory;

    @Autowired
    public BookRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Book save(Book book) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(book);
            transaction.commit();
            return book;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't add book " + book + " to DB", e);
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Book book = session.find(Book.class, id);
            return Optional.ofNullable(book);
        } catch (Exception e) {
            throw new RuntimeException("Can't get book with id: " + id, e);
        }
    }

    @Override
    public List<Book> findAll() {
        String query = "FROM Book";
        try (Session session = sessionFactory.openSession()) {
            Query getAllBooks = session.createQuery(query, Book.class);
            return getAllBooks.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Can't get list of books from DB", e);
        }
    }
}
