package android.support.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
 import nz.xbc.designsupportdecompilation.R.dimen; import nz.xbc.designsupportdecompilation.R.style;  import nz.xbc.designsupportdecompilation.R.styleable;
import android.support.design.widget.AnimationUtils;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButtonEclairMr1;
import android.support.design.widget.FloatingActionButtonHoneycombMr1;
import android.support.design.widget.FloatingActionButtonImpl;
import android.support.design.widget.FloatingActionButtonLollipop;
import android.support.design.widget.ShadowViewDelegate;
import android.support.design.widget.Snackbar;
import android.support.design.widget.ViewGroupUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import java.util.List;

@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)
public class FloatingActionButton extends ImageView {
   private static final int SIZE_MINI = 1;
   private static final int SIZE_NORMAL = 0;
   private ColorStateList mBackgroundTint;
   private Mode mBackgroundTintMode;
   private int mBorderWidth;
   private int mRippleColor;
   private int mSize;
   private int mContentPadding;
   private final Rect mShadowPadding;
   private final FloatingActionButtonImpl mImpl;

   public FloatingActionButton(Context context) {
      this(context, (AttributeSet)null);
   }

   public FloatingActionButton(Context context, AttributeSet attrs) {
      this(context, attrs, 0);
   }

   public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      this.mShadowPadding = new Rect();
      TypedArray a = context.obtainStyledAttributes(attrs, styleable.FloatingActionButton, defStyleAttr, style.Widget_Design_FloatingActionButton);
      Drawable background = a.getDrawable(styleable.FloatingActionButton_android_background);
      this.mBackgroundTint = a.getColorStateList(styleable.FloatingActionButton_backgroundTint);
      this.mBackgroundTintMode = parseTintMode(a.getInt(styleable.FloatingActionButton_backgroundTintMode, -1), (Mode)null);
      this.mRippleColor = a.getColor(styleable.FloatingActionButton_rippleColor, 0);
      this.mSize = a.getInt(styleable.FloatingActionButton_fabSize, 0);
      this.mBorderWidth = a.getDimensionPixelSize(styleable.FloatingActionButton_borderWidth, 0);
      float elevation = a.getDimension(styleable.FloatingActionButton_elevation, 0.0F);
      float pressedTranslationZ = a.getDimension(styleable.FloatingActionButton_pressedTranslationZ, 0.0F);
      a.recycle();
      ShadowViewDelegate delegate = new ShadowViewDelegate() {
         public float getRadius() {
            return (float)FloatingActionButton.this.getSizeDimension() / 2.0F;
         }

         public void setShadowPadding(int left, int top, int right, int bottom) {
            FloatingActionButton.this.mShadowPadding.set(left, top, right, bottom);
            FloatingActionButton.this.setPadding(left + FloatingActionButton.this.mContentPadding, top + FloatingActionButton.this.mContentPadding, right + FloatingActionButton.this.mContentPadding, bottom + FloatingActionButton.this.mContentPadding);
         }

         public void setBackgroundDrawable(Drawable background) {
            FloatingActionButton.super.setBackgroundDrawable(background);
         }
      };
      int sdk = VERSION.SDK_INT;
      if(sdk >= 21) {
         this.mImpl = new FloatingActionButtonLollipop(this, delegate);
      } else if(sdk >= 12) {
         this.mImpl = new FloatingActionButtonHoneycombMr1(this, delegate);
      } else {
         this.mImpl = new FloatingActionButtonEclairMr1(this, delegate);
      }

      int maxContentSize = (int)this.getResources().getDimension(dimen.fab_content_size);
      this.mContentPadding = (this.getSizeDimension() - maxContentSize) / 2;
      this.mImpl.setBackgroundDrawable(background, this.mBackgroundTint, this.mBackgroundTintMode, this.mRippleColor, this.mBorderWidth);
      this.mImpl.setElevation(elevation);
      this.mImpl.setPressedTranslationZ(pressedTranslationZ);
      this.setClickable(true);
   }

   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int preferredSize = this.getSizeDimension();
      int w = resolveAdjustedSize(preferredSize, widthMeasureSpec);
      int h = resolveAdjustedSize(preferredSize, heightMeasureSpec);
      int d = Math.min(w, h);
      this.setMeasuredDimension(d + this.mShadowPadding.left + this.mShadowPadding.right, d + this.mShadowPadding.top + this.mShadowPadding.bottom);
   }

   public void setRippleColor(int color) {
      if(this.mRippleColor != color) {
         this.mRippleColor = color;
         this.mImpl.setRippleColor(color);
      }

   }

   @Nullable
   public ColorStateList getBackgroundTintList() {
      return this.mBackgroundTint;
   }

   public void setBackgroundTintList(@Nullable ColorStateList tint) {
      this.mImpl.setBackgroundTintList(tint);
   }

   @Nullable
   public Mode getBackgroundTintMode() {
      return this.mBackgroundTintMode;
   }

   public void setBackgroundTintMode(@Nullable Mode tintMode) {
      this.mImpl.setBackgroundTintMode(tintMode);
   }

   public void setBackgroundDrawable(Drawable background) {
      if(this.mImpl != null) {
         this.mImpl.setBackgroundDrawable(background, this.mBackgroundTint, this.mBackgroundTintMode, this.mRippleColor, this.mBorderWidth);
      }

   }

   public void show() {
      if(this.getVisibility() != 0) {
         this.setVisibility(0);
         if(ViewCompat.isLaidOut(this)) {
            this.mImpl.show();
         }

      }
   }

   public void hide() {
      if(this.getVisibility() == 0) {
         if(ViewCompat.isLaidOut(this) && !this.isInEditMode()) {
            this.mImpl.hide();
         } else {
            this.setVisibility(8);
         }

      }
   }

   final int getSizeDimension() {
      switch(this.mSize) {
      case 0:
      default:
         return this.getResources().getDimensionPixelSize(dimen.fab_size_normal);
      case 1:
         return this.getResources().getDimensionPixelSize(dimen.fab_size_mini);
      }
   }

   protected void drawableStateChanged() {
      super.drawableStateChanged();
      this.mImpl.onDrawableStateChanged(this.getDrawableState());
   }

   @TargetApi(11)
   public void jumpDrawablesToCurrentState() {
      super.jumpDrawablesToCurrentState();
      this.mImpl.jumpDrawableToCurrentState();
   }

   private static int resolveAdjustedSize(int desiredSize, int measureSpec) {
      int result = desiredSize;
      int specMode = MeasureSpec.getMode(measureSpec);
      int specSize = MeasureSpec.getSize(measureSpec);
      switch(specMode) {
      case Integer.MIN_VALUE:
         result = Math.min(desiredSize, specSize);
         break;
      case 0:
         result = desiredSize;
         break;
      case 1073741824:
         result = specSize;
      }

      return result;
   }

   static Mode parseTintMode(int value, Mode defaultMode) {
      switch(value) {
      case 3:
         return Mode.SRC_OVER;
      case 4:
      case 6:
      case 7:
      case 8:
      case 10:
      case 11:
      case 12:
      case 13:
      default:
         return defaultMode;
      case 5:
         return Mode.SRC_IN;
      case 9:
         return Mode.SRC_ATOP;
      case 14:
         return Mode.MULTIPLY;
      case 15:
         return Mode.SCREEN;
      }
   }

   public static class Behavior extends CoordinatorLayout.Behavior {
      private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
      private Rect mTmpRect;
      private float mTranslationY;

      public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
         return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
      }

      public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
         if(dependency instanceof Snackbar.SnackbarLayout) {
            this.updateFabTranslationForSnackbar(parent, child, dependency);
         } else if(dependency instanceof AppBarLayout) {
            this.updateFabVisibility(parent, (AppBarLayout)dependency, child);
         }

         return false;
      }

      public void onDependentViewRemoved(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
         if(dependency instanceof Snackbar.SnackbarLayout) {
            ViewCompat.animate(child).translationY(0.0F).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener((ViewPropertyAnimatorListener)null);
         }

      }

      private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, FloatingActionButton child) {
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         if(lp.getAnchorId() != appBarLayout.getId()) {
            return false;
         } else {
            if(this.mTmpRect == null) {
               this.mTmpRect = new Rect();
            }

            Rect rect = this.mTmpRect;
            ViewGroupUtils.getDescendantRect(parent, appBarLayout, rect);
            if(rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
               child.hide();
            } else {
               child.show();
            }

            return true;
         }
      }

      private void updateFabTranslationForSnackbar(CoordinatorLayout parent, FloatingActionButton fab, View snackbar) {
         if(fab.getVisibility() == 0) {
            float translationY = this.getFabTranslationYForSnackbar(parent, fab);
            if(translationY != this.mTranslationY) {
               ViewCompat.animate(fab).cancel();
               ViewCompat.setTranslationY(fab, translationY);
               this.mTranslationY = translationY;
            }

         }
      }

      private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FloatingActionButton fab) {
         float minOffset = 0.0F;
         List dependencies = parent.getDependencies(fab);
         int i = 0;

         for(int z = dependencies.size(); i < z; ++i) {
            View view = (View)dependencies.get(i);
            if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
               minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float)view.getHeight());
            }
         }

         return minOffset;
      }

      public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child, int layoutDirection) {
         List dependencies = parent.getDependencies(child);
         int i = 0;

         for(int count = dependencies.size(); i < count; ++i) {
            View dependency = (View)dependencies.get(i);
            if(dependency instanceof AppBarLayout && this.updateFabVisibility(parent, (AppBarLayout)dependency, child)) {
               break;
            }
         }

         parent.onLayoutChild(child, layoutDirection);
         this.offsetIfNeeded(parent, child);
         return true;
      }

      private void offsetIfNeeded(CoordinatorLayout parent, FloatingActionButton fab) {
         Rect padding = fab.mShadowPadding;
         if(padding != null && padding.centerX() > 0 && padding.centerY() > 0) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
            int offsetTB = 0;
            int offsetLR = 0;
            if(fab.getRight() >= parent.getWidth() - lp.rightMargin) {
               offsetLR = padding.right;
            } else if(fab.getLeft() <= lp.leftMargin) {
               offsetLR = -padding.left;
            }

            if(fab.getBottom() >= parent.getBottom() - lp.bottomMargin) {
               offsetTB = padding.bottom;
            } else if(fab.getTop() <= lp.topMargin) {
               offsetTB = -padding.top;
            }

            fab.offsetTopAndBottom(offsetTB);
            fab.offsetLeftAndRight(offsetLR);
         }

      }

      static {
         SNACKBAR_BEHAVIOR_ENABLED = VERSION.SDK_INT >= 11;
      }
   }
}
