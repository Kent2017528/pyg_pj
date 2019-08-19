app.service('brandService',function ($http) {
    this.search=function (page,size,searchEntity) {
        return $http.post('../brand/search.do?page='+page +'&size='+size, searchEntity)
    };

    this.add=function (entity) {
        return $http.post('../brand/add.do',entity)
    };
    this.update=function (entity) {
        return $http.post('../brand/update.do',entity)
    };
    this.findOne=function (id) {
        return $http.get('../brand/findOne.do?id='+id)
    }
    this.delete=function (ids) {
        return $http.post('../brand/delete.do?ids='+ids)
    }

});