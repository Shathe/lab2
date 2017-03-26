var ws;
var client;
var subscription;

$(document).ready(function() {
	initConn();
	checkBox = document.getElementById('history');
    checkBox.checked = false;
});

var app = angular.module('myApp', []);
app.controller('myCtrl', function($scope) {
	// Inicializar los tweets
	$scope.tweets = [];
	// si se pide una busqueda
	this.getTweets = function() {
		if (subscription != undefined) {
			// si ya estabas subscrito a anteriores busquedas te desuscribes
			// (solo una abierta a la vez)
			subscription.unsubscribe();
			$scope.tweets = [];
		}
		if(document.getElementById('history').checked){
  		    console.log("history checked");

	        $.get('/search',{q: $("#q").val(), restriccion: $("#restriccion").val(), dificultad: $("#dificultad").val()})
	        .done(function(data, status) {
	  		    console.log(data);
	            $scope.tweets=data;
	            $scope.$apply();
	        })
	        .fail(function(data, status) {
	          console.log(data);
	        });
	        
		}else{
  		    console.log("history NO checked");

			var query = $("#q").val();
			var dificultad = $("#dificultad").val();
			var restriccion = $("#restriccion").val();
			claveSubscripcion = query+"-"+dificultad+"-"+restriccion;

			client.send("/app/search", {}, claveSubscripcion);
			// Te subscribes a la busqueda. La funcion se ejecuta cuando el servidor
			// envia algo por la cola subscrita

			console.log(claveSubscripcion);
			subscription = client.subscribe("/queue/search/" +  claveSubscripcion, function(
					mensaje) {
				var data = JSON.parse(mensaje.body);
				$scope.tweets.unshift(data);
				$scope.$apply();
			}, {
				id : query
			});
		}
		

	};
});

/*
 * Iniciarlizar la conexion con el servidor con el endpoint (del websocket)
 * llamado twitter
 */
function initConn() {
	ws = new SockJS("/twitter");
	client = Stomp.over(ws);
	var headers = {};

	var connect_callback = function() {
		// called back after the client is connected and authenticated to the
		// STOMP server
	};
	var error_callback = function(error) {
		// display the error's message header:
		console.log(error);
	};
	client.connect(headers, connect_callback, error_callback);
}
