package grytsenko.library.service;

import grytsenko.library.model.BookEvent;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class BookeventSqlService {
	private static final String PERSISTENCE_UNIT_NAME = "libraryDatabase";
	private static EntityManagerFactory factory;

	public List<BookEvent> getList(long bookId) {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = factory.createEntityManager();
		Query q = em
				.createQuery("select b from book_event b where b.book_id = "
						+ bookId);
		@SuppressWarnings("unchecked")
		List<BookEvent> list = q.getResultList();
		return list;
	}
}
