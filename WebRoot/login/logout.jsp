<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Logout successfully!</title>
</head>
<body id="Logout">
	<div>
	<h1>Logout successfully!</h1>
	<h0><a name="login" href="<c:url value='/login/login.jsp'/>">Login</a></h0>
	
	</div>
	
	<%	System.out.println("logout.jsp, request.getParameterMap()="+request.getParameterMap().toString());
		System.out.println("logout.jsp, response.getWriter()()="+response.getWriter().toString());
		//if (request.getParameter("logout") != null) {
			System.out.println("Logout! session.invalidate()");
			session.invalidate();
		//}
	 %>
</body>
</html>