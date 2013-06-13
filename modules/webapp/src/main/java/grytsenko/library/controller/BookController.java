package grytsenko.library.controller;

import grytsenko.library.model.Book;
import grytsenko.library.model.BookStatus;
import grytsenko.library.model.User;
import grytsenko.library.service.BookService;
import grytsenko.library.service.BookServiceException;
import grytsenko.library.service.MailService;
import grytsenko.library.service.MailServiceException;
import grytsenko.library.service.UserService;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Manages requests for a single book.
 */
@Controller
@RequestMapping(value = "/book", params = "bookId")
@SessionAttributes({ "user", "book" })
public class BookController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BookController.class);

    private UserService userService;
    private BookService bookService;
    private MailService mailService;

    @Autowired
    public BookController(UserService userService, BookService bookService,
            MailService mailService) {
        this.userService = userService;
        this.bookService = bookService;
        this.mailService = mailService;
    }

    @ModelAttribute("user")
    public User currentUser(Principal principal) {
        return userService.get(principal.getName());
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getBook(@RequestParam("bookId") Long bookId, Model model) {
        try {
            Book book = bookService.find(bookId);
            model.addAttribute("book", book);
        } catch (BookServiceException exception) {
            LOGGER.warn("Book {} was not found.", bookId);
            model.addAttribute("lastOperationFailed", true);
        }
        return "book";
    }

    /**
     * User reserves a book.
     */
    @RequestMapping(params = "reserve", method = RequestMethod.POST)
    public String reserve(@ModelAttribute("book") Book book,
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {
        String username = user.getUsername();
        Long bookId = book.getId();

        LOGGER.debug("User {} reserves the book {}.", username, bookId);

        try {
            bookService.reserve(book, user);
            LOGGER.debug("The book {} was reserved for {}.", bookId, username);
        } catch (BookServiceException exception) {
            LOGGER.warn("The book {} wasn't reserved for {}.", bookId, username);
            LOGGER.warn("Reason: '{}'.", exception.getMessage());

            redirectAttributes.addFlashAttribute("lastOperationFailed", true);

            return redirectToBook(bookId);
        }

        try {
            mailService.notifyReserved(book, book.getReservedBy());
            LOGGER.debug("Notification was sent to {}.", username);
        } catch (MailServiceException exception) {
            LOGGER.warn("Notification was not sent to {}.", username);
        }

        return redirectToBook(bookId);
    }

    /**
     * User or manager releases a book.
     */
    @RequestMapping(params = "release", method = RequestMethod.POST)
    public String release(@ModelAttribute("book") Book book,
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {
        String username = user.getUsername();
        Long bookId = book.getId();

        LOGGER.debug("User {} releases the book {}.", username, bookId);

        try {
            bookService.release(book, user);
            LOGGER.debug("The book {} was released by {}.", bookId, username);
        } catch (BookServiceException exception) {
            LOGGER.warn("The book {} wasn't released by {}.", bookId, username);
            LOGGER.warn("Reason: '{}'.", exception.getMessage());

            redirectAttributes.addFlashAttribute("lastOperationFailed", true);

            return redirectToBook(bookId);
        }

        try {
            mailService.notifyReleased(book, user);
            LOGGER.debug("Notification was sent to {}.", username);
        } catch (MailServiceException exception) {
            LOGGER.warn("Notification was not sent to {}.", username);
        }

        return redirectToBook(bookId);
    }

    /**
     * Manager takes out a book from library.
     */
    @RequestMapping(params = "takeOut", method = RequestMethod.POST)
    public String takeOut(@ModelAttribute("book") Book book,
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {
        String username = user.getUsername();
        Long bookId = book.getId();

        LOGGER.debug("User {} takes out the book {}.", username, bookId);

        try {
            bookService.takeOut(book, user);
            LOGGER.debug("The book {} was taken out by {}.", bookId, username);
        } catch (BookServiceException exception) {
            LOGGER.warn("The book {} wasn't taken out by {}.", bookId, username);
            LOGGER.warn("Reason: '{}'.", exception.getMessage());

            redirectAttributes.addFlashAttribute("lastOperationFailed", true);

            return redirectToBook(bookId);
        }

        try {
            mailService.notifyBorrowed(book, book.getBorrowedBy());
            LOGGER.debug("Notification was sent to {}.", username);
        } catch (MailServiceException exception) {
            LOGGER.warn("Notification was not sent to {}.", username);
        }

        return redirectToBook(bookId);
    }

    /**
     * Manager takes back a book to library.
     */
    @RequestMapping(params = "takeBack", method = RequestMethod.POST)
    public String takeBack(@ModelAttribute("book") Book book,
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {
        String username = user.getUsername();
        Long bookId = book.getId();

        LOGGER.debug("User {} takes back the book {}.", username, bookId);

        try {
            bookService.takeBack(book, user);
            LOGGER.debug("The book {} was taken back by {}.", bookId, username);
        } catch (BookServiceException exception) {
            LOGGER.warn("The book {} wasn't taken back by {}.", bookId,
                    username);
            LOGGER.warn("Reason: '{}'.", exception.getMessage());

            redirectAttributes.addFlashAttribute("lastOperationFailed", true);

            return redirectToBook(bookId);
        }

        try {
            mailService.notifyReturned(book, user);
            LOGGER.debug("Notification was sent to {}.", username);
        } catch (MailServiceException exception) {
            LOGGER.warn("Notification was not sent to {}.", username);
        }

        return redirectToBook(bookId);
    }

    /**
     * Manager reminds that book is reserved or is borrowed by user.
     */
    @RequestMapping(params = "remind", method = RequestMethod.POST)
    public String remind(@ModelAttribute("book") Book book,
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {
        String username = user.getUsername();
        Long bookId = book.getId();
        BookStatus bookStatus = book.getStatus();

        LOGGER.debug("Manager {} reminds that the book {} is {}.", username,
                bookId, bookStatus);

        try {
            if (book.getStatus() == BookStatus.RESERVED) {
                mailService.notifyReserved(book, book.getReservedBy());
            } else if (book.getStatus() == BookStatus.BORROWED) {
                mailService.notifyBorrowed(book, book.getBorrowedBy());
            }
        } catch (MailServiceException exception) {
            LOGGER.warn("Notification was not sent to {}.", username);

            redirectAttributes.addFlashAttribute("lastOperationFailed", true);
        }

        return redirectToBook(bookId);
    }

    private String redirectToBook(Long bookId) {
        return "redirect:/book?bookId=" + bookId;
    }

}
