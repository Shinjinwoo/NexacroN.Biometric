package com.tobesoft.plugin.mediaplayerobject;

import static com.tobesoft.plugin.plugincommonlib.info.ActivityRequest.BIOMETRIC_ACTIVITY_REQUEST;

import android.content.Intent;
import android.os.Build;

import com.nexacro.NexacroActivity;
import com.nexacro.plugin.NexacroPlugin;
import com.tobesoft.plugin.mediaplayerobject.plugininterface.MediaPlayerInterface;

import org.json.JSONObject;

public class MediaPlayerObject extends NexacroPlugin {

    private final String LOG_TAG = getClass().getSimpleName();

    private static final String SVCID = "svcid";
    private static final String REASON = "reason";
    private static final String RETVAL = "returnvalue";

    private static final String CALL_BACK = "_oncallback";
    private static final String PAGE_LOAD = "_onpageload";

    private static final String METHOD_CALLMETHOD = "callMethod";

    public static final int CODE_SUCCES = 0;
    public static final int CODE_ERROR = -1;

    public static final int MEDIAPLAYER_ACTIVITY_REQUEST = 123123123;

    public String mServiceId = "";

    MediaPlayerInterface mMediaPlayerInterface;
    NexacroActivity mActivity;


    public MediaPlayerObject(String objectId) {
        super(objectId);

        mMediaPlayerInterface = (MediaPlayerInterface) NexacroActivity.getInstance();
        mMediaPlayerInterface.setMediaplayerObject(this);
        mActivity = NexacroActivity.getInstance();
    }

    @Override
    public void init(JSONObject paramObject) {

    }

    @Override
    public void release(JSONObject paramObject) {

    }

    @Override
    public void execute(String method, JSONObject paramObject) {
        mServiceId = "";

        if (method.equals(METHOD_CALLMETHOD)) {
            try {
                JSONObject params = paramObject.getJSONObject("params");

                mServiceId = params.getString("serviceid");

                if (mServiceId.equals("biometricOpen")) {

                    Intent intent = new Intent(mActivity, MediaPlayerActivity.class);
                    //intent.putExtras(extraParam);

                    mActivity.startActivityForResult(intent,MEDIAPLAYER_ACTIVITY_REQUEST);
                }
            } catch (Exception e) {
                send(CODE_ERROR, METHOD_CALLMETHOD + ":" + e.getMessage());
            }
        }
    }

    public boolean send(int reason, Object retval) {

        return send(mServiceId, CALL_BACK, reason, retval);
    }

    public boolean send(String svcid, String callMethod, int reason, Object retval) {

        JSONObject obj = new JSONObject();

        try {
            if (mServiceId != null) {
                obj.put(SVCID, svcid);
                obj.put(REASON, reason);
                obj.put(RETVAL, retval);

                callback(callMethod, obj);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }


    public static boolean isActivityResult(int requestCode) {
        boolean result = false;
        switch (requestCode) {
            case MEDIAPLAYER_ACTIVITY_REQUEST:
                result = true;
                break;
        }
        return result;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == MEDIAPLAYER_ACTIVITY_REQUEST) {
            if (intent != null) {

            }
        }
    }


}
