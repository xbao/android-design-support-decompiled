package android.support.design.widget;

import android.os.Build.VERSION;
import android.support.design.widget.ValueAnimatorCompat;
import android.support.design.widget.ValueAnimatorCompatImplEclairMr1;
import android.support.design.widget.ValueAnimatorCompatImplHoneycombMr1;
import android.support.design.widget.ViewUtilsLollipop;
import android.view.View;

class ViewUtils {
   static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR = new ValueAnimatorCompat.Creator() {
      public ValueAnimatorCompat createAnimator() {
         return new ValueAnimatorCompat((ValueAnimatorCompat.Impl)(VERSION.SDK_INT >= 12?new ValueAnimatorCompatImplHoneycombMr1():new ValueAnimatorCompatImplEclairMr1()));
      }
   };
   private static final ViewUtils.ViewUtilsImpl IMPL;

   static void setBoundsViewOutlineProvider(View view) {
      IMPL.setBoundsViewOutlineProvider(view);
   }

   static ValueAnimatorCompat createAnimator() {
      return DEFAULT_ANIMATOR_CREATOR.createAnimator();
   }

   static {
      int version = VERSION.SDK_INT;
      if(version >= 21) {
         IMPL = new ViewUtils.ViewUtilsImplLollipop(null);
      } else {
         IMPL = new ViewUtils.ViewUtilsImplBase(null);
      }

   }

   private static class ViewUtilsImplLollipop implements ViewUtils.ViewUtilsImpl {
      private ViewUtilsImplLollipop() {
      }

      public void setBoundsViewOutlineProvider(View view) {
         ViewUtilsLollipop.setBoundsViewOutlineProvider(view);
      }

      // $FF: synthetic method
      ViewUtilsImplLollipop(Object x0) {
         this();
      }
   }

   private static class ViewUtilsImplBase implements ViewUtils.ViewUtilsImpl {
      private ViewUtilsImplBase() {
      }

      public void setBoundsViewOutlineProvider(View view) {
      }

      // $FF: synthetic method
      ViewUtilsImplBase(Object x0) {
         this();
      }
   }

   private interface ViewUtilsImpl {
      void setBoundsViewOutlineProvider(View var1);
   }
}
