package android.support.design.widget;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Path.FillType;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
 import nz.xbc.designsupportdecompilation.R.color;
import android.support.v7.graphics.drawable.DrawableWrapper;

class ShadowDrawableWrapper extends DrawableWrapper {
   static final double COS_45 = Math.cos(Math.toRadians(45.0D));
   static final float SHADOW_MULTIPLIER = 1.5F;
   static final float SHADOW_TOP_SCALE = 0.25F;
   static final float SHADOW_HORIZ_SCALE = 0.5F;
   static final float SHADOW_BOTTOM_SCALE = 1.0F;
   final Paint mCornerShadowPaint;
   final Paint mEdgeShadowPaint;
   final RectF mContentBounds;
   float mCornerRadius;
   Path mCornerShadowPath;
   float mMaxShadowSize;
   float mRawMaxShadowSize;
   float mShadowSize;
   float mRawShadowSize;
   private boolean mDirty = true;
   private final int mShadowStartColor;
   private final int mShadowMiddleColor;
   private final int mShadowEndColor;
   private boolean mAddPaddingForCorners = true;
   private boolean mPrintedShadowClipWarning = false;

   public ShadowDrawableWrapper(Resources resources, Drawable content, float radius, float shadowSize, float maxShadowSize) {
      super(content);
      this.mShadowStartColor = resources.getColor(color.shadow_start_color);
      this.mShadowMiddleColor = resources.getColor(color.shadow_mid_color);
      this.mShadowEndColor = resources.getColor(color.shadow_end_color);
      this.mCornerShadowPaint = new Paint(5);
      this.mCornerShadowPaint.setStyle(Style.FILL);
      this.mCornerRadius = (float)Math.round(radius);
      this.mContentBounds = new RectF();
      this.mEdgeShadowPaint = new Paint(this.mCornerShadowPaint);
      this.mEdgeShadowPaint.setAntiAlias(false);
      this.setShadowSize(shadowSize, maxShadowSize);
   }

   private static int toEven(float value) {
      int i = Math.round(value);
      return i % 2 == 1?i - 1:i;
   }

   public void setAddPaddingForCorners(boolean addPaddingForCorners) {
      this.mAddPaddingForCorners = addPaddingForCorners;
      this.invalidateSelf();
   }

   public void setAlpha(int alpha) {
      super.setAlpha(alpha);
      this.mCornerShadowPaint.setAlpha(alpha);
      this.mEdgeShadowPaint.setAlpha(alpha);
   }

   protected void onBoundsChange(Rect bounds) {
      this.mDirty = true;
   }

   void setShadowSize(float shadowSize, float maxShadowSize) {
      if(shadowSize >= 0.0F && maxShadowSize >= 0.0F) {
         shadowSize = (float)toEven(shadowSize);
         maxShadowSize = (float)toEven(maxShadowSize);
         if(shadowSize > maxShadowSize) {
            shadowSize = maxShadowSize;
            if(!this.mPrintedShadowClipWarning) {
               this.mPrintedShadowClipWarning = true;
            }
         }

         if(this.mRawShadowSize != shadowSize || this.mRawMaxShadowSize != maxShadowSize) {
            this.mRawShadowSize = shadowSize;
            this.mRawMaxShadowSize = maxShadowSize;
            this.mShadowSize = (float)Math.round(shadowSize * 1.5F);
            this.mMaxShadowSize = maxShadowSize;
            this.mDirty = true;
            this.invalidateSelf();
         }
      } else {
         throw new IllegalArgumentException("invalid shadow size");
      }
   }

   public boolean getPadding(Rect padding) {
      int vOffset = (int)Math.ceil((double)calculateVerticalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners));
      int hOffset = (int)Math.ceil((double)calculateHorizontalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners));
      padding.set(hOffset, vOffset, hOffset, vOffset);
      return true;
   }

   public static float calculateVerticalPadding(float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
      return addPaddingForCorners?(float)((double)(maxShadowSize * 1.5F) + (1.0D - COS_45) * (double)cornerRadius):maxShadowSize * 1.5F;
   }

   public static float calculateHorizontalPadding(float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
      return addPaddingForCorners?(float)((double)maxShadowSize + (1.0D - COS_45) * (double)cornerRadius):maxShadowSize;
   }

   public int getOpacity() {
      return -3;
   }

   public void setCornerRadius(float radius) {
      radius = (float)Math.round(radius);
      if(this.mCornerRadius != radius) {
         this.mCornerRadius = radius;
         this.mDirty = true;
         this.invalidateSelf();
      }
   }

   public void draw(Canvas canvas) {
      if(this.mDirty) {
         this.buildComponents(this.getBounds());
         this.mDirty = false;
      }

      this.drawShadow(canvas);
      super.draw(canvas);
   }

   private void drawShadow(Canvas canvas) {
      float edgeShadowTop = -this.mCornerRadius - this.mShadowSize;
      float shadowOffset = this.mCornerRadius;
      boolean drawHorizontalEdges = this.mContentBounds.width() - 2.0F * shadowOffset > 0.0F;
      boolean drawVerticalEdges = this.mContentBounds.height() - 2.0F * shadowOffset > 0.0F;
      float shadowOffsetTop = this.mRawShadowSize - this.mRawShadowSize * 0.25F;
      float shadowOffsetHorizontal = this.mRawShadowSize - this.mRawShadowSize * 0.5F;
      float shadowOffsetBottom = this.mRawShadowSize - this.mRawShadowSize * 1.0F;
      float shadowScaleHorizontal = shadowOffset / (shadowOffset + shadowOffsetHorizontal);
      float shadowScaleTop = shadowOffset / (shadowOffset + shadowOffsetTop);
      float shadowScaleBottom = shadowOffset / (shadowOffset + shadowOffsetBottom);
      int saved = canvas.save();
      canvas.translate(this.mContentBounds.left + shadowOffset, this.mContentBounds.top + shadowOffset);
      canvas.scale(shadowScaleHorizontal, shadowScaleTop);
      canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
      if(drawHorizontalEdges) {
         canvas.scale(1.0F / shadowScaleHorizontal, 1.0F);
         canvas.drawRect(0.0F, edgeShadowTop, this.mContentBounds.width() - 2.0F * shadowOffset, -this.mCornerRadius, this.mEdgeShadowPaint);
      }

      canvas.restoreToCount(saved);
      saved = canvas.save();
      canvas.translate(this.mContentBounds.right - shadowOffset, this.mContentBounds.bottom - shadowOffset);
      canvas.scale(shadowScaleHorizontal, shadowScaleBottom);
      canvas.rotate(180.0F);
      canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
      if(drawHorizontalEdges) {
         canvas.scale(1.0F / shadowScaleHorizontal, 1.0F);
         canvas.drawRect(0.0F, edgeShadowTop, this.mContentBounds.width() - 2.0F * shadowOffset, -this.mCornerRadius + this.mShadowSize, this.mEdgeShadowPaint);
      }

      canvas.restoreToCount(saved);
      saved = canvas.save();
      canvas.translate(this.mContentBounds.left + shadowOffset, this.mContentBounds.bottom - shadowOffset);
      canvas.scale(shadowScaleHorizontal, shadowScaleBottom);
      canvas.rotate(270.0F);
      canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
      if(drawVerticalEdges) {
         canvas.scale(1.0F / shadowScaleBottom, 1.0F);
         canvas.drawRect(0.0F, edgeShadowTop, this.mContentBounds.height() - 2.0F * shadowOffset, -this.mCornerRadius, this.mEdgeShadowPaint);
      }

      canvas.restoreToCount(saved);
      saved = canvas.save();
      canvas.translate(this.mContentBounds.right - shadowOffset, this.mContentBounds.top + shadowOffset);
      canvas.scale(shadowScaleHorizontal, shadowScaleTop);
      canvas.rotate(90.0F);
      canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
      if(drawVerticalEdges) {
         canvas.scale(1.0F / shadowScaleTop, 1.0F);
         canvas.drawRect(0.0F, edgeShadowTop, this.mContentBounds.height() - 2.0F * shadowOffset, -this.mCornerRadius, this.mEdgeShadowPaint);
      }

      canvas.restoreToCount(saved);
   }

   private void buildShadowCorners() {
      RectF innerBounds = new RectF(-this.mCornerRadius, -this.mCornerRadius, this.mCornerRadius, this.mCornerRadius);
      RectF outerBounds = new RectF(innerBounds);
      outerBounds.inset(-this.mShadowSize, -this.mShadowSize);
      if(this.mCornerShadowPath == null) {
         this.mCornerShadowPath = new Path();
      } else {
         this.mCornerShadowPath.reset();
      }

      this.mCornerShadowPath.setFillType(FillType.EVEN_ODD);
      this.mCornerShadowPath.moveTo(-this.mCornerRadius, 0.0F);
      this.mCornerShadowPath.rLineTo(-this.mShadowSize, 0.0F);
      this.mCornerShadowPath.arcTo(outerBounds, 180.0F, 90.0F, false);
      this.mCornerShadowPath.arcTo(innerBounds, 270.0F, -90.0F, false);
      this.mCornerShadowPath.close();
      float shadowRadius = -outerBounds.top;
      if(shadowRadius > 0.0F) {
         float startRatio = this.mCornerRadius / shadowRadius;
         float midRatio = startRatio + (1.0F - startRatio) / 2.0F;
         this.mCornerShadowPaint.setShader(new RadialGradient(0.0F, 0.0F, shadowRadius, new int[]{0, this.mShadowStartColor, this.mShadowMiddleColor, this.mShadowEndColor}, new float[]{0.0F, startRatio, midRatio, 1.0F}, TileMode.CLAMP));
      }

      this.mEdgeShadowPaint.setShader(new LinearGradient(0.0F, innerBounds.top, 0.0F, outerBounds.top, new int[]{this.mShadowStartColor, this.mShadowMiddleColor, this.mShadowEndColor}, new float[]{0.0F, 0.5F, 1.0F}, TileMode.CLAMP));
      this.mEdgeShadowPaint.setAntiAlias(false);
   }

   private void buildComponents(Rect bounds) {
      float verticalOffset = this.mRawMaxShadowSize * 1.5F;
      this.mContentBounds.set((float)bounds.left + this.mRawMaxShadowSize, (float)bounds.top + verticalOffset, (float)bounds.right - this.mRawMaxShadowSize, (float)bounds.bottom - verticalOffset);
      this.getWrappedDrawable().setBounds((int)this.mContentBounds.left, (int)this.mContentBounds.top, (int)this.mContentBounds.right, (int)this.mContentBounds.bottom);
      this.buildShadowCorners();
   }

   public float getCornerRadius() {
      return this.mCornerRadius;
   }

   public void setShadowSize(float size) {
      this.setShadowSize(size, this.mRawMaxShadowSize);
   }

   public void setMaxShadowSize(float size) {
      this.setShadowSize(this.mRawShadowSize, size);
   }

   public float getShadowSize() {
      return this.mRawShadowSize;
   }

   public float getMaxShadowSize() {
      return this.mRawMaxShadowSize;
   }

   public float getMinWidth() {
      float content = 2.0F * Math.max(this.mRawMaxShadowSize, this.mCornerRadius + this.mRawMaxShadowSize / 2.0F);
      return content + this.mRawMaxShadowSize * 2.0F;
   }

   public float getMinHeight() {
      float content = 2.0F * Math.max(this.mRawMaxShadowSize, this.mCornerRadius + this.mRawMaxShadowSize * 1.5F / 2.0F);
      return content + this.mRawMaxShadowSize * 1.5F * 2.0F;
   }
}
