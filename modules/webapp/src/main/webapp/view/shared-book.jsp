<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>

<html>

<head>
<meta charset="utf-8">

<title><fmt:message key="app.title" /></title>

<link href="<c:url value="/libs/bootstrap/css/bootstrap.css" />"
    rel="stylesheet" media="screen" />
<link href="<c:url value="/libs/fancybox/css/jquery.fancybox.css" />"
    rel="stylesheet" media="screen" />
<link href="<c:url value="/libs/library/css/common.css" />" rel="stylesheet" />
</head>

<body>
    <%@include file="components/navbar.jsp"%>

    <div class="container">
        <div class="row-fluid">
            <c:set var="bookDetails" value="${book.details}" />
            <%@include file="components/book-details.jsp"%>
        </div>

        <hr />

        <!-- Notifications -->
        <div class="row-fluid">
            <c:if test="${not empty bookNotUpdated}">
                <!-- Book was not updated. -->
                <div class="alert alert-error">
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                    <fmt:message key="warning.book.not.updated" />
                </div>
            </c:if>
            <c:if test="${not empty userNotNotified}">
                <!-- User was not notified. -->
                <div class="alert alert-warning">
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                    <fmt:message key="warning.user.not.notified" />
                </div>
            </c:if>
        </div>

        <!-- Status -->
        <div class="row-fluid">
            <c:choose>
                <c:when test="${book.available}">
                    <p class="text-info">
                        <fmt:message key="book.message.available" />
                    </p>
                </c:when>
                <c:when test="${book.reserved}">
                    <p class="text-info">
                        <fmt:message key="book.message.reserved">
                            <fmt:param
                                value="<strong>${book.usedBy.readableName}</strong>" />
                            <fmt:param>
                                <fmt:formatDate type="date" dateStyle="short"
                                    value="${book.usedSince}" />
                            </fmt:param>
                        </fmt:message>
                    </p>
                </c:when>
                <c:when test="${book.borrowed}">
                    <p class="text-info">
                        <fmt:message key="book.message.borrowed">
                            <fmt:param
                                value="<strong>${book.usedBy.readableName}</strong>" />
                            <fmt:param>
                                <fmt:formatDate type="date" dateStyle="short"
                                    value="${book.usedSince}" />
                            </fmt:param>
                        </fmt:message>
                    </p>
                </c:when>
            </c:choose>
        </div>

        <!-- Actions -->
        <div class="row-fluid">
            <form:form method="POST">
                <div class="pull-right">
                    <p>
                        <c:choose>
                            <c:when test="${book.hasSubscriber(user)}">
                                <button name="unsubscribe" type="submit"
                                    class="btn btn-info">
                                    <i class="icon-envelope"></i>
                                    <fmt:message key="book.action.unsubscribe" />
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button name="subscribe" type="submit"
                                    class="btn btn-info">
                                    <i class="icon-envelope"></i>
                                    <fmt:message key="book.action.subscribe" />
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <p>
                    <c:if
                        test="${book.isManagedBy(user) and (book.reserved or book.borrowed)}">
                        <button name="remind" type="submit" class="btn btn-info">
                            <i class="icon-bell"></i>
                            <fmt:message key="book.action.remind" />
                        </button>
                    </c:if>

                    <c:if test="${book.canBeReserved()}">
                        <button name="reserve" type="submit"
                            class="btn btn-primary">
                            <fmt:message key="book.action.reserve" />
                        </button>
                    </c:if>

                    <c:if test="${book.canBeReleasedBy(user)}">
                        <button name="release" type="submit"
                            class="btn btn-warning">
                            <fmt:message key="book.action.release" />
                        </button>
                    </c:if>
                    <c:if test="${book.canBeTakenOutBy(user)}">
                        <button name="takeOut" type="submit"
                            class="btn btn-warning">
                            <fmt:message key="book.action.takeout" />
                        </button>
                    </c:if>
                    <c:if test="${book.canBeTakenBackBy(user)}">
                        <button name="takeBack" type="submit"
                            class="btn btn-warning">
                            <fmt:message key="book.action.takeback" />
                        </button>
                    </c:if>
                </p>
                	<c:if test="${user.isManager()}">
						<%@include file="components/book-event.jsp"%>
						<c:if test="${!bookEvent.isEmpty()}">
							<button name="clear_log" type="submit" class="btn btn-danger">
							  	<i class="icon-remove"></i>
								<fmt:message key="book.action.clear" />
							</button>
						</c:if>
					</c:if>
            </form:form>
        </div>
    </div>

    <script src="<c:url value="/libs/jquery/jquery.js" />"></script>
    <script src="<c:url value="/libs/bootstrap/js/bootstrap.js" />"></script>
    <script src="<c:url value="/libs/fancybox/js/jquery.fancybox.js" />"></script>
    <script src="<c:url value="/libs/library/js/fancybox.init.js" />"></script>
</body>

</html>
