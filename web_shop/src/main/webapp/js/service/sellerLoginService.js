app.service("loginService",function($http){
	
	this.showName = function(){
		return $http.get("../sellerLogin/showName.do");
	}
});