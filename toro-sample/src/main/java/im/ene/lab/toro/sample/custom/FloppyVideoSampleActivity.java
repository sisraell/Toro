/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.lab.toro.sample.custom;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.OnPlayerStateChangeListener;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.State;
import im.ene.lab.toro.player.widget.FloppyVideoView;
import im.ene.lab.toro.sample.R;

/**
 * Created by eneim on 6/23/16.
 */
public class FloppyVideoSampleActivity extends AppCompatActivity {

  private FloppyVideoView videoView;
  private Button changeScaleTypeButton;
  private Button changeVideo;

  private final String[] VIDEOS = new String[] {
      "https://r5---sn-a5m7ln76.c.docs.google.com/videoplayback?requiressl=yes&id=be8f2b6f12962493&itag=59&source=webdrive&ttl=transient&app=texmex&ip=124.33.192.254&ipbits=8&expire=1466674004&sparams=expire,id,ip,ipbits,itag,mm,mn,ms,mv,nh,pl,requiressl,source,ttl&signature=42B55503F256CEA47F6FBACD68F8D089D8B94C5F.01649C48E06DE87B1E14AF49A22A161A83F082E7&key=cms1&pl=17&cpn=ZPLidLA-BtyNX9om&c=WEB&cver=1.20160622&redirect_counter=1&req_id=c232ebf92a9636e2&cms_redirect=yes&mm=34&mn=sn-a5m7ln76&ms=ltu&mt=1466659601&mv=m&nh=IgpwcjAyLm5ydDE5KgkxMjcuMC4wLjE",
      // "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
      "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd"
  };

  int currentVideoIndex = 0;

  final FloppyVideoView.ScaleType[] SCALE_TYPES = {
      FloppyVideoView.ScaleType.FIT_CENTER, FloppyVideoView.ScaleType.FIT_XY,
      FloppyVideoView.ScaleType.FIT_START, FloppyVideoView.ScaleType.FIT_END,
      FloppyVideoView.ScaleType.CENTER, FloppyVideoView.ScaleType.CENTER_CROP,
      FloppyVideoView.ScaleType.CENTER_INSIDE
  };

  int scaleTypeIndex = 0;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_floppy_video);
    videoView = (FloppyVideoView) findViewById(R.id.video_view);
    changeScaleTypeButton = (Button) findViewById(R.id.scale_button);
    changeVideo = (Button) findViewById(R.id.change_video);

    videoView.setScaleType(SCALE_TYPES[scaleTypeIndex % SCALE_TYPES.length]);

    changeScaleTypeButton.setText("SCALE TYPE: " + videoView.getScaleType().name());
    changeScaleTypeButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        videoView.setScaleType(SCALE_TYPES[++scaleTypeIndex % SCALE_TYPES.length]);
        changeScaleTypeButton.setText("SCALE TYPE: " + videoView.getScaleType().name());
      }
    });

    changeVideo.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        currentVideoIndex ++;
        currentVideoIndex = currentVideoIndex % 2;
        videoView.setMedia(Uri.parse(VIDEOS[currentVideoIndex]));
        videoView.start();
      }
    });

    videoView.setOnPlayerStateChangeListener(new OnPlayerStateChangeListener() {
      @Override public void onPlayerStateChanged(Cineer player, boolean playWhenReady,
          @State int playbackState) {
        if (playbackState == Cineer.PLAYER_PREPARED) {
          videoView.start();
        }
      }

      @Override public boolean onPlayerError(Cineer player, PlaybackException error) {
        return false;
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    videoView.setMedia(Uri.parse(VIDEOS[currentVideoIndex]));
    videoView.start();
  }

  @Override protected void onPause() {
    super.onPause();
    videoView.stop();
  }
}
