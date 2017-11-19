<%@ page language="java" import="java.util.*,java.io.*,com.catination.server.util.JSONUtil" pageEncoding="ISO-8859-1"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

response.setContentType("application/json;charset=utf-8");
response.setCharacterEncoding("UTF-8");
PrintWriter pw = response.getWriter();
//String[] s = {"hell", "world"};
Map<String, Object> map = new HashMap<String, Object>();
map.put("id", "0"); 
map.put("name", "zhang");
pw.write(JSONUtil.toJson(map));
pw.flush();
%>