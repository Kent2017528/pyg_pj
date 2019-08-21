app.controller('brandController',function ($scope,$controller,brandService) {

    $controller('baseController',{$scope:$scope});//继承






    $scope.searchEntity={};

    //新增
    $scope.save=function () {
        var object={};
        if ($scope.entity.id!=null){
            object=brandService.update($scope.entity);
        }else{
            object=brandService.add($scope.entity);
        }
        object.success(
            function (response) {
                if (response.success){
                    $scope.reloadList();
                } else {
                    alert(response.message)
                }
            })
    };
    //查询一个
    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    };
    //删除
    $scope.delete=function () {
        brandService.delete($scope.selectIds).success(
            function (response) {
                if (response.success){
                    $scope.reloadList();
                } else {
                    alert(response.message)
                }
            })
    };
    //条件查询
    $scope.search=function(page,size){
        brandService.search(page,size,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }


});
