app.controller('payController' ,function($scope ,$location,payService,cartService){
	$scope.showName=function(){
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
	
	$scope.createNative=function(){
		payService.createNative().success(
			function(response){
				
				//显示订单号和金额
				$scope.money= (response.total_fee/100).toFixed(2);
				$scope.out_trade_no=response.out_trade_no;
				
				//生成二维码
				 var qr=new QRious({
					    element:document.getElementById('qrious'),
						size:250,
						value:response.code_url,
						level:'H'
			     });
				 
				 queryPayStatus();//调用查询
				
			}	
		);	
	}
	
	//调用查询
	queryPayStatus=function(){
		payService.queryPayStatus($scope.out_trade_no).success(
			function(response){
				if(response.success){
					location.href="paysuccess.html#?money="+$scope.money;
				}else{
					if(response.message=='二维码超时'){
						$scope.createNative();//重新生成二维码
					}else{
						location.href="payfail.html";
					}
				}				
			}		
		);		
	}
	
	//获取金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
	
});