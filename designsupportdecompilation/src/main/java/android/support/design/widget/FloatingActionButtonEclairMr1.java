package android.support.design.widget;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import nz.xbc.designsupportdecompilation.R;
import nz.xbc.designsupportdecompilation.R.anim;
import android.support.design.widget.AnimationUtils;
import android.support.design.widget.FloatingActionButtonImpl;
import android.support.design.widget.ShadowDrawableWrapper;
import android.support.design.widget.ShadowViewDelegate;
import android.support.design.widget.StateListAnimator;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

class FloatingActionButtonEclairMr1 extends FloatingActionButtonImpl {
   private Drawable mShapeDrawable;
   private Drawable mRippleDrawable;
   private Drawable mBorderDrawable;
   private float mElevation;
   private float mPressedTranslationZ;
   private int mAnimationDuration;
   private StateListAnimator mStateListAnimator;
   ShadowDrawableWrapper mShadowDrawable;
   private boolean mIsHiding;

   FloatingActionButtonEclairMr1(View view, ShadowViewDelegate shadowViewDelegate) {
      super(view, shadowViewDelegate);
      this.mAnimationDuration = view.getResources().getInteger(17694720);
      this.mStateListAnimator = new StateListAnimator();
      this.mStateListAnimator.setTarget(view);
      this.mStateListAnimator.addState(PRESSED_ENABLED_STATE_SET, this.setupAnimation(new FloatingActionButtonEclairMr1.ElevateToTranslationZAnimation(null)));
      this.mStateListAnimator.addState(FOCUSED_ENABLED_STATE_SET, this.setupAnimation(new FloatingActionButtonEclairMr1.ElevateToTranslationZAnimation(null)));
      this.mStateListAnimator.addState(EMPTY_STATE_SET, this.setupAnimation(new FloatingActionButtonEclairMr1.ResetElevationAnimation(null)));
   }

   void setBackgroundDrawable(Drawable originalBackground, ColorStateList backgroundTint, Mode backgroundTintMode, int rippleColor, int borderWidth) {
      this.mShapeDrawable = DrawableCompat.wrap(originalBackground);
      DrawableCompat.setTintList(this.mShapeDrawable, backgroundTint);
      if(backgroundTintMode != null) {
         DrawableCompat.setTintMode(this.mShapeDrawable, backgroundTintMode);
      }

      GradientDrawable touchFeedbackShape = new GradientDrawable();
      touchFeedbackShape.setShape(1);
      touchFeedbackShape.setColor(-1);
      touchFeedbackShape.setCornerRadius(this.mShadowViewDelegate.getRadius());
      this.mRippleDrawable = DrawableCompat.wrap(touchFeedbackShape);
      DrawableCompat.setTintList(this.mRippleDrawable, createColorStateList(rippleColor));
      DrawableCompat.setTintMode(this.mRippleDrawable, Mode.MULTIPLY);
      Drawable[] layers;
      if(borderWidth > 0) {
         this.mBorderDrawable = this.createBorderDrawable(borderWidth, backgroundTint);
         layers = new Drawable[]{this.mBorderDrawable, this.mShapeDrawable, this.mRippleDrawable};
      } else {
         this.mBorderDrawable = null;
         layers = new Drawable[]{this.mShapeDrawable, this.mRippleDrawable};
      }

      this.mShadowDrawable = new ShadowDrawableWrapper(this.mView.getResources(), new LayerDrawable(layers), this.mShadowViewDelegate.getRadius(), this.mElevation, this.mElevation + this.mPressedTranslationZ);
      this.mShadowDrawable.setAddPaddingForCorners(false);
      this.mShadowViewDelegate.setBackgroundDrawable(this.mShadowDrawable);
      this.updatePadding();
   }

   void setBackgroundTintList(ColorStateList tint) {
      DrawableCompat.setTintList(this.mShapeDrawable, tint);
      if(this.mBorderDrawable != null) {
         DrawableCompat.setTintList(this.mBorderDrawable, tint);
      }

   }

   void setBackgroundTintMode(Mode tintMode) {
      DrawableCompat.setTintMode(this.mShapeDrawable, tintMode);
   }

   void setRippleColor(int rippleColor) {
      DrawableCompat.setTintList(this.mRippleDrawable, createColorStateList(rippleColor));
   }

   void setElevation(float elevation) {
      if(this.mElevation != elevation && this.mShadowDrawable != null) {
         this.mShadowDrawable.setShadowSize(elevation, elevation + this.mPressedTranslationZ);
         this.mElevation = elevation;
         this.updatePadding();
      }

   }

   void setPressedTranslationZ(float translationZ) {
      if(this.mPressedTranslationZ != translationZ && this.mShadowDrawable != null) {
         this.mPressedTranslationZ = translationZ;
         this.mShadowDrawable.setMaxShadowSize(this.mElevation + translationZ);
         this.updatePadding();
      }

   }

   void onDrawableStateChanged(int[] state) {
      this.mStateListAnimator.setState(state);
   }

   void jumpDrawableToCurrentState() {
      this.mStateListAnimator.jumpToCurrentState();
   }

   void hide() {
      if(!this.mIsHiding) {
         Animation anim = android.view.animation.AnimationUtils.loadAnimation(this.mView.getContext(), R.anim.fab_out);
         anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
         anim.setDuration(200L);
         anim.setAnimationListener(new AnimationUtils.AnimationListenerAdapter() {
            public void onAnimationStart(Animation animation) {
               FloatingActionButtonEclairMr1.this.mIsHiding = true;
            }

            public void onAnimationEnd(Animation animation) {
               FloatingActionButtonEclairMr1.this.mIsHiding = false;
               FloatingActionButtonEclairMr1.this.mView.setVisibility(8);
            }
         });
         this.mView.startAnimation(anim);
      }
   }

   void show() {
      Animation anim = android.view.animation.AnimationUtils.loadAnimation(this.mView.getContext(), R.anim.fab_in);
      anim.setDuration(200L);
      anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      this.mView.startAnimation(anim);
   }

   private void updatePadding() {
      Rect rect = new Rect();
      this.mShadowDrawable.getPadding(rect);
      this.mShadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
   }

   private Animation setupAnimation(Animation animation) {
      animation.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      animation.setDuration((long)this.mAnimationDuration);
      return animation;
   }

   private static ColorStateList createColorStateList(int selectedColor) {
      int[][] states = new int[3][];
      int[] colors = new int[3];
      byte i = 0;
      states[i] = FOCUSED_ENABLED_STATE_SET;
      colors[i] = selectedColor;
      int var4 = i + 1;
      states[var4] = PRESSED_ENABLED_STATE_SET;
      colors[var4] = selectedColor;
      ++var4;
      states[var4] = new int[0];
      colors[var4] = 0;
      ++var4;
      return new ColorStateList(states, colors);
   }

   private class ElevateToTranslationZAnimation extends FloatingActionButtonEclairMr1.BaseShadowAnimation {
      private ElevateToTranslationZAnimation() {
         super();
      }

      protected float getTargetShadowSize() {
         return FloatingActionButtonEclairMr1.this.mElevation + FloatingActionButtonEclairMr1.this.mPressedTranslationZ;
      }

      // $FF: synthetic method
      ElevateToTranslationZAnimation(Object x1) {
         this();
      }
   }

   private class ResetElevationAnimation extends FloatingActionButtonEclairMr1.BaseShadowAnimation {
      private ResetElevationAnimation() {
         super();
      }

      protected float getTargetShadowSize() {
         return FloatingActionButtonEclairMr1.this.mElevation;
      }

      // $FF: synthetic method
      ResetElevationAnimation(Object x1) {
         this();
      }
   }

   private abstract class BaseShadowAnimation extends Animation {
      private float mShadowSizeStart;
      private float mShadowSizeDiff;

      private BaseShadowAnimation() {
      }

      public void reset() {
         super.reset();
         this.mShadowSizeStart = FloatingActionButtonEclairMr1.this.mShadowDrawable.getShadowSize();
         this.mShadowSizeDiff = this.getTargetShadowSize() - this.mShadowSizeStart;
      }

      protected void applyTransformation(float interpolatedTime, Transformation t) {
         FloatingActionButtonEclairMr1.this.mShadowDrawable.setShadowSize(this.mShadowSizeStart + this.mShadowSizeDiff * interpolatedTime);
      }

      protected abstract float getTargetShadowSize();

      // $FF: synthetic method
      BaseShadowAnimation(Object x1) {
         this();
      }
   }
}
