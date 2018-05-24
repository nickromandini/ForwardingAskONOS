// js for sample app custom view
(function () {
    'use strict';

    // injected refs
    var $log, $scope, wss, ks, ds;

    var msgSent = false;


    // constants
    var dataReq = 'fwdaskCustomDataRequest',
        dataResp = 'fwdaskCustomDataResponse',
        dataNotification = 'fwdaskCustomDataNotification';
    
    var dialogId = 'fwdask-dialog',
    dialogOpts = {
        edge: 'left'
    };
    

    function createConfirmationText(data) {
        var content = ds.createDiv();
        content.append('p').text(data.message);
        return content;
    }
    
    function dOk () {
        if (msgSent) {
            wss.sendEvent(dataResp, {
                response: 'ok'
            });
            $scope.data.message = "";
            $scope.textArea = { "background-color" : "white" };
            $scope.$apply();
            msgSent = false;
        }
    }
    
    function dCancel () {
        if (msgSent) {
            wss.sendEvent(dataResp, {
                response: 'cancel'
            });
            $scope.data.message = "";
            $scope.textArea = { "background-color" : "white" };
            $scope.$apply();
            msgSent = false;
        }
    }

    function reqDataCb(data) {
		$scope.data = data;
        $scope.data.message.replace(/\\r\\n/g, "<br />");
		$scope.$apply();
        msgSent = true;
    }

    function notificationDataCb(data) {
        if (data.message.includes("ACCEPT")) {
        	$scope.textArea = { "background-color" : "lightGreen" };
        } else if (data.message.includes("DISCARD")) {
        	$scope.textArea = { "background-color" : "lightCoral" };
        }
        ds.openDialog(dialogId, dialogOpts)
        .setTitle('Notification')
        .addContent(createConfirmationText(data))
        .addOk()
        .bindKeys();
    }


    angular.module('ovFwdaskCustom', [])
        .controller('OvFwdaskCustomCtrl',
        ['$log', '$scope', 'WebSocketService', 'KeyService', 'DialogService',

        function (_$log_, _$scope_, _wss_, _ks_, _ds_) {
            $log = _$log_;
            $scope = _$scope_;
            wss = _wss_;
            ks = _ks_;
            ds = _ds_;

            var handlers = {};
            $scope.data = {};

            // data response handler
            handlers[dataReq] = reqDataCb;
            handlers[dataNotification] = notificationDataCb;
            wss.bindHandlers(handlers);

            $scope.dOk = dOk;
            $scope.dCancel = dCancel;

            // cleanup
            $scope.$on('$destroy', function () {
                wss.unbindHandlers(handlers);
                $log.log('OvFwdaskCustomCtrl has been destroyed');
            });

            $log.log('OvFwdaskCustomCtrl has been created');
        }]);
    
        

}());
