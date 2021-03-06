(function() {
    'use strict';

    angular
        .module('colegioEsplanadaApp')
        .controller('TurmaDialogController', TurmaDialogController);

    TurmaDialogController.$inject = ['$timeout', '$scope', '$stateParams', 'User', 'Turma', '$state', 'TurmaAluno'];

    function TurmaDialogController ($timeout, $scope, $stateParams, User, Turma, $state, TurmaAluno) {
        var vm = this;

        vm.save = save;
        vm.disciplinas = ['Artes', 'Biologia', 'Educação Física', 'Educação Religiosa', 'Espanhol', 'Filosofia', 'Física', 'Geografia', 'História', 'Inglês', 'Língua Portuguesa', 'Literatura', 'Matemática', 'Química', 'Sociologia'];
        vm.usuarios = [];
        
        
        $scope.gridUsuarios = {};
		
		$scope.gridUsuarios.columnDefs = [
										 {name:'Ação', cellTemplate:'<button type="button" class="btn btn-default btn-sm" ng-click="grid.appScope.removeUser(row);"><span class="glyphicon glyphicon-remove-circle"></span></button>',	width: '100', enableFiltering: false},
										 {name:'Nome', field : 'firstName'},
										];
		var listaUsuario = [];
		
        $scope.listaUsuarioTratada = User.query(function(usuarios){
        	angular.forEach(usuarios, function(usuario){
        		if(usuario.authorities[0] == 'ROLE_USER'){
        			vm.usuarios.push(usuario);
        		}
        		
        		if(usuario.turma != undefined && $stateParams.id == usuario.turma.id){
        			listaUsuario.push(usuario);
           	 	}
        	});
        	
        	$scope.gridUsuarios.data = listaUsuario;
        	for(var i = 0; i < vm.usuarios.length; i++){
        		angular.forEach(listaUsuario, function(usuario){
        			if(usuario.id == vm.usuarios[i].id){
        				vm.usuarios.splice([i], 1);
        			}
        		});
        	}
        });
        
        if($state.current.name == 'turma.edit'){
        	Turma.get({id: $stateParams.id}, function(result) {
                vm.turma = result;
            });
        }
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function save () {
            vm.isSaving = true;
            var int = 0;
            angular.forEach(vm.turma.usuarios, function(user){
            	var usuario = {};
            	usuario.id = user;
            	vm.turma.usuarios[int] = usuario;
            	int++;
            });
            
            if (vm.turma.id !== null) {
            	vm.turma.usuarios = $scope.gridUsuarios.data;
                Turma.update(vm.turma, onSaveSuccess, onSaveError);
            } else {
                Turma.save(vm.turma, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('colegioEsplanadaApp:turmaUpdate', result);
            vm.isSaving = false;
            $state.go('turma');
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        $scope.removeUser = function(usuario){
        	for(var i = 0; i < $scope.gridUsuarios.data.length; i++){
        		if($scope.gridUsuarios.data[i].id == usuario.entity.id){
        			$scope.gridUsuarios.data.splice([i], 1);
        			vm.usuarios.push(usuario.entity);
        		}
        	}
        }
        
        $scope.addUser = function(){
        	angular.forEach(vm.turma.usuarios, function(usuarioSelecionado){
        		angular.forEach(vm.usuarios, function(usuarioLista){
        			if(usuarioLista.id == usuarioSelecionado){
        				$scope.gridUsuarios.data.push(usuarioLista);
        			}
        		});
        	});
        	
        	for(var i = 0; i < vm.usuarios.length; i++){
        		angular.forEach($scope.gridUsuarios.data, function(usuarioGrid){
        			if(usuarioGrid.id == vm.usuarios[i].id){
        				vm.usuarios.splice([i], 1);
        			}
        		});
        	}
        }
    }
})();
