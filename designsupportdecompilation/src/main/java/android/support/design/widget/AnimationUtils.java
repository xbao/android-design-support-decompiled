package android.support.design.widget;

import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Animation.AnimationListener;

class AnimationUtils {
   static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
   static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
   static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

   static float lerp(float startValue, float endValue, float fraction) {
      return startValue + fraction * (endValue - startValue);
   }

   static int lerp(int startValue, int endValue, float fraction) {
      return startValue + Math.round(fraction * (float)(endValue - startValue));
   }

   static class AnimationListenerAdapter implements AnimationListener {
      public void onAnimationStart(Animation animation) {
      }

      public void onAnimationEnd(Animation animation) {
      }

      public void onAnimationRepeat(Animation animation) {
      }
   }
}
