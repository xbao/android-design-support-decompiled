package android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Animator.AnimatorListener;
import android.support.design.widget.AnimationUtils;
import android.support.design.widget.FloatingActionButtonEclairMr1;
import android.support.design.widget.ShadowViewDelegate;
import android.view.View;

class FloatingActionButtonHoneycombMr1 extends FloatingActionButtonEclairMr1 {
   private boolean mIsHiding;

   FloatingActionButtonHoneycombMr1(View view, ShadowViewDelegate shadowViewDelegate) {
      super(view, shadowViewDelegate);
   }

   void hide() {
      if(!this.mIsHiding) {
         this.mView.animate().scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setDuration(200L).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
               FloatingActionButtonHoneycombMr1.this.mIsHiding = true;
            }

            public void onAnimationCancel(Animator animation) {
               FloatingActionButtonHoneycombMr1.this.mIsHiding = false;
            }

            public void onAnimationEnd(Animator animation) {
               FloatingActionButtonHoneycombMr1.this.mIsHiding = false;
               FloatingActionButtonHoneycombMr1.this.mView.setVisibility(8);
            }
         });
      }
   }

   void show() {
      this.mView.animate().scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setDuration(200L).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener((AnimatorListener)null);
   }
}
