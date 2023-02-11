<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ru">
<head>
    <title>Meal list</title>
    <style>
        .normal {
            color: green;
        }
        .excess {
            color: red;
        }
        table {
            border-collapse: collapse;
            line-height: 1.4;
            border: 2px solid black;
        }
        th {
            padding: 10px;
            border: 2px solid black;
        }
        td {
            padding: 5px 7px;
            border: 2px solid black;
        }
    </style>
</head>
<body>
<h3><a href="index.html">Home</a></h3>
<h3><a href="users.jsp">Users</a></h3>
<h2>Meals</h2>
<a href="meals?action=create">Add Meal</a>
<br><br>
<table>
    <thead>
    <tr>
        <th>Date</th>
        <th>Description</th>
        <th>Calories</th>
        <th></th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    <jsp:useBean id="meals" scope="request" type="java.util.List"/>
    <c:forEach items="${meals}" var="meal">
        <jsp:useBean id="meal" type="ru.javawebinar.topjava.model.MealTo"/>
        <tr class="${meal.excess ? 'excess' : 'normal'}">
            <td>${meal.dateTime.toLocalDate()} ${meal.dateTime.toLocalTime()}</td>
            <td>${meal.description}</td>
            <td>${meal.calories}</td>
            <td><a href="meals?action=update&id=${meal.id}">Update</a></td>
            <td><a href="meals?action=delete&id=${meal.id}">Delete</a></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>