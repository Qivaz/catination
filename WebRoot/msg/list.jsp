<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8"
	contentType="text/html;charset=UTF-8"%>
<%@ include file="/includes/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Admin Console</title>
<meta name="menu" content="msg" />
<link rel="stylesheet" type="text/css" href="<c:url value='/styles/tablesorter/style.css'/>" />
<script type="text/javascript" src="<c:url value='/scripts/jquery.tablesorter.js'/>"></script>
</head>

<body>

<h1>Messages</h1>

<table id="tableList" class="tablesorter" cellspacing="1">
	<thead>
		<tr>
			<th>Group-Or-Not</th>
			<th>From</th>
			<th>To</th>
			<th>Date</th>
			<th>Message</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="msg" items="${msgList}">
			<tr>
				<td align="center">
				<c:choose>
				<c:when test="${(user.type==2) eq true}">
					<img src="images/user-online.png" />
				</c:when>
				<c:otherwise>
					<img src="images/user-offline.png" />
				</c:otherwise>
				</c:choose>
				</td>
				<td><c:out value="${msg.from}" /></td>
				<td><c:out value="${msg.to}" /></td>
				<td align="center"><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${msg.createdDate}" /></td>
				<td><c:out value="${msg.message}" /></td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<script type="text/javascript">
//<![CDATA[
$(function() {
	$('#tableList').tablesorter();
	//$('#tableList').tablesorter( {sortList: [[0,0], [1,0]]} );
	//$('table tr:nth-child(odd)').addClass('odd');
	$('table tr:nth-child(even)').addClass('even');	 
});
//]]>
</script>

</body>
</html>
