package android.support.design.widget;

import android.view.animation.Interpolator;

class ValueAnimatorCompat {
   private final ValueAnimatorCompat.Impl mImpl;

   ValueAnimatorCompat(ValueAnimatorCompat.Impl impl) {
      this.mImpl = impl;
   }

   public void start() {
      this.mImpl.start();
   }

   public boolean isRunning() {
      return this.mImpl.isRunning();
   }

   public void setInterpolator(Interpolator interpolator) {
      this.mImpl.setInterpolator(interpolator);
   }

   public void setUpdateListener(final ValueAnimatorCompat.AnimatorUpdateListener updateListener) {
      if(updateListener != null) {
         this.mImpl.setUpdateListener(new ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy() {
            public void onAnimationUpdate() {
               updateListener.onAnimationUpdate(ValueAnimatorCompat.this);
            }
         });
      } else {
         this.mImpl.setUpdateListener((ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy)null);
      }

   }

   public void setListener(final ValueAnimatorCompat.AnimatorListener listener) {
      if(listener != null) {
         this.mImpl.setListener(new ValueAnimatorCompat.Impl.AnimatorListenerProxy() {
            public void onAnimationStart() {
               listener.onAnimationStart(ValueAnimatorCompat.this);
            }

            public void onAnimationEnd() {
               listener.onAnimationEnd(ValueAnimatorCompat.this);
            }

            public void onAnimationCancel() {
               listener.onAnimationCancel(ValueAnimatorCompat.this);
            }
         });
      } else {
         this.mImpl.setListener((ValueAnimatorCompat.Impl.AnimatorListenerProxy)null);
      }

   }

   public void setIntValues(int from, int to) {
      this.mImpl.setIntValues(from, to);
   }

   public int getAnimatedIntValue() {
      return this.mImpl.getAnimatedIntValue();
   }

   public void setFloatValues(float from, float to) {
      this.mImpl.setFloatValues(from, to);
   }

   public float getAnimatedFloatValue() {
      return this.mImpl.getAnimatedFloatValue();
   }

   public void setDuration(int duration) {
      this.mImpl.setDuration(duration);
   }

   public void cancel() {
      this.mImpl.cancel();
   }

   public float getAnimatedFraction() {
      return this.mImpl.getAnimatedFraction();
   }

   public void end() {
      this.mImpl.end();
   }

   abstract static class Impl {
      abstract void start();

      abstract boolean isRunning();

      abstract void setInterpolator(Interpolator var1);

      abstract void setListener(ValueAnimatorCompat.Impl.AnimatorListenerProxy var1);

      abstract void setUpdateListener(ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy var1);

      abstract void setIntValues(int var1, int var2);

      abstract int getAnimatedIntValue();

      abstract void setFloatValues(float var1, float var2);

      abstract float getAnimatedFloatValue();

      abstract void setDuration(int var1);

      abstract void cancel();

      abstract float getAnimatedFraction();

      abstract void end();

      interface AnimatorListenerProxy {
         void onAnimationStart();

         void onAnimationEnd();

         void onAnimationCancel();
      }

      interface AnimatorUpdateListenerProxy {
         void onAnimationUpdate();
      }
   }

   interface Creator {
      ValueAnimatorCompat createAnimator();
   }

   static class AnimatorListenerAdapter implements ValueAnimatorCompat.AnimatorListener {
      public void onAnimationStart(ValueAnimatorCompat animator) {
      }

      public void onAnimationEnd(ValueAnimatorCompat animator) {
      }

      public void onAnimationCancel(ValueAnimatorCompat animator) {
      }
   }

   interface AnimatorListener {
      void onAnimationStart(ValueAnimatorCompat var1);

      void onAnimationEnd(ValueAnimatorCompat var1);

      void onAnimationCancel(ValueAnimatorCompat var1);
   }

   interface AnimatorUpdateListener {
      void onAnimationUpdate(ValueAnimatorCompat var1);
   }
}
