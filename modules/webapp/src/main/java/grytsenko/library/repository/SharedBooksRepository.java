package grytsenko.library.repository;

import grytsenko.library.model.SharedBook;
import grytsenko.library.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository of shared books.
 */
public interface SharedBooksRepository extends JpaRepository<SharedBook, Long> {

    /**
     * Finds all books which are used by user.
     */
    List<SharedBook> findByUsedBy(User user);

    /**
     * Finds all books of the given subscriber.
     */
    @Query("SELECT book FROM shared_books book INNER JOIN book.subscribers AS subscriber WHERE subscriber = :user GROUP BY book.id")
    List<SharedBook> findBySubscriber(@Param("user") User user);

}
