

angular.module('moviesServices', ['ngResource']).
  factory('Movie', function($resource) {
    return $resource('search/movies')
    });
  });
