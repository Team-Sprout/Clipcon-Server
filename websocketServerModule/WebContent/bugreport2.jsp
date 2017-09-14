
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h2>Bug Report</h2>
	<form action="${pageContext.request.contextPath}/bugreport"
		method="post" enctype="multipart/form-data">
		<textarea placeholder="Insert bug message" rows="8" cols="50" name="bugMessage"></textarea>
		<br>
		<input type="submit"></input>
	</form>
</body>
</html>