package grytsenko.library.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * Information about book event.
 */
@Entity(name = "book_event")
public class BookEvent implements Serializable {
	public Date getDateChanges() {
		return dateChanges;
	}

	public void setDateChanges(Date dateChanges) {
		this.dateChanges = dateChanges;
	}

	private static final long serialVersionUID = 6759600794860542365L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Basic
	@Column(name = "book_id", length = 20)
	private Long book_id;

	@Basic
	@Column(name = "user_name", length = 100)
	private String userName;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 10)
	private ActionsStateBook status;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_changes")
	private Date dateChanges;

	@Version
	private Integer version;

	public BookEvent() {
	}

	public ActionsStateBook getStatus() {
		return status;
	}

	public void setStatus(ActionsStateBook status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBook_id() {
		return book_id;
	}

	public void setBook_id(Long book_id) {
		this.book_id = book_id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void reserve(User user, Long bookId, Date dateChange) {

		status = ActionsStateBook.RESERVED;
		userName = user.getUsername();
		book_id = bookId;
		dateChanges = dateChange;
	}

	public void release(User user, Long bookId, Date dateChange) {

		status = ActionsStateBook.RELEASE;
		userName = user.getUsername();
		book_id = bookId;
		dateChanges = dateChange;
	}

	public void tackeOut(User user, Long bookId, Date dateChange) {

		status = ActionsStateBook.TAKEOUT;
		userName = user.getUsername();
		book_id = bookId;
		dateChanges = dateChange;
	}

	public void tackeBack(User user, Long bookId, Date dateChange) {

		status = ActionsStateBook.TAKEBACK;
		userName = user.getUsername();
		book_id = bookId;
		dateChanges = dateChange;
	}

}
