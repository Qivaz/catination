<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<% 
String path = request.getContextPath(); 
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
System.out.println("path="+path);
System.out.println("basePath="+basePath);
%>
<%@ include file="/includes/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <base href="<%=basePath%>"> 
    <title>Admin Console</title>
	<meta name="menu" content="upload" />    
</head>

<body>

<h1>Upload!</h1>

<form action="/upload.do?action=send" method="post" enctype="multipart/form-data"> 
<label>Advertisement 1:</label><br/>
<input type="file" name="file1" id="file1"/><br/>
<input type="text" name="file1_info" /><br/>
<label>Advertisement 2:</label><br/>
<input type="file" name="file2" id="file2"/><br/>
<input type="text" name="file2_info" /><br/>
<label>Advertisement 3:</label><br/>
<input type="file" name="file3" id="file3"/><br/>
<input type="text" name="file3_info" /><br/>
<label>Advertisement 4:</label><br/>
<input type="file" name="file4" id="file4"/><br/>
<input type="text" name="file4_info" /><br/>
<label>Advertisement 5:</label><br/>
<input type="file" name="file5" id="file5"/><br/>
<input type="text" name="file5_info" /><br/>
<br/>
<input type="submit" value="  上传  "/> 
</form> 

</body>
</html>
