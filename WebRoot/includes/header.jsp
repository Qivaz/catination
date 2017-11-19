<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8"%>
<%@ include file="/includes/taglibs.jsp"%>

<div id="branding">
	<span style="float:right;"><h0><a name="logout" href="<c:url value='/login/logout.jsp'/>">Logout</a></h0></span>
	<h1><a href="<c:url value='/'/>">Light MSG Administrator Console</a></h1>
	<%	System.out.println("header.jsp, request.getParameterMap()="+request.getParameterMap().toString());
		if (request.getParameter("logout") != null) {
			System.out.println("Logout! session.invalidate()");
			session.invalidate();
		}
		System.out.println("header.jsp, response.getWriter()()="+response.getWriter().toString());
	 %>
</div>
