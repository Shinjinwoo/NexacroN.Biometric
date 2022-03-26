
//==============================================================================
//Biometric
//==============================================================================

//==============================================================================
//nexacro.Event.BiometricEventInfo
//Biometric에 요청된 작업이 성공했을 때 발생되는 이벤트에서 사용되는 EventInfo Object
//==============================================================================

if(!nexacro.Event.BiometricEventInfo)
{
    nexacro.Event.BiometricEventInfo = function (strEventId, strSvcId, intReason, strReturnValue)
    {
        this.eventid = strEventId;                                              // 이벤트ID
        this.svcid = strSvcId;                                                  // 이벤트 서비스 ID
        this.reason = intReason;                                                // 이벤트 발생분류 코드
        this.returnvalue = strReturnValue;                                      // 이벤트 수행결과 (type:Variant)
    }
    _pBiometricEventInfo = nexacro.Event.BiometricEventInfo.prototype = nexacro._createPrototype(nexacro.Event);
    _pBiometricEventInfo._type = "nexacroBiometricEventInfo";
    _pBiometricEventInfo._type_name = "BiometricEventInfo";
    _pBiometricEventInfo = null;
}

//==============================================================================
//nexacro.Event.BiometricErrorEventInfo
//Biometric에 요청된 작업이 실패했을 때 발생되는 이벤트에서 사용되는 EventInfo Object
//==============================================================================
if(!nexacro.Event.BiometricErrorEventInfo)
{
    nexacro.Event.BiometricErrorEventInfo = function (strEventId, strSvcId, intReason, intErrorCode, strErrorMsg)
    {
        this.eventid = strEventId;                                              // 이벤트ID
        this.svcid = strSvcId;                                                  // 이벤트 서비스 ID
        this.reason = intReason;
        this.errorcode = intErrorCode;
        this.errormsg = strErrorMsg;

    }
    _pBiometricErrorEventInfo = nexacro.Event.BiometricErrorEventInfo.prototype = nexacro._createPrototype(nexacro.Event);
    _pBiometricErrorEventInfo._type = "nexacroBiometricErrorEventInfo";
    _pBiometricErrorEventInfo._type_name = "BiometricErrorEventInfo";
    _pBiometricErrorEventInfo = null;
}

//==============================================================================
//nexacro.Biometric
//Biometric를 연동하기 위해 사용한다.
//==============================================================================
if (!nexacro.Biometric)
{
    nexacro.Biometric = function(name, obj)
    {
        this._id = nexacro.Device.makeID();
        nexacro.Device._userCreatedObj[this._id] = this;
        this.name = name || "";

        this.enableevent = true;

        this.timeout = 10;

        this._clsnm = ["Biometric"];
        this._reasoncode = {
            constructor : {ifcls: 0, fn: "constructor"},
            destroy     : {ifcls: 0, fn: "destroy"},

            callMethod  : {ifcls: 0, fn: "callMethod"},
        };

        this._event_list = {
            "oncallback": 1,
            "onpageload": 1,
        };

        // native constructor
        var params = {} ;
        var fninfo = this._reasoncode.constructor;
        this._execFn(fninfo, params);
    };

    var _pBiometric = nexacro.Biometric.prototype = nexacro._createPrototype(nexacro._EventSinkObject);

    _pBiometric._type = "nexacroBiometric";
    _pBiometric._type_name = "Biometric";

    _pBiometric.destroy = function()
    {
        var params = {};
        var jsonstr;

        delete nexacro.Device._userCreatedObj[this._id];

        var fninfo = this._reasoncode.destroy;
        this._execFn(fninfo, params);
        return true;
    };

    //===================User Method=========================//
    _pBiometric.callMethod = function(methodid, param)
    {
        var fninfo = this._reasoncode.callMethod;

        var params = {};

        params.serviceid = methodid;
        if(param === undefined || param == null) params.param = {};
        else params.param = param;

        this._execFn(fninfo, params);
    };

    //===================Native Call=========================//
    _pBiometric._execFn = function(_obj, _param)
    {
        if(nexacro.Device.curDevice == 0)
        {
            var jsonstr = this._getJSONStr(_obj, _param);
            this._log(jsonstr);
            nexacro.Device.exec(jsonstr);
        }
        else
        {
            var jsonstr = this._getJSONStr(_obj, _param);
            this._log(jsonstr);
            nexacro.Device.exec(jsonstr);
        }
    }

    _pBiometric._getJSONStr = function(_obj, _param)
    {
        var _id = this._id;
        var _clsnm = this._clsnm[_obj.ifcls];
        var _fnnm = _obj.fn;
        var value = {};
        value.id = _id;
        value.div = _clsnm;
        value.method = _fnnm;
        value.params = _param;

        return  JSON.stringify(value);
    }

    _pBiometric._log = function(arg)
    {
        if(trace) {
            trace(arg);
        }
    }


    //===================EVENT=========================//
    _pBiometric._oncallback = function(objData) {
        var e = new nexacro.Event.BiometricEventInfo("oncallback", objData.svcid, objData.reason, objData.returnvalue);
        this.$fire_oncallback(this, e);
    };
    _pBiometric.$fire_oncallback = function (objBiometric, eBiometricEventInfo) {
        if (this.oncallback && this.oncallback._has_handlers) {
            return this.oncallback._fireEvent(this, eBiometricEventInfo);
        }
        return true;
    };

    _pBiometric._onpageload = function(objData) {
        var e = new nexacro.Event.BiometricEventInfo("onpageload", objData.svcid, objData.reason, objData.returnvalue);
        this.$fire_onpageload(this, e);
    };
    _pBiometric.$fire_onpageload = function (objBiometric, eBiometricEventInfo) {
        if (this.onpageload && this.onpageload._has_handlers) {
            return this.onpageload._fireEvent(this, eBiometricEventInfo);
        }
        return true;
    };

    delete _pBiometric;
}