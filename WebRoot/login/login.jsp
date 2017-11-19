<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Login</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</head>
<body>
	<h0>欢迎使用，请登陆:</h0>
	
	<form action=/login/login.jsp  method=post>
		用户名：<input type="text" name="username"><br>
		密码：&nbsp&nbsp&nbsp&nbsp<input type="password" name="userpwd"><br>
		</br></br><input type="submit" value="  登陆  ">
		&nbsp&nbsp<input type="reset" value="  重置  ">
	</form>
<%
	//session.invalidate();
	System.out.println("login.jsp, request.getParameterMap()="+request.getParameterMap().toString());
	if(request.getParameter("username") !=null && request.getParameter("userpwd") != null){
		if(request.getParameter("username") == "" || request.getParameter("userpwd") == ""){
			System.out.println("<script language=\"JavaScript\">alert(\"用户名和密码不能为空！\")</script>");
		}
		else{
			//Date date = new Date();
			String username=request.getParameter("username");
			String pwd=request.getParameter("userpwd");
			if(username.equals("admin") && pwd.equals("admin")){
				//Cookie usercookie = new Cookie("username", "" + username);
				//usercookie.setMaxAge(60 * 60 * 24 * 365); //设置Cookie有效期
				//response.addCookie(usercookie);
				//session = request.getSession();
				
				System.out.println("REQUST:session="+session);
				session.setAttribute("Login", username);
				response.sendRedirect("/");
			}
			else{
				System.out.println("<script language=\"JavaScript\">alert(\"用户名或密码错误！\")</script>");
			}
		}
	}
%>
</body>
</html>