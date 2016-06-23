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

package im.ene.lab.toro.player.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.Media;
import im.ene.lab.toro.media.OnPlayerStateChangeListener;
import im.ene.lab.toro.player.R;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eneim on 6/23/16.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)  //
public class FloppyVideoView extends FrameLayout implements Cineer.Player {

  private static final String TAG = "FloppyVideoView";

  /**
   * Options for scaling the bounds of an image to the bounds of this view.
   */
  public enum ScaleType {
    FIT_XY(1),

    FIT_START(2),

    FIT_CENTER(3),

    FIT_END(4),

    CENTER(5),

    CENTER_CROP(6),

    CENTER_INSIDE(7);

    ScaleType(int ni) {
      nativeInt = ni;
    }

    final int nativeInt;

    // cache scale types
    private static final Map<Integer, ScaleType> scaleTypes;

    static {
      scaleTypes = new HashMap<>();
      for (ScaleType screen : ScaleType.values()) {
        scaleTypes.put(screen.nativeInt, screen);
      }
    }

    public static ScaleType lookup(int nativeInt) {
      return scaleTypes.get(nativeInt);
    }
  }

  public FloppyVideoView(Context context) {
    this(context, null);
  }

  public FloppyVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public FloppyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public FloppyVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs, defStyleAttr);
  }

  private TextureView textureView;
  private Cineer.VideoPlayer playerDelegate;

  private void init(Context context, AttributeSet attrs, int defStyleAttr) {
    int layoutId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
        ? R.layout.tr_player_widget_floppy_video : R.layout.tr_player_widget_floppy_video_legacy;
    inflate(context, layoutId, this);
    if (getChildCount() > 0) {
      textureView = (TextureView) getChildAt(0);
    }
    if (textureView != null && textureView instanceof Cineer.Player) {
      playerDelegate = (Cineer.VideoPlayer) textureView;
    } else {
      throw new IllegalArgumentException("Video playback View must implement Cineer.Player");
    }

    Log.i(TAG, "init: " + textureView.getClass().getSimpleName());
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloppyVideoView);

    try {
      mRetainRatio = typedArray.getBoolean(R.styleable.FloppyVideoView_retainRatio, true);
      int scaleType = typedArray.getInt(R.styleable.FloppyVideoView_scaleType, 1);
      mScaleType = ScaleType.lookup(scaleType);
    } finally {
      typedArray.recycle();
    }

    mMatrix = new Matrix();

    // trick to correctly measure video
    if (ViewCompat.getMinimumHeight(this) == 0) {
      setMinimumHeight(1);
    }

    // trick to correctly measure video
    if (ViewCompat.getMinimumWidth(this) == 0) {
      setMinimumWidth(1);
    }
  }

  @Override public void start() {
    playerDelegate.start();
  }

  @Override public void pause() {
    playerDelegate.pause();
  }

  @Override public void stop() {
    playerDelegate.stop();
  }

  @Override public long getDuration() {
    return playerDelegate.getDuration();
  }

  @Override public long getCurrentPosition() {
    return playerDelegate.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    playerDelegate.seekTo(pos);
  }

  @Override public boolean isPlaying() {
    return playerDelegate.isPlaying();
  }

  @Override public int getBufferPercentage() {
    return playerDelegate.getBufferPercentage();
  }

  @Override public int getAudioSessionId() {
    return playerDelegate.getAudioSessionId();
  }

  @Override public void setBackgroundAudioEnabled(boolean enabled) {
    playerDelegate.setBackgroundAudioEnabled(enabled);
  }

  @Override public void setMedia(@NonNull Media source) {
    playerDelegate.setMedia(source);
  }

  @Override public void setMedia(Uri uri) {
    playerDelegate.setMedia(uri);
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    playerDelegate.setVolume(volume);
  }

  @Override public void setOnPlayerStateChangeListener(OnPlayerStateChangeListener listener) {
    playerDelegate.setOnPlayerStateChangeListener(listener);
  }

  /* Custom implementation for ScaleType */

  @NonNull private Matrix mMatrix;
  @NonNull private ScaleType mScaleType;
  private boolean mRetainRatio = true;

  /**
   * Controls how the image should be resized or moved to match the size
   * of this ImageView.
   *
   * @param scaleType The desired scaling mode.
   */
  public void setScaleType(ScaleType scaleType) {
    if (scaleType == null) {
      throw new NullPointerException();
    }

    if (mScaleType != scaleType) {
      mScaleType = scaleType;

      setWillNotCacheDrawing(mScaleType == ScaleType.CENTER);

      requestLayout();
      updateScaleType();
      invalidate();
    }
  }

  /**
   * Return the current scale type in use by this ImageView.
   *
   * @see ScaleType
   */
  public ScaleType getScaleType() {
    return mScaleType;
  }

  private void updateScaleType() {
    boolean hasChanged = false;
    if (ScaleType.CENTER_CROP == mScaleType) {

      float scaleX = (float) getWidth() / (float) playerDelegate.getVideoWidth();
      float scaleY = (float) getHeight() / (float) playerDelegate.getVideoHeight();
      float maxScale = Math.max(scaleX, scaleY);
      scaleX = maxScale / scaleX;
      scaleY = maxScale / scaleY;
      mMatrix.setScale(scaleX, scaleY, getWidth() / 2, getHeight() / 2);
      hasChanged = true;
    } else if (ScaleType.CENTER_INSIDE == mScaleType) {

      float scaleX = (float) playerDelegate.getVideoWidth() / (float) getWidth();
      float scaleY = (float) playerDelegate.getVideoHeight() / (float) getHeight();
      float maxScale = Math.max(scaleX, scaleY);
      if (maxScale > 1) {
        scaleX = scaleX / maxScale;
        scaleY = scaleY / maxScale;
      }
      mMatrix.setScale(scaleX, scaleY, getWidth() / 2, getHeight() / 2);
      hasChanged = true;
    } else if (ScaleType.CENTER == mScaleType) {

      float sx = (float) playerDelegate.getVideoWidth() / (float) getWidth();
      float sy = (float) playerDelegate.getVideoHeight() / (float) getHeight();
      mMatrix.setScale(sx, sy, getWidth() / 2, getHeight() / 2);
      hasChanged = true;
    } else if (ScaleType.FIT_XY == mScaleType) {

      mMatrix.setScale(1, 1, 0, 0);
      hasChanged = true;
    } else if (ScaleType.FIT_START == mScaleType) {

      float scaleX = (float) getWidth() / playerDelegate.getVideoWidth();
      float scaleY = (float) getHeight() / playerDelegate.getVideoHeight();
      float minScale = Math.min(scaleX, scaleY);
      scaleX = minScale / scaleX;
      scaleY = minScale / scaleY;
      mMatrix.setScale(scaleX, scaleY, 0, 0);
      hasChanged = true;
    } else if (ScaleType.FIT_END == mScaleType) {

      float scaleX = (float) getWidth() / playerDelegate.getVideoWidth();
      float scaleY = (float) getHeight() / playerDelegate.getVideoHeight();
      float minScale = Math.min(scaleX, scaleY);
      scaleX = minScale / scaleX;
      scaleY = minScale / scaleY;
      mMatrix.setScale(scaleX, scaleY, getWidth(), getHeight());
      hasChanged = true;
    } else if (ScaleType.FIT_CENTER == mScaleType) {

      float scaleX = (float) getWidth() / (float) playerDelegate.getVideoWidth();
      float scaleY = (float) getHeight() / (float) playerDelegate.getVideoHeight();
      float minScale = Math.min(scaleX, scaleY);
      scaleX = minScale / scaleX;
      scaleY = minScale / scaleY;
      mMatrix.setScale(scaleX, scaleY, getWidth() / 2, getHeight() / 2);
      hasChanged = true;
    }

    if (hasChanged) {
      textureView.setTransform(mMatrix);
    }
  }

  @Override public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setClassName(FloppyVideoView.class.getName());
  }

  @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(FloppyVideoView.class.getName());
  }
}
