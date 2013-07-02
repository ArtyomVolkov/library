package grytsenko.library.service;

import grytsenko.library.model.BookEvent;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

public class BookeventSqlService {
	private static final String PERSISTENCE_UNIT_NAME = "libraryDatabase";
	private static EntityManagerFactory factory;

	/**
	 * Method finds list of books by ID
	 * 
	 * @param bookId
	 * @return
	 */
	@Transactional
	public List<BookEvent> getList(long bookId) {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = null;
		List<BookEvent> bookEvent = null;
		try {
			em = factory.createEntityManager();
			Query q = em
					.createQuery("select b from book_event b where b.book_id = "
							+ bookId);
			@SuppressWarnings("unchecked")
			List<BookEvent> list = q.getResultList();
			bookEvent = list;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (em != null) {
				em.close();
			}
		}
		return bookEvent;
	}

	/**
	 * Method finds list of Id books.
	 * 
	 * @param bookId
	 * @return list id
	 */
	@Transactional
	public List<Long> getListId(long bookId) {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = null;
		List<Long> listI = null;
		try {
			em = factory.createEntityManager();
			Query q = em
					.createQuery("SELECT id FROM book_event where book_id = "
							+ bookId);
			@SuppressWarnings("unchecked")
			List<Long> list = q.getResultList();
			listI = list;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (em != null) {
				em.close();
			}
		}
		return listI;
	}
}
