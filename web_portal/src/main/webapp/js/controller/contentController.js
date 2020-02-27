app.controller("contentController",function($scope,contentService,cartService){
	$scope.showName=function() {
		cartService.showName().success(
			function(response){
				if (response.loginName=="" || response.loginName==null){
					$("#showOrLogin").html("请 <a href=\"login.html\">登录</a>\n" +
						"\t\t\t\t\t\t<span><a href=\"http://localhost:8083/register.html\">免费注册</a></span>");
					return;
				}
				$(".loginName").html(response.loginName);
				$(".logout").html("<span class=\"safe\"><a href=\"logout/cas\">退出登录 </a></span>");
				// $scope.loginName=response.loginName;
			}
		);
	}
	$scope.contentList = [];
	// 根据分类ID查询广告的方法:
	$scope.findByCategoryId = function(categoryId){
		contentService.findByCategoryId(categoryId).success(function(response){
			$scope.contentList[categoryId] = response;
		});
	}

	//搜索,跳转到portal系统查询列表页面(传递参数）
	$scope.search=function(){
		if ($("#autocomplete").val() == null || $("#autocomplete").val() == "" || $("#autocomplete").val()=="undefined") {
			$scope.keywords="手机";
		}
		location.href="http://localhost:8080/search.html#?keywords="+$scope.keywords;
	}
});