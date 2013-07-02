<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<style>
.book-event {
	overflow-y: auto;
	width: 100%;
	height: 25%;
	padding: 5px;
	border: solid 1px black;
	white-space: nowrap;
	border-color: blue;
}
</style>

<h2>Journal of book event</h2>
<div class="book-event">

	<c:if test="${bookEvent.isEmpty()}">
		<fmt:message key="bookEvent.message.empty" />
	</c:if>

	<c:forEach var="bookevent" items="${bookEvent}" varStatus="loop">
		<h5>Book was ${bookevent.status} by ${bookevent.userName} in
			${bookevent.dateChanges}</h5>
	</c:forEach>
</div>

