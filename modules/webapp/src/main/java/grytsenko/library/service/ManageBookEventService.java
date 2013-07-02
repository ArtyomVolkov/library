package grytsenko.library.service;

import static grytsenko.library.util.DateUtils.now;
import grytsenko.library.model.BookEvent;
import grytsenko.library.model.User;
import grytsenko.library.repository.BookEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageBookEventService {

	@Autowired
	BookEventRepository bookEventRepository;

	/**
     * Reserves a book for book event.
     */
	@Transactional
	public BookEvent reserve(BookEvent bookEvent, User user, Long bookId) {
		bookEvent.reserve(user, bookId, now());
		return update(bookEvent);
	}

	/**
     * Release a book for book event.
     */
	@Transactional
	public BookEvent release(BookEvent bookEvent, User user, Long bookId) {
		bookEvent.release(user, bookId, now());
		return update(bookEvent);
	}
	
	/**
     * Take out a book for book event.
     */
	@Transactional
	public BookEvent tackeOut(BookEvent bookEvent, User user, Long bookId) {
		bookEvent.tackeOut(user, bookId, now());
		return update(bookEvent);
	}

	/**
     * Take back a book for book event.
     */
	@Transactional
	public BookEvent tackeBack(BookEvent bookEvent, User user, Long bookId) {
		bookEvent.tackeBack(user, bookId, now());
		return update(bookEvent);
	}
	
	
	/**
     * Update status book event.
     */
	public BookEvent update(BookEvent bookEvent) {
		return bookEventRepository.saveAndFlush(bookEvent);

	}

}
