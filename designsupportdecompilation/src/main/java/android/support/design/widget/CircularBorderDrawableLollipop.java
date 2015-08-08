package android.support.design.widget;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.support.design.widget.CircularBorderDrawable;

class CircularBorderDrawableLollipop extends CircularBorderDrawable {
   private ColorStateList mTint;
   private Mode mTintMode;
   private PorterDuffColorFilter mTintFilter;

   CircularBorderDrawableLollipop() {
      this.mTintMode = Mode.SRC_IN;
   }

   public void draw(Canvas canvas) {
      boolean clearColorFilter;
      if(this.mTintFilter != null && this.mPaint.getColorFilter() == null) {
         this.mPaint.setColorFilter(this.mTintFilter);
         clearColorFilter = true;
      } else {
         clearColorFilter = false;
      }

      super.draw(canvas);
      if(clearColorFilter) {
         this.mPaint.setColorFilter((ColorFilter)null);
      }

   }

   public void setTintList(ColorStateList tint) {
      this.mTint = tint;
      this.mTintFilter = this.updateTintFilter(tint, this.mTintMode);
      this.invalidateSelf();
   }

   public void setTintMode(Mode tintMode) {
      this.mTintMode = tintMode;
      this.mTintFilter = this.updateTintFilter(this.mTint, tintMode);
      this.invalidateSelf();
   }

   public void getOutline(Outline outline) {
      this.copyBounds(this.mRect);
      outline.setOval(this.mRect);
   }

   private PorterDuffColorFilter updateTintFilter(ColorStateList tint, Mode tintMode) {
      if(tint != null && tintMode != null) {
         int color = tint.getColorForState(this.getState(), 0);
         return new PorterDuffColorFilter(color, tintMode);
      } else {
         return null;
      }
   }
}
