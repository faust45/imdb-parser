var app = angular.module('imdb-core', ['ui.bootstrap', 'ngResource']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.
      when('/search/:q', {controller: UsersCtrl})
  }]);



function TypeaheadCtrl($scope) {
  $scope.selected = undefined;
  $scope.states = ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California', 'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia', 'Hawaii', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana', 'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire', 'New Jersey', 'New Mexico', 'New York', 'North Dakota', 'North Carolina', 'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island', 'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont', 'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'];
}

function UsersCtrl($scope, $routeParams, $http) {
    $scope.smartSearch = function(sortType) {
        sendReq();
        function sendReq() {
            $http.get('/search/movies?order=' + sortType + '&q=' + $scope.typeaheadValue).success(function(data) {
                $scope.movies = data;
            });
        }
    };

  $scope.$watch('typeaheadValue',function(newVal,oldVal){
    clearTimeout($scope.typingTimer);
    $scope.typingTimer = setTimeout(sendReq, 500);

    function sendReq() {
        if (newVal && newVal.length > 1) {
            $http.get('/search/movies?q='+newVal).success(function(data) {
                $scope.movies = data;
            });
        }
    }
  });
}
