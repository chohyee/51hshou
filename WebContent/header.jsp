<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="img/navigate-down.ico" />
</head>
<!-- 登录 注册 购物车... -->
<div class="container-fluid">
	<div class="col-md-4">
		<img style="width:200px;height:60px" src="img/logo2.jpg" />
	</div>
	<div class="col-md-5">
<!-- 		<img src="img/header.png" /> -->
	</div>
	<div class="col-md-3" style="padding-top:20px">
		<ol class="list-inline">
			<c:if test="${empty user }">
				<li><a href="login.jsp">登录</a></li>
				<li><a href="register.jsp">注册</a></li>
			</c:if>
			<c:if test="${!empty user }">
				<span>欢迎您！</span><i style="color:red">${user.username }</i>
			</c:if>
			<li><a href="${pageContext.request.contextPath }/user?method=logout">注销</a></li>
			<li><a href="cart.jsp">购物车</a></li>
			<li><a href="${pageContext.request.contextPath }/product?method=myOrders">我的订单</a></li>
		</ol>
	</div>
</div>

<!-- 导航条 -->
<div class="container-fluid">
	<nav class="navbar navbar-inverse">
		<div class="container-fluid">
			<!-- Brand and toggle get grouped for better mobile display -->
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
					<span class="sr-only">Toggle navigation</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="${pageContext.request.contextPath}">首页</a>
			</div>

			<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
				<ul class="nav navbar-nav" id="categoryUl">
					<!-- <li class="active"><a href="product_list.htm">手机数码<span class="sr-only">(current)</span></a></li> -->
					<!-- 不能用 -->
					<!--<c:forEach items="${categoryList }" var="categoryPro">
						<li><a href="#">${categoryPro.cname }</a></li>
					</c:forEach>-->
				</ul>
				<script type="text/javascript">
				//header.jsp加载完毕后 去服务器端获得所有的category数据
				$(function(){
					var content = "";
					$.post(
						"${pageContext.request.contextPath}/product?method=categoryList",
						function(data){
							//[{"cid":"xxx","cname":"xxxx"},{},{}]
							//动态创建<li><a href="#">${category.cname }</a></li>
							for(var i=0;i<data.length;i++){
								content+="<li><a href='${pageContext.request.contextPath}/product?method=productListByCid&cid="+data[i].cid+"'class='index_nav' onclick='fn(this)'>"+data[i].cname+"</a></li>";
							}
							
							//将拼接好的li放置到ul中
							$("#categoryUl").html(content);
						},
						"json"
					);
				});
				
				</script>
				<form class="navbar-form navbar-right" role="search">
					<div class="form-group">
						<input type="text" class="form-control" placeholder="Solr">
					</div>
					<button type="submit" class="btn btn-default">Submit</button>
				</form>
			</div>
		</div>
	</nav>
</div>
</html>