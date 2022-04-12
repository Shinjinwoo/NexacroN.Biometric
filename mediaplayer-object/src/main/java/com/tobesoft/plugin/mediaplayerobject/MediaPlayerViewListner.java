package com.tobesoft.plugin.mediaplayerobject;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

public class MediaPlayerViewListner implements TextureView.SurfaceTextureListener {

    Context mContext;
    String mVideoSource;

    MediaPlayerViewListner(Context context, String source) {
        this.mContext = context;
        this.mVideoSource = source;
    }

    @Override
    // 텍스쳐가 준비되면 불리는 메서드이다. 여기에서 초기화를 처리한다.
    public void onSurfaceTextureAvailable(
            SurfaceTexture surfaceTexture, int width, int height) {
        try {
            // 먼저 사용할 미디어 플레이어를 만든다.
            MediaPlayer mPlayer = new MediaPlayer();

            // 인자로 들어온 surfaceTexture 기반으로 Surface 하나 생성한다.
            Surface surface = new Surface(surfaceTexture);

            // 만들어진 Surface 미디어 플레이어에 세팅한다.
            mPlayer.setSurface(surface);

            // 미디어 플레이어의 비디오 소스를 세팅한다.
            // 이 경우에는 raw 디렉토리의 sample.mp4다. 참고로 .mp4는 생략하고 ~/sample로 접근한다.
            Uri uri = Uri.parse(
                    "android.resource://" + mContext.getPackageName() + "/raw/" + mVideoSource);
            mPlayer.setDataSource(mContext, uri);

            // 이제 미디어 플레이어를 준비시킨다.
            // 준비 함수는 prepare()와 prepareAsync()가 있는데 각각 동기, 비동기 버전이다.
            // 지금과 같은 로컬 파일의 경우 prepare()로 동기 처리를 해도 되지만,
            // 스트리밍을 할 때에는 반드시 prepareAsync()로 비동기 처리를 해야 한다.
            // 모든 경우에 그냥 비동기 처리를 하는 것이 좋은 습관이다. --> 과연 ?

            mPlayer.prepareAsync();

            // 준비가 끝나는 지점을 알기 위해 리스너를 등록한다.
            // 비디오뷰에서 썼던 것과 같은 리스너이다.
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 준비가 완료되면 비디오를 재생한다.
                    mp.start();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(
            SurfaceTexture surfaceTexture, int width, int height) {
        // 텍스쳐 사이즈가 변경되면 불린다. 여기에서는 처리하지 않는다.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        // 텍스쳐가 파괴되면 불린다. 여기에서는 처리하지 않는다.
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // 텍스쳐 업데이트가 일어나면 불린다. 미디어 플레이어가 계속 업데이트 중이므로 여기에서는 처리하지 않는다.
    }
}