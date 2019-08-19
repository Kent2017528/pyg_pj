app.controller('brandController',function ($scope,$http,brandService) {
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    };
    //刷新列表
    $scope.reloadList=function(){
        $scope.search( $scope.paginationConf.currentPage ,  $scope.paginationConf.itemsPerPage );
    };

    $scope.selectIds=[];//用户勾选的ID集合
    //用户勾选复选框
    $scope.updateSelection=function($event,id){
        if($event.target.checked){
            $scope.selectIds.push(id);//push向集合添加元素
        }else{
            var index= $scope.selectIds.indexOf(id);//查找值的 位置
            $scope.selectIds.splice(index,1);//参数1：移除的位置 参数2：移除的个数
        }
    };
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
