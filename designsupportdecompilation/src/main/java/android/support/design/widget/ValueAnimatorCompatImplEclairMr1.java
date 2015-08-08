package android.support.design.widget;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.design.widget.AnimationUtils;
import android.support.design.widget.ValueAnimatorCompat;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

class ValueAnimatorCompatImplEclairMr1 extends ValueAnimatorCompat.Impl {
   private static final int HANDLER_DELAY = 10;
   private static final int DEFAULT_DURATION = 200;
   private static final Handler sHandler = new Handler(Looper.getMainLooper());
   private long mStartTime;
   private boolean mIsRunning;
   private final int[] mIntValues = new int[2];
   private final float[] mFloatValues = new float[2];
   private int mDuration = 200;
   private Interpolator mInterpolator;
   private ValueAnimatorCompat.Impl.AnimatorListenerProxy mListener;
   private ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy mUpdateListener;
   private float mAnimatedFraction;
   private final Runnable mRunnable = new Runnable() {
      public void run() {
         ValueAnimatorCompatImplEclairMr1.this.update();
      }
   };

   public void start() {
      if(!this.mIsRunning) {
         if(this.mInterpolator == null) {
            this.mInterpolator = new AccelerateDecelerateInterpolator();
         }

         this.mStartTime = SystemClock.uptimeMillis();
         this.mIsRunning = true;
         if(this.mListener != null) {
            this.mListener.onAnimationStart();
         }

         sHandler.postDelayed(this.mRunnable, 10L);
      }
   }

   public boolean isRunning() {
      return this.mIsRunning;
   }

   public void setInterpolator(Interpolator interpolator) {
      this.mInterpolator = interpolator;
   }

   public void setListener(ValueAnimatorCompat.Impl.AnimatorListenerProxy listener) {
      this.mListener = listener;
   }

   public void setUpdateListener(ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy updateListener) {
      this.mUpdateListener = updateListener;
   }

   public void setIntValues(int from, int to) {
      this.mIntValues[0] = from;
      this.mIntValues[1] = to;
   }

   public int getAnimatedIntValue() {
      return AnimationUtils.lerp(this.mIntValues[0], this.mIntValues[1], this.getAnimatedFraction());
   }

   public void setFloatValues(float from, float to) {
      this.mFloatValues[0] = from;
      this.mFloatValues[1] = to;
   }

   public float getAnimatedFloatValue() {
      return AnimationUtils.lerp(this.mFloatValues[0], this.mFloatValues[1], this.getAnimatedFraction());
   }

   public void setDuration(int duration) {
      this.mDuration = duration;
   }

   public void cancel() {
      this.mIsRunning = false;
      sHandler.removeCallbacks(this.mRunnable);
      if(this.mListener != null) {
         this.mListener.onAnimationCancel();
      }

   }

   public float getAnimatedFraction() {
      return this.mAnimatedFraction;
   }

   public void end() {
      if(this.mIsRunning) {
         this.mIsRunning = false;
         sHandler.removeCallbacks(this.mRunnable);
         this.mAnimatedFraction = 1.0F;
         if(this.mUpdateListener != null) {
            this.mUpdateListener.onAnimationUpdate();
         }

         if(this.mListener != null) {
            this.mListener.onAnimationEnd();
         }
      }

   }

   private void update() {
      if(this.mIsRunning) {
         long elapsed = SystemClock.uptimeMillis() - this.mStartTime;
         float linearFraction = (float)elapsed / (float)this.mDuration;
         this.mAnimatedFraction = this.mInterpolator != null?this.mInterpolator.getInterpolation(linearFraction):linearFraction;
         if(this.mUpdateListener != null) {
            this.mUpdateListener.onAnimationUpdate();
         }

         if(SystemClock.uptimeMillis() >= this.mStartTime + (long)this.mDuration) {
            this.mIsRunning = false;
            if(this.mListener != null) {
               this.mListener.onAnimationEnd();
            }
         }
      }

      if(this.mIsRunning) {
         sHandler.postDelayed(this.mRunnable, 10L);
      }

   }
}
