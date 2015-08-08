package android.support.design.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;

class CircularBorderDrawable extends Drawable {
   private static final float DRAW_STROKE_WIDTH_MULTIPLE = 1.3333F;
   final Paint mPaint = new Paint(1);
   final Rect mRect = new Rect();
   final RectF mRectF = new RectF();
   float mBorderWidth;
   private int mTopOuterStrokeColor;
   private int mTopInnerStrokeColor;
   private int mBottomOuterStrokeColor;
   private int mBottomInnerStrokeColor;
   private int mTintColor;
   private boolean mInvalidateShader = true;

   public CircularBorderDrawable() {
      this.mPaint.setStyle(Style.STROKE);
   }

   void setGradientColors(int topOuterStrokeColor, int topInnerStrokeColor, int bottomOuterStrokeColor, int bottomInnerStrokeColor) {
      this.mTopOuterStrokeColor = topOuterStrokeColor;
      this.mTopInnerStrokeColor = topInnerStrokeColor;
      this.mBottomOuterStrokeColor = bottomOuterStrokeColor;
      this.mBottomInnerStrokeColor = bottomInnerStrokeColor;
   }

   void setBorderWidth(float width) {
      if(this.mBorderWidth != width) {
         this.mBorderWidth = width;
         this.mPaint.setStrokeWidth(width * 1.3333F);
         this.mInvalidateShader = true;
         this.invalidateSelf();
      }

   }

   public void draw(Canvas canvas) {
      if(this.mInvalidateShader) {
         this.mPaint.setShader(this.createGradientShader());
         this.mInvalidateShader = false;
      }

      float halfBorderWidth = this.mPaint.getStrokeWidth() / 2.0F;
      RectF rectF = this.mRectF;
      this.copyBounds(this.mRect);
      rectF.set(this.mRect);
      rectF.left += halfBorderWidth;
      rectF.top += halfBorderWidth;
      rectF.right -= halfBorderWidth;
      rectF.bottom -= halfBorderWidth;
      canvas.drawOval(rectF, this.mPaint);
   }

   public boolean getPadding(Rect padding) {
      int borderWidth = Math.round(this.mBorderWidth);
      padding.set(borderWidth, borderWidth, borderWidth, borderWidth);
      return true;
   }

   public void setAlpha(int alpha) {
      this.mPaint.setAlpha(alpha);
      this.invalidateSelf();
   }

   void setTintColor(int tintColor) {
      this.mTintColor = tintColor;
      this.mInvalidateShader = true;
      this.invalidateSelf();
   }

   public void setColorFilter(ColorFilter colorFilter) {
      this.mPaint.setColorFilter(colorFilter);
      this.invalidateSelf();
   }

   public int getOpacity() {
      return this.mBorderWidth > 0.0F?-3:-2;
   }

   protected void onBoundsChange(Rect bounds) {
      this.mInvalidateShader = true;
   }

   private Shader createGradientShader() {
      Rect rect = this.mRect;
      this.copyBounds(rect);
      float borderRatio = this.mBorderWidth / (float)rect.height();
      int[] colors = new int[]{ColorUtils.compositeColors(this.mTopOuterStrokeColor, this.mTintColor), ColorUtils.compositeColors(this.mTopInnerStrokeColor, this.mTintColor), ColorUtils.compositeColors(ColorUtils.setAlphaComponent(this.mTopInnerStrokeColor, 0), this.mTintColor), ColorUtils.compositeColors(ColorUtils.setAlphaComponent(this.mBottomInnerStrokeColor, 0), this.mTintColor), ColorUtils.compositeColors(this.mBottomInnerStrokeColor, this.mTintColor), ColorUtils.compositeColors(this.mBottomOuterStrokeColor, this.mTintColor)};
      float[] positions = new float[]{0.0F, borderRatio, 0.5F, 0.5F, 1.0F - borderRatio, 1.0F};
      return new LinearGradient(0.0F, (float)rect.top, 0.0F, (float)rect.bottom, colors, positions, TileMode.CLAMP);
   }
}
