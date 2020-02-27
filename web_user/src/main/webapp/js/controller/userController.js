//控制层
app.controller('userController', function ($scope, $controller, userService) {

    //注册用户
    $scope.reg = function () {
    	//判断用户名是否为空
    	if($("#username").val()==null || $("#username").val()==""){
			alert("用户名不能为空");
			return;
		}
		//比较两次输入的密码是否一致
    	if ($("#password1").val()==null || $("#password1").val()==""
			|| $("#password2").val()==null || $("#password2").val()=="" || $("#password1").val()!=$("#password2").val()){
			alert("两次输入密码不一致，请重新输入");
			return;
		}
        if ($("#phone").val() == null || $("#phone").val() == "") {
            alert("请输入手机号!");
            return;
        }
		if($("#YZM").val()==null || $("#YZM").val()==""){
			alert("验证码不能为空");
			return;
		}
        //新增
        userService.add($scope.entity, $scope.smsCode).success(
            function (response) {
                if(response.message=="用户注册成功!"){
                    location.href="http://localhost:8083/";
                }
            }
        );
    }

    //发送验证码
    $scope.sendCode = function () {
        $("#YZM").removeAttr("disabled","");
        if ($("#phone").val() == null || $("#phone").val() == "") {
            alert("请输入手机号!");
            return;
        }
        time = 120;//验证码的默认时间设置为120秒
        _settime($("#YZMCode"));
		userService.sendCode($scope.entity.phone  ).success(
			function(response){
				alert(response.message);
			}
		);
        function _settime(obj) {
            if (time == 120) {
                obj.html(time + "秒重新发送验证码!");
                obj.attr("disabled", "disabled");
                time--;
            } else if (time == 0) {
                obj.removeAttr("disabled");
                obj.html("收不到?可重新发送!");
                return;
            } else {
                obj.html(time + "秒重新发送验证码!");
                time--;
            }
            setTimeout(function () {
                _settime(obj);
            }, 1000);
        }
    }
});	
