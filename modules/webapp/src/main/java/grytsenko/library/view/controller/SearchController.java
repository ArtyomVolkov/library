package grytsenko.library.view.controller;

import static grytsenko.library.view.Navigation.SEARCH_PATH;
import static grytsenko.library.view.ViewConstants.THUMBNAILS_PER_PAGE;
import grytsenko.library.model.SearchResults;
import grytsenko.library.model.SharedBook;
import grytsenko.library.model.User;
import grytsenko.library.service.ManageUsersService;
import grytsenko.library.service.SearchSharedBooksService;

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

/**
 * Processes a search requests.
 * 
 * <p>
 * Search is performed over shared books.
 */
@Controller
@RequestMapping(SEARCH_PATH)
@SessionAttributes({ "user" })
public class SearchController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SearchController.class);

    @Autowired
    ManageUsersService manageUsersService;
    @Autowired
    SearchSharedBooksService searchSharedBooksService;

    @ModelAttribute("user")
    public User currentUser(Principal principal) {
        return manageUsersService.find(principal.getName());
    }

    /**
     * User views list of books.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String searchAll(
            @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
            @ModelAttribute("user") User user, Model model) {
        LOGGER.debug("Find all shared books.");
        LOGGER.debug("Take page {}.", pageNum);

        SearchResults<SharedBook> books = searchSharedBooksService.findAll(
                pageNum, THUMBNAILS_PER_PAGE);
        model.addAttribute("searchResults", books);

        return SEARCH_PATH;
    }

}
