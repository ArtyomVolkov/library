package grytsenko.library.service;

import static grytsenko.library.test.MockitoUtils.doReturnFirstArgument;
import static grytsenko.library.test.TestBooks.availableBook;
import static grytsenko.library.test.TestBooks.borrowedBook;
import static grytsenko.library.test.TestBooks.reservedBook;
import static grytsenko.library.test.TestUsers.guest;
import static grytsenko.library.test.TestUsers.manager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import grytsenko.library.model.Book;
import grytsenko.library.model.BookStatus;
import grytsenko.library.model.User;
import grytsenko.library.model.UserRole;
import grytsenko.library.repository.BookRepository;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BookService}.
 */
public class BookServiceTests {

    BookRepository bookRepository;

    BookService bookService;

    @Before
    public void prepare() {
        // Setup SUT.
        bookRepository = mock(BookRepository.class);

        bookService = new BookService(bookRepository);

        // Setup behavior.
        doReturnFirstArgument().when(bookRepository).saveAndFlush(
                any(Book.class));
    }

    /**
     * User reserves a book.
     */
    @Test
    public void testReserve() throws Exception {
        // Setup data.
        User reservedBy = guest();
        Book availableBook = availableBook();

        // Execute.
        Book reservedBook = bookService.reserve(availableBook, reservedBy);

        // Verify state.
        assertEquals(BookStatus.RESERVED, reservedBook.getStatus());
        assertEquals(reservedBy, reservedBook.getReservedBy());
        assertNotNull(reservedBook.getReservedSince());

        // Verify behavior.
        verify(bookRepository).saveAndFlush(availableBook);
        verifyNoMoreInteractions(bookRepository);
    }

    /**
     * User reserves a book, but it is not available.
     */
    @Test
    public void testReserveNotAvailable() throws Exception {
        // Setup data.
        Book reservedBook = reservedBook(guest());

        // Execute.
        try {
            bookService.reserve(reservedBook, manager());
            fail();
        } catch (BookServiceException exception) {
        }

        // Verify behavior.
        verifyZeroInteractions(bookRepository);
    }

    /**
     * User, who has reserved book, releases it.
     */
    @Test
    public void testReleaseByUser() throws Exception {
        // Setup data.
        User reservedBy = guest();
        Book reservedBook = reservedBook(reservedBy);

        // Execute.
        Book availableBook = bookService.release(reservedBook, reservedBy);

        // Verify state.
        assertEquals(BookStatus.AVAILABLE, availableBook.getStatus());
        assertNull(availableBook.getReservedBy());

        // Verify behavior.
        verify(bookRepository).saveAndFlush(reservedBook);
        verifyNoMoreInteractions(bookRepository);
    }

    /**
     * Manager releases a book.
     */
    @Test
    public void testReleaseByManager() throws Exception {
        // Setup data.
        Book reservedBook = reservedBook(guest());

        // Execute.
        Book availableBook = bookService.release(reservedBook, manager());

        // Verify state.
        assertEquals(BookStatus.AVAILABLE, availableBook.getStatus());
        assertNull(availableBook.getReservedBy());

        // Verify behavior.
        verify(bookRepository).saveAndFlush(reservedBook);
        verifyNoMoreInteractions(bookRepository);
    }

    /**
     * Other user (not a one, who has reserved a book, and not a manager)
     * releases a book.
     */
    @Test
    public void testReleaseNotAllowed() {
        // Setup data.
        User releasedBy = new User();
        releasedBy.setId(8L);
        releasedBy.setUsername("other");
        releasedBy.setRole(UserRole.USER);
        Book reservedBook = reservedBook(guest());

        // Execute.
        try {
            bookService.release(reservedBook, releasedBy);
            fail();
        } catch (BookServiceException exception) {
        }

        // Verify behavior.
        verifyZeroInteractions(bookRepository);
    }

    /**
     * Manager releases a book, which was not reserved.
     */
    @Test
    public void testReleaseNotReserved() throws Exception {
        // Setup data.
        Book availableBook = availableBook();

        // Execute.
        try {
            bookService.release(availableBook, manager());
            fail();
        } catch (BookServiceException exception) {
        }

        // Verify behavior.
        verifyZeroInteractions(bookRepository);
    }

    /**
     * When user borrows a reserved book, manager takes out this book from
     * library.
     */
    @Test
    public void testTakeOut() throws Exception {
        // Setup data.
        User borrowedBy = guest();
        Book reservedBook = reservedBook(borrowedBy);

        // Execute.
        Book borrowedBook = bookService.takeOut(reservedBook, manager());

        // Verify state.
        assertEquals(BookStatus.BORROWED, borrowedBook.getStatus());

        assertEquals(borrowedBy, borrowedBook.getBorrowedBy());
        assertNotNull(borrowedBook.getBorrowedSince());

        assertNull(borrowedBook.getReservedBy());
        assertNull(borrowedBook.getReservedSince());

        // Verify behavior.
        verify(bookRepository).saveAndFlush(reservedBook);
        verifyNoMoreInteractions(bookRepository);
    }

    /**
     * Some user, but not a manager, tries to take out a book.
     */
    @Test
    public void testTakeOutNotAllowed() throws Exception {
        // Setup data.
        Book reservedBook = reservedBook(guest());

        // Execute.
        try {
            bookService.takeOut(reservedBook, guest());
            fail();
        } catch (BookServiceException exception) {
        }

        // Verify behavior.
        verifyZeroInteractions(bookRepository);
    }

    /**
     * Manager tries to take out a book, that is not reserved.
     */
    @Test
    public void testTakeOutNotReserved() {
        // Setup data.
        Book availableBook = availableBook();

        // Execute.
        try {
            bookService.takeOut(availableBook, manager());
            fail();
        } catch (BookServiceException exception) {
        }

        // Verify behavior.
        verifyZeroInteractions(bookRepository);
    }

    /**
     * Manager takes back a book to library.
     */
    @Test
    public void testTakeBack() throws Exception {
        // Setup data.
        User manager = manager();
        Book borrowedBook = borrowedBook(guest());

        // Execute.
        Book availableBook = bookService.takeBack(borrowedBook, manager);

        // Verify state.
        assertEquals(BookStatus.AVAILABLE, availableBook.getStatus());

        assertEquals(manager, availableBook.getManagedBy());
        assertNotNull(availableBook.getManagedSince());

        assertNull(availableBook.getBorrowedBy());
        assertNull(availableBook.getBorrowedSince());

        // Verify behavior.
        verify(bookRepository).saveAndFlush(borrowedBook);
        verifyNoMoreInteractions(bookRepository);
    }

    /**
     * Some user, but not a manager, tries to take back a book.
     */
    @Test
    public void testTakeBackNotAllowed() throws Exception {
        // Setup data.
        Book borrowedBook = borrowedBook(guest());

        // Execute.
        try {
            bookService.takeBack(borrowedBook, guest());
            fail();
        } catch (BookServiceException exception) {
        }

        // Verify behavior.
        verifyZeroInteractions(bookRepository);
    }

    /**
     * Manager tries to take out a book, that is not borrowed.
     */
    @Test
    public void testTakeBackNotBorrowed() {
        // Setup data.
        Book availableBook = availableBook();

        // Execute.
        try {
            bookService.takeBack(availableBook, manager());
            fail();
        } catch (BookServiceException exception) {
        }

        // Verify behavior.
        verifyZeroInteractions(bookRepository);
    }

}
