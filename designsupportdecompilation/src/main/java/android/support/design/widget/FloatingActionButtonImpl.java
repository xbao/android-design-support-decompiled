package android.support.design.widget;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
 import nz.xbc.designsupportdecompilation.R.color;
import android.support.design.widget.CircularBorderDrawable;
import android.support.design.widget.ShadowViewDelegate;
import android.view.View;

abstract class FloatingActionButtonImpl {
   static final int SHOW_HIDE_ANIM_DURATION = 200;
   static final int[] PRESSED_ENABLED_STATE_SET = new int[]{16842919, 16842910};
   static final int[] FOCUSED_ENABLED_STATE_SET = new int[]{16842908, 16842910};
   static final int[] EMPTY_STATE_SET = new int[0];
   final View mView;
   final ShadowViewDelegate mShadowViewDelegate;

   FloatingActionButtonImpl(View view, ShadowViewDelegate shadowViewDelegate) {
      this.mView = view;
      this.mShadowViewDelegate = shadowViewDelegate;
   }

   abstract void setBackgroundDrawable(Drawable var1, ColorStateList var2, Mode var3, int var4, int var5);

   abstract void setBackgroundTintList(ColorStateList var1);

   abstract void setBackgroundTintMode(Mode var1);

   abstract void setRippleColor(int var1);

   abstract void setElevation(float var1);

   abstract void setPressedTranslationZ(float var1);

   abstract void onDrawableStateChanged(int[] var1);

   abstract void jumpDrawableToCurrentState();

   abstract void hide();

   abstract void show();

   Drawable createBorderDrawable(int borderWidth, ColorStateList backgroundTint) {
      Resources resources = this.mView.getResources();
      CircularBorderDrawable borderDrawable = this.newCircularDrawable();
      borderDrawable.setGradientColors(resources.getColor(color.fab_stroke_top_outer_color), resources.getColor(color.fab_stroke_top_inner_color), resources.getColor(color.fab_stroke_end_inner_color), resources.getColor(color.fab_stroke_end_outer_color));
      borderDrawable.setBorderWidth((float)borderWidth);
      borderDrawable.setTintColor(backgroundTint.getDefaultColor());
      return borderDrawable;
   }

   CircularBorderDrawable newCircularDrawable() {
      return new CircularBorderDrawable();
   }
}
