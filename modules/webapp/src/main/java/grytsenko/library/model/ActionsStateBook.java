package grytsenko.library.model;

/**
 * Action on the status of the book
 */
public enum ActionsStateBook {

	/**
	 * Book is reserved by someone.
	 */
	RESERVED,

	/**
	 * Book is release by someone.
	 */
	RELEASE,

	/**
	 * Book is take back by manager.
	 */
	TAKEBACK,

	/**
	 * Book is take out by manager.
	 */
	TAKEOUT;

}
