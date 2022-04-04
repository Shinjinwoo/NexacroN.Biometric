package com.example.androidruntime;

import android.content.Intent;
import android.os.Bundle;

import com.nexacro.NexacroActivity;
import com.tobeosft.plugin.biometricobject.BiometricObject;
import com.tobeosft.plugin.biometricobject.pluginnterface.BiometricInterface;
import com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject;
import com.tobesoft.plugin.mediaplayerobject.plugininterface.MediaPlayerInterface;

public class NexacroActivityExt extends NexacroActivity implements BiometricInterface, MediaPlayerInterface {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* BioMetric 연동 코드 ****************************************************************************/

    private BiometricObject mBiometricObject;

    @Override
    public void setBiometricObject(BiometricObject obj) {
        this.mBiometricObject = obj;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (mBiometricObject.isActivityResult(requestCode)) {
            if (mBiometricObject != null) {
                mBiometricObject.onActivityResult(requestCode, resultCode, intent);
            }
        } else if (mMediaPlayerObject.isActivityResult(requestCode)) {
            if (mMediaPlayerObject != null) {
                mMediaPlayerObject.onActivityResult(requestCode, resultCode, intent);
            }
        }
    }

    private MediaPlayerObject mMediaPlayerObject;

    @Override
    public void setMediaplayerObject(MediaPlayerObject mediaplayerObject) {
        this.mMediaPlayerObject = mediaplayerObject;
    }
    /* BioMetric 연동 코드 ****************************************************************************/

}
