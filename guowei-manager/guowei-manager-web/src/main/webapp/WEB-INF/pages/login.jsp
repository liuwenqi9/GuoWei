<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/include/common.jsp"%>

<!DOCTYPE html>
<html>
	<head>
		<!--title-->
       	<%@ include file="/WEB-INF/pages/include/title.jsp" %>
       	
		<link rel="stylesheet" href="<%=path%>/res/plugins/validator/bootstrapValidator.min.css">
		<script src="<%=path%>/res/plugins/validator/bootstrapValidator.min.js"></script>	 
		<script src="<%=path%>/res/plugins/validator/language/zh_CN.js"></script> 
		
		<script>
			$(function() {
				$('input').iCheck({
					checkboxClass : 'icheckbox_square-blue',
					radioClass : 'iradio_square-blue',
					increaseArea : '20%' // optional
				});
			});
		</script>
	</head>
	
	<!-- 正文部分 -->
	<body class="hold-transition login-page" >
		<div class="login-box">
			<div class="login-logo">
				<b style="color: white;"><sp:message code="sys.name"/></b>
			</div>
			<!-- /.login-logo -->
			<div class="login-box-body">
				  <p class="login-box-msg"><sp:message code="manager.login"/></p> 
	
				<form id="sign" action="<%=path%>/managers/sign" method="post">
					<div class="form-group has-feedback">
						<input type="text" class="form-control" name="name" placeholder="<sp:message code="manager.name"/>">
					</div>
					<div class="form-group has-feedback">
						<input type="password" class="form-control" name="password" placeholder="<sp:message code="manager.password"/>">
					</div>
					<div class="row">
						<div class="col-xs-8">
							<div class="checkbox icheck">
								<label> <input type="checkbox"> <sp:message code="manager.remember"/> </label>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<a href="<%=path%>/register.jsp">忘记密码</a>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-12">
							<button type="submit" class="btn btn-primary btn-block"><sp:message code="sys.sign"/></button>
						</div>
					</div>
				</form>
				<div class="row">
					<div class="col-xs-12">
						<a href="<%=path%>/managers/reg" class="btn btn-default btn-block"> <sp:message code="manager.reg"/> </a>
					</div>
				</div>
	
			</div>
			<!-- /.login-box-body -->
		</div>
		<!-- /.login-box -->
		
		<script type="text/javascript">
			$(document).ready(function() {
			    $('#sign').bootstrapValidator({
			        message: 'This value is not valid',
			        feedbackIcons: {
			            valid: 'glyphicon glyphicon-ok',
			            invalid: 'glyphicon glyphicon-remove',
			            validating: 'glyphicon glyphicon-refresh'
			        },
			        fields: {
			            name: {
			                validators: {
			                    notEmpty: {}
			                }
			            },
			            password: {
			                validators: {
			                	notEmpty: {}
			                }
			            }
			        }
			    });
			});
		</script>
	</body>
</html>