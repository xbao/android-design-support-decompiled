package android.support.design.widget;

import android.util.StateSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

final class StateListAnimator {
   private final ArrayList mTuples = new ArrayList();
   private StateListAnimator.Tuple mLastMatch = null;
   private Animation mRunningAnimation = null;
   private WeakReference mViewRef;
   private AnimationListener mAnimationListener = new AnimationListener() {
      public void onAnimationEnd(Animation animation) {
         if(StateListAnimator.this.mRunningAnimation == animation) {
            StateListAnimator.this.mRunningAnimation = null;
         }

      }

      public void onAnimationStart(Animation animation) {
      }

      public void onAnimationRepeat(Animation animation) {
      }
   };

   public void addState(int[] specs, Animation animation) {
      StateListAnimator.Tuple tuple = new StateListAnimator.Tuple(specs, animation, null);
      animation.setAnimationListener(this.mAnimationListener);
      this.mTuples.add(tuple);
   }

   Animation getRunningAnimation() {
      return this.mRunningAnimation;
   }

   View getTarget() {
      return this.mViewRef == null?null:(View)this.mViewRef.get();
   }

   void setTarget(View view) {
      View current = this.getTarget();
      if(current != view) {
         if(current != null) {
            this.clearTarget();
         }

         if(view != null) {
            this.mViewRef = new WeakReference(view);
         }

      }
   }

   private void clearTarget() {
      View view = this.getTarget();
      int size = this.mTuples.size();

      for(int i = 0; i < size; ++i) {
         Animation anim = ((StateListAnimator.Tuple)this.mTuples.get(i)).mAnimation;
         if(view.getAnimation() == anim) {
            view.clearAnimation();
         }
      }

      this.mViewRef = null;
      this.mLastMatch = null;
      this.mRunningAnimation = null;
   }

   void setState(int[] state) {
      StateListAnimator.Tuple match = null;
      int count = this.mTuples.size();

      for(int i = 0; i < count; ++i) {
         StateListAnimator.Tuple tuple = (StateListAnimator.Tuple)this.mTuples.get(i);
         if(StateSet.stateSetMatches(tuple.mSpecs, state)) {
            match = tuple;
            break;
         }
      }

      if(match != this.mLastMatch) {
         if(this.mLastMatch != null) {
            this.cancel();
         }

         this.mLastMatch = match;
         if(match != null) {
            this.start(match);
         }

      }
   }

   private void start(StateListAnimator.Tuple match) {
      this.mRunningAnimation = match.mAnimation;
      View view = this.getTarget();
      if(view != null) {
         view.startAnimation(this.mRunningAnimation);
      }

   }

   private void cancel() {
      if(this.mRunningAnimation != null) {
         View view = this.getTarget();
         if(view != null && view.getAnimation() == this.mRunningAnimation) {
            view.clearAnimation();
         }

         this.mRunningAnimation = null;
      }

   }

   ArrayList getTuples() {
      return this.mTuples;
   }

   public void jumpToCurrentState() {
      if(this.mRunningAnimation != null) {
         View view = this.getTarget();
         if(view != null && view.getAnimation() == this.mRunningAnimation) {
            view.clearAnimation();
         }
      }

   }

   static class Tuple {
      final int[] mSpecs;
      final Animation mAnimation;

      private Tuple(int[] specs, Animation Animation) {
         this.mSpecs = specs;
         this.mAnimation = Animation;
      }

      int[] getSpecs() {
         return this.mSpecs;
      }

      Animation getAnimation() {
         return this.mAnimation;
      }

      // $FF: synthetic method
      Tuple(int[] x0, Animation x1, Object x2) {
         this(x0, x1);
      }
   }
}
