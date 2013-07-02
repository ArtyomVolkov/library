package grytsenko.library.repository;

import grytsenko.library.model.BookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository of book event.
 */
public interface BookEventRepository extends JpaRepository<BookEvent, Long> {

}
