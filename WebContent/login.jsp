<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>W商城首页，登录</title>
<link rel="stylesheet" href="css/bootstrap.min.css" type="text/css" />
<script src="js/jquery-1.11.3.min.js" type="text/javascript"></script>
<!-- 引入jquery插件，验证 -->
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script src="js/bootstrap.min.js" type="text/javascript"></script>
<!-- 引入自定义css文件 style.css -->
<link rel="stylesheet" href="css/style.css" type="text/css" />
<script type="text/javascript">
	function changeImg(boj){
		boj.src="${pageContext.request.contextPath}/identifyingCode?time="+new Date().getTime();
	}
	//自定义验证码校验规则
	//检测验证码是否正确
		$.validator.addMethod(
		"checkUsername",
		function(value,element,params){
			var flag = false;
		//规则名称、校验函数
		//$.validator.addMethod("校验规则名称",function(value,element,params)){}
		//value:用户输入的内容
		//element:被检验的元素
		//params:规则对应的参数
			$.ajax({
				"async":false,//不能用异步，不然下面的return会先运行,因此要用同步,get,post不能同步
				"url":"${pageContext.request.contextPath}/product?method=checkIdentify",
				"data":{"checkCode":value},//传给服务器的数据
				"dataType":"json",
				"success":function(data){//data为成功返回的数据
					flag = data.isSame;	
				}
			});
		//
			return flag;
		}
	);
	$(function(){
		$("#myCode").validate({
			rules : {
				"checkCode" : {
					"checkUsername":true,
				},
			},
			messages : {
				"checkCode" : {
					"checkUsername":"错误验证码",
				},
			},
		});
	});
</script>
<style>
body {
	margin-top: 20px;
	margin: 0 auto;
}

.carousel-inner .item img {
	width: 100%;
	height: 300px;
}

.container .row div {
	/* position:relative;
				 float:left; */
	
}

font {
	color: #666;
	font-size: 22px;
	font-weight: normal;
	padding-right: 17px;
}
</style>
</head>
<body>

	<!-- 引入header.jsp -->
	<jsp:include page="/header.jsp"></jsp:include>


	<div class="container"
		style="width: 100%; height: 460px; background: #FF2C4C url('images/loginbg.jpg') no-repeat;">
		<div class="row">
			<div class="col-md-7">
				<!--<img src="./image/login.jpg" width="500" height="330" alt="会员登录" title="会员登录">-->
			</div>

			<div class="col-md-5">
				<div
					style="width: 440px; border: 1px solid #E7E7E7; padding: 20px 0 20px 30px; border-radius: 5px; margin-top: 60px; background: #fff;">
					<font>会员登录</font>USER LOGIN
					<div>&nbsp;</div>
					<form class="form-horizontal" id="myCode" action="${pageContext.request.contextPath }/user?method=login" method="post">
						<div class="form-group">
							<label for="username" class="col-sm-2 control-label">用户名</label>
							<div class="col-sm-6">
								<input type="text" class="form-control" id="username" name="username"
									placeholder="请输入用户名">
							</div>
						</div>
						<div class="form-group">
							<label for="inputPassword3" class="col-sm-2 control-label">密码</label>
							<div class="col-sm-6">
								<input type="password" class="form-control" id="inputPassword3" name="password"
									placeholder="请输入密码">
							</div>
						</div>
						<div class="form-group">
							<label for="inputPassword3" class="col-sm-2 control-label">验证码</label>
							<div class="col-sm-3">
								<input type="text" class="form-control" name="checkCode" id="inputPassword3"
									placeholder="请输入验证码">
							</div>
							<div class="col-sm-3" style="margin-left:-30px">
								<img src="${pageContext.request.contextPath }/identifyingCode" onclick="changeImg(this)" />
							</div>
						</div>
						<div class="form-group">
							<div class="col-sm-offset-2 col-sm-10">
								<div class="checkbox">
									<label> <input type="checkbox" name="autoLogin" value="true"> 自动登录
									</label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <label> <input
										type="checkbox"> 记住用户名
									</label>
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="col-sm-offset-2 col-sm-10">
								<input type="submit" width="100" value="登录" name="submit"
									style="background: url('./images/login.gif') no-repeat scroll 0 0 rgba(0, 0, 0, 0); height: 35px; width: 100px; color: white;">
							</div>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>

	<!-- 引入footer.jsp -->
	<jsp:include page="/footer.jsp"></jsp:include>

</body>
</html>