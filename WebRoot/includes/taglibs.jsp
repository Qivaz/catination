<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page"%>

<%
	System.out.println("RESP:session="+session);
	String username=(String)session.getAttribute("Login");
	if (username != null){
		System.out.print("Welcome '"+username+"'!!");
		//session.invalidate();
	} else {
		System.out.println("Pls login first!");
		System.out.println("Will redirect to Login.jsp after 0 seconds..");
		response.sendRedirect("/login/login.jsp");
		//response.setHeader("refresh","2;URL=/login/login.jsp");
	}
%>