<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>W商城首页，会员注册</title>
<link rel="stylesheet" href="css/bootstrap.min.css" type="text/css" />
<script src="js/jquery-1.11.3.min.js" type="text/javascript"></script>
<!-- 引入jquery插件，验证 -->
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script src="js/bootstrap.min.js" type="text/javascript"></script>
<!-- 引入自定义css文件 style.css -->
<link rel="stylesheet" href="css/style.css" type="text/css" />

<style>
body {
	margin-top: 20px;
	margin: 0 auto;
}

.carousel-inner .item img {
	width: 100%;
	height: 300px;
}

font {
	color: #3164af;
	font-size: 18px;
	font-weight: normal;
	padding: 0 10px;
}
.error {
	/*当验证错误信息出现时，字体为红色*/
	color:red;
}
</style>
<script>
//jquery刷新验证码
function changeImg(boj){
	boj.src="${pageContext.request.contextPath}/identifyingCode?time="+new Date().getTime();
}

//自定义校验规则(本案例为插件和ajax组合规则)
//检测用户名是否存在
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
				"url":"${pageContext.request.contextPath}/user?method=checkUsername",
				"data":{"username":value},//传给服务器的数据
				"dataType":"json",
				"success":function(data){//data为成功返回的数据
					//alert(data);
					flag = data.isExit;	
				}
			});
		//
			return !flag;//function(value,element,params)返回为true表示该规则通过,
						//因此数据库返回true表示存在该名字，因此规则要返回false.
		}
	);
	//自定义验证码校验规则
	//检测验证码是否正确
		$.validator.addMethod(
		"checkIdentifyCode",
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
	$(function() {
		$("#myform").validate({
			rules : {
				"username" : {
					"required" : true,
					"checkUsername":true,
				},
				"password" : {
					"required" : true,
					"rangelength" : [6,12],
				},
				"repassword" : {
					"required" : true,
					"rangelength" : [6,12],
					"equalTo" : "#password",
				},
				"email" : {
					"required" : true,
					"email" : true,
				},
				"name" : {
					"required" : true,
				},
				"sex" : {
					"required" : true,
				},
				"birthday" : {
					"required" : true,
				},
				"checkCode":{
					"checkIdentifyCode":true,
				}
			},
			messages : {
				"username" : {
					"required" : "用户名不能为空",
					"checkUsername":"用户名已存在，请重新输入",
				},
				"password" : {
					"required" : "密码不能为空",
					"rangelength" : "密码长度为6-12位",
				},
				"repassword" : {
					"required" : "确认密码不能为空",
					"rangelength" : "密码长度为6-12位",
					"equalTo" : "两次密码不一致",
				},
				"email" : {
					"required" : "邮箱不能为空",
					"email" : "邮箱格式不正确",
				},
				"name" : {
					"required" : "姓名不能为空",
				},
				"birthday" : {
					"required" : "请选择出生日期",
				},
				"checkCode":{
					"checkIdentifyCode":"验证码输入错误",
				}
			/*sex错误信息直接写在标签里面了*/
			}
		});
	});
</script>
</head>
<body>

	<!-- 引入header.jsp -->
	<jsp:include page="/header.jsp"></jsp:include>

	<div class="container"
		style="width: 100%; background: url('image/regist_bg.jpg');">
		<div class="row">
			<div class="col-md-2"></div>
			<div class="col-md-8"
				style="background: #fff; padding: 40px 80px; margin: 30px; border: 7px solid #ccc;">
				<font>会员注册</font>USER REGISTER
				<form class="form-horizontal" id="myform" action="${pageContext.request.contextPath }/user?method=register" method="post" style="margin-top: 5px;">
					<div class="form-group">
						<label for="username" class="col-sm-2 control-label">用户名</label>
						<div class="col-sm-6">
							<input type="text" class="form-control" name="username" id="username" placeholder="请输入用户名">
						</div>
					</div>
					<div class="form-group">
						<label for="inputPassword3" class="col-sm-2 control-label">密码</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="password" name="password" placeholder="请输入密码">
						</div>
					</div>
					<div class="form-group">
						<label for="confirmpwd" class="col-sm-2 control-label">确认密码</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="confirmpwd" name="repassword"
								placeholder="请输入确认密码">
						</div>
					</div>
					<div class="form-group">
						<label for="inputEmail3" class="col-sm-2 control-label">Email</label>
						<div class="col-sm-6">
							<input type="email" class="form-control" name="email" id="inputEmail3"
								placeholder="Email">
						</div>
					</div>
					<div class="form-group">
						<label for="usercaption" class="col-sm-2 control-label">姓名</label>
						<div class="col-sm-6">
							<input type="text" class="form-control" name="name" id="usercaption"
								placeholder="请输入姓名">
						</div>
					</div>
					<div class="form-group opt">
						<label for="inlineRadio1" class="col-sm-2 control-label">性别</label>
						<div class="col-sm-6">
							<label class="radio-inline"> <input type="radio"
								name="sex" id="sex1" value="male">
								男
							</label> <label class="radio-inline"> <input type="radio"
								name="sex" id="sex2" value="female">
								女
							</label>
							<label class="error" for="sex" style="display:none">请选择性别</label>
						</div>
					</div>
					<div class="form-group">
						<label for="date" class="col-sm-2 control-label">出生日期</label>
						<div class="col-sm-6">
							<input type="date" class="form-control" name="birthday">
						</div>
					</div>

					<div class="form-group">
						<label for="date" class="col-sm-2 control-label">验证码</label>
						<div class="col-sm-3">
							<input type="text" class="form-control" name="checkCode">
						</div>
						<div class="col-sm-2">
							<img src="${pageContext.request.contextPath }/identifyingCode" onclick="changeImg(this)"/>
						</div>

					</div>

					<div class="form-group">
						<div class="col-sm-offset-2 col-sm-10">
							<input type="submit" width="100" value="注册" name="submit"
								style="background: url('./images/register.gif') no-repeat scroll 0 0 rgba(0, 0, 0, 0); height: 35px; width: 100px; color: white;">
						</div>
					</div>
				</form>
			</div>

			<div class="col-md-2"></div>

		</div>
	</div>

	<!-- 引入footer.jsp -->
	<jsp:include page="/footer.jsp"></jsp:include>

</body>
</html>




