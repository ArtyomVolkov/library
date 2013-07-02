package grytsenko.library.view.controller;

import static grytsenko.library.view.Navigation.BOOK_ID_PARAM;
import static grytsenko.library.view.Navigation.SHARED_BOOK_PATH;
import static grytsenko.library.view.Navigation.USER_ATTR;
import static grytsenko.library.view.Navigation.redirectToSharedBook;
import grytsenko.library.model.BookEvent;
import grytsenko.library.model.SharedBook;
import grytsenko.library.model.User;
import grytsenko.library.service.BookNotUpdatedException;
import grytsenko.library.service.BookeventSqlService;
import grytsenko.library.service.ManageBookEventService;
import grytsenko.library.service.ManageSharedBooksService;
import grytsenko.library.service.ManageUsersService;
import grytsenko.library.service.NotifyUsersService;
import grytsenko.library.service.SearchSharedBooksService;
import grytsenko.library.service.UserNotNotifiedException;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Processes requests for shared book.
 */
@Controller
@RequestMapping(value = SHARED_BOOK_PATH, params = BOOK_ID_PARAM)
@SessionAttributes({ "user" })
public class SharedBookController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SharedBookController.class);

    @Autowired
    ManageUsersService manageUsersService;
    @Autowired
    NotifyUsersService notifyUsersService;

    @Autowired
    SearchSharedBooksService searchSharedBooksService;
    @Autowired
    ManageSharedBooksService manageSharedBooksService;
    @Autowired
    ManageBookEventService manageBookEventService; 

    @ModelAttribute(USER_ATTR)
    public User currentUser(Principal principal) {
        return manageUsersService.find(principal.getName());
    }

    /**
     * User views details about book.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String getBook(@RequestParam(BOOK_ID_PARAM) Long bookId, Model model) {
        LOGGER.debug("Find book {}.", bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        model.addAttribute("book", book);
        
        BookeventSqlService sqlService = new BookeventSqlService();
        List<BookEvent> bookEvent = sqlService.getList(bookId);
        model.addAttribute("bookEvent", bookEvent);
        
        return SHARED_BOOK_PATH;
    }

    /**
     * User reserves a book.
     */
    @RequestMapping(params = "reserve", method = RequestMethod.POST)
    public String reserve(@RequestParam(BOOK_ID_PARAM) Long bookId,
            @ModelAttribute(USER_ATTR) User user)
            throws BookNotUpdatedException, UserNotNotifiedException {
        LOGGER.debug("Reserve book {}.", bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        book = manageSharedBooksService.reserve(book, user);
        notifyUsersService.notifyReserved(book, user);
        BookEvent bookEvent = new BookEvent();
        bookEvent = manageBookEventService.reserve(bookEvent, user, bookId);

        return redirectToSharedBook(bookId);
    }

    /**
     * User or manager releases a book.
     */
    @RequestMapping(params = "release", method = RequestMethod.POST)
    public String release(@RequestParam(BOOK_ID_PARAM) Long bookId,
            @ModelAttribute(USER_ATTR) User user)
            throws BookNotUpdatedException, UserNotNotifiedException {
        LOGGER.debug("Release book {}.", bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        User wasReservedBy = book.getUsedBy();
        book = manageSharedBooksService.release(book, user);
        BookEvent bookEvent = new BookEvent();
        bookEvent = manageBookEventService.release(bookEvent, user, bookId);
        notifyUsersService.notifyReleased(book, wasReservedBy);

        return redirectToSharedBook(bookId);
    }

    /**
     * Manager takes out a book from library.
     */
    @RequestMapping(params = "takeOut", method = RequestMethod.POST)
    public String takeOut(@RequestParam(BOOK_ID_PARAM) Long bookId,
            @ModelAttribute(USER_ATTR) User user)
            throws BookNotUpdatedException, UserNotNotifiedException {
        LOGGER.debug("Take out book {}.", bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        book = manageSharedBooksService.takeOut(book, user);
        BookEvent bookEvent = new BookEvent();
        bookEvent = manageBookEventService.tackeOut(bookEvent, user, bookId);
        notifyUsersService.notifyBorrowed(book, book.getUsedBy());

        return redirectToSharedBook(bookId);
    }

    /**
     * Manager takes back a book to library.
     */
    @RequestMapping(params = "takeBack", method = RequestMethod.POST)
    public String takeBack(@RequestParam(BOOK_ID_PARAM) Long bookId,
            @ModelAttribute(USER_ATTR) User user)
            throws BookNotUpdatedException, UserNotNotifiedException {
        LOGGER.debug("Take back book {}.", bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        User wasBorrowedBy = book.getUsedBy();
        book = manageSharedBooksService.takeBack(book, user);
        BookEvent bookEvent = new BookEvent();
        bookEvent = manageBookEventService.tackeBack(bookEvent, user, bookId);
        notifyUsersService.notifyReturned(book, wasBorrowedBy);

        return redirectToSharedBook(bookId);
    }

    /**
     * Manager reminds that book is reserved or is borrowed by user.
     */
    @RequestMapping(params = "remind", method = RequestMethod.POST)
    public String remind(@RequestParam(BOOK_ID_PARAM) Long bookId,
            @ModelAttribute(USER_ATTR) User user)
            throws UserNotNotifiedException {
        LOGGER.debug("Remind about book {}.", bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        if (!book.isManagedBy(user)) {
            LOGGER.debug("Book {} is not managed by {}.", bookId,
                    user.getUsername());
            return redirectToSharedBook(bookId);
        }

        if (book.isReserved()) {
            notifyUsersService.notifyReserved(book, book.getUsedBy());
        } else if (book.isBorrowed()) {
            notifyUsersService.notifyBorrowed(book, book.getUsedBy());
        }

        return redirectToSharedBook(bookId);
    }

    /**
     * User creates subscription to the book.
     */
    @RequestMapping(params = "subscribe", method = RequestMethod.POST)
    public String subscribe(@RequestParam(BOOK_ID_PARAM) Long bookId,
            @ModelAttribute(USER_ATTR) User user)
            throws BookNotUpdatedException, UserNotNotifiedException {
        LOGGER.debug("Subscribe {} to emails about book {}.",
                user.getUsername(), bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        book = manageSharedBooksService.subscribe(book, user);

        return redirectToSharedBook(bookId);
    }

    /**
     * User cancels subscription to the book.
     */
    @RequestMapping(params = "unsubscribe", method = RequestMethod.POST)
    public String unsubscribe(@RequestParam(BOOK_ID_PARAM) Long bookId,
            @ModelAttribute(USER_ATTR) User user)
            throws BookNotUpdatedException, UserNotNotifiedException {
        LOGGER.debug("Unsubscribe {} from emails about book {}.",
                user.getUsername(), bookId);

        SharedBook book = searchSharedBooksService.find(bookId);
        book = manageSharedBooksService.unsubscribe(book, user);

        return redirectToSharedBook(bookId);
    }

    /**
     * If book was not updated, then notification should be shown.
     */
    @ExceptionHandler(BookNotUpdatedException.class)
    public String whenBookNotUpdated(BookNotUpdatedException exception,
            HttpServletRequest request) {
        Long bookId = Long.parseLong(request.getParameter(BOOK_ID_PARAM));
        LOGGER.warn("Book {} was not updated, because: '{}'.", bookId,
                exception.getMessage());

        FlashMap attrs = RequestContextUtils.getOutputFlashMap(request);
        attrs.put("bookNotUpdated", true);

        return redirectToSharedBook(bookId);
    }

    /**
     * If user was not notified, then notification should be shown.
     */
    @ExceptionHandler(UserNotNotifiedException.class)
    public String whenUserNotNotified(UserNotNotifiedException exception,
            HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_ATTR);
        LOGGER.warn("User {} was not notified, because: '{}'.",
                user.getUsername(), exception.getMessage());

        FlashMap attrs = RequestContextUtils.getOutputFlashMap(request);
        attrs.put("userNotNotified", true);

        Long bookId = Long.parseLong(request.getParameter(BOOK_ID_PARAM));
        return redirectToSharedBook(bookId);
    }

}
