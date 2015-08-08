package android.support.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
 import nz.xbc.designsupportdecompilation.R.id; import nz.xbc.designsupportdecompilation.R.style;  import nz.xbc.designsupportdecompilation.R.styleable;
import android.support.design.widget.AnimationUtils;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingTextHelper;
import android.support.design.widget.ValueAnimatorCompat;
import android.support.design.widget.ViewGroupUtils;
import android.support.design.widget.ViewOffsetHelper;
import android.support.design.widget.ViewUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CollapsingToolbarLayout extends FrameLayout {
   private static final int SCRIM_ANIMATION_DURATION = 600;
   private boolean mRefreshToolbar;
   private int mToolbarId;
   private Toolbar mToolbar;
   private View mDummyView;
   private int mExpandedMarginLeft;
   private int mExpandedMarginTop;
   private int mExpandedMarginRight;
   private int mExpandedMarginBottom;
   private final Rect mTmpRect;
   private final CollapsingTextHelper mCollapsingTextHelper;
   private Drawable mContentScrim;
   private Drawable mStatusBarScrim;
   private int mScrimAlpha;
   private boolean mScrimsAreShown;
   private ValueAnimatorCompat mScrimAnimator;
   private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;
   private int mCurrentOffset;
   private WindowInsetsCompat mLastInsets;

   public CollapsingToolbarLayout(Context context) {
      this(context, (AttributeSet)null);
   }

   public CollapsingToolbarLayout(Context context, AttributeSet attrs) {
      this(context, attrs, 0);
   }

   public CollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      this.mRefreshToolbar = true;
      this.mTmpRect = new Rect();
      this.mCollapsingTextHelper = new CollapsingTextHelper(this);
      this.mCollapsingTextHelper.setExpandedTextVerticalGravity(80);
      this.mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
      TypedArray a = context.obtainStyledAttributes(attrs, styleable.CollapsingToolbarLayout, defStyleAttr, style.Widget_Design_CollapsingToolbar);
      this.mExpandedMarginLeft = this.mExpandedMarginTop = this.mExpandedMarginRight = this.mExpandedMarginBottom = a.getDimensionPixelSize(styleable.CollapsingToolbarLayout_expandedTitleMargin, 0);
      boolean isRtl = ViewCompat.getLayoutDirection(this) == 1;
      int tp;
      if(a.hasValue(styleable.CollapsingToolbarLayout_expandedTitleMarginStart)) {
         tp = a.getDimensionPixelSize(styleable.CollapsingToolbarLayout_expandedTitleMarginStart, 0);
         if(isRtl) {
            this.mExpandedMarginRight = tp;
         } else {
            this.mExpandedMarginLeft = tp;
         }
      }

      if(a.hasValue(styleable.CollapsingToolbarLayout_expandedTitleMarginEnd)) {
         tp = a.getDimensionPixelSize(styleable.CollapsingToolbarLayout_expandedTitleMarginEnd, 0);
         if(isRtl) {
            this.mExpandedMarginLeft = tp;
         } else {
            this.mExpandedMarginRight = tp;
         }
      }

      if(a.hasValue(styleable.CollapsingToolbarLayout_expandedTitleMarginTop)) {
         this.mExpandedMarginTop = a.getDimensionPixelSize(styleable.CollapsingToolbarLayout_expandedTitleMarginTop, 0);
      }

      if(a.hasValue(styleable.CollapsingToolbarLayout_expandedTitleMarginBottom)) {
         this.mExpandedMarginBottom = a.getDimensionPixelSize(styleable.CollapsingToolbarLayout_expandedTitleMarginBottom, 0);
      }

      tp = a.getResourceId(styleable.CollapsingToolbarLayout_expandedTitleTextAppearance, style.TextAppearance_AppCompat_Title);
      this.mCollapsingTextHelper.setExpandedTextAppearance(tp);
      tp = a.getResourceId(styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance, style.TextAppearance_AppCompat_Widget_ActionBar_Title);
      this.mCollapsingTextHelper.setCollapsedTextAppearance(tp);
      this.setContentScrim(a.getDrawable(styleable.CollapsingToolbarLayout_contentScrim));
      this.setStatusBarScrim(a.getDrawable(styleable.CollapsingToolbarLayout_statusBarScrim));
      this.mToolbarId = a.getResourceId(styleable.CollapsingToolbarLayout_toolbarId, -1);
      a.recycle();
      this.setWillNotDraw(false);
      ViewCompat.setOnApplyWindowInsetsListener(this, new android.support.v4.view.OnApplyWindowInsetsListener() {
         public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            CollapsingToolbarLayout.this.mLastInsets = insets;
            CollapsingToolbarLayout.this.requestLayout();
            return insets.consumeSystemWindowInsets();
         }
      });
   }

   protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      ViewParent parent = this.getParent();
      if(parent instanceof AppBarLayout) {
         if(this.mOnOffsetChangedListener == null) {
            this.mOnOffsetChangedListener = new CollapsingToolbarLayout.OffsetUpdateListener(null);
         }

         ((AppBarLayout)parent).addOnOffsetChangedListener(this.mOnOffsetChangedListener);
      }

   }

   protected void onDetachedFromWindow() {
      ViewParent parent = this.getParent();
      if(this.mOnOffsetChangedListener != null && parent instanceof AppBarLayout) {
         ((AppBarLayout)parent).removeOnOffsetChangedListener(this.mOnOffsetChangedListener);
      }

      super.onDetachedFromWindow();
   }

   public void draw(Canvas canvas) {
      super.draw(canvas);
      this.ensureToolbar();
      if(this.mToolbar == null && this.mContentScrim != null && this.mScrimAlpha > 0) {
         this.mContentScrim.mutate().setAlpha(this.mScrimAlpha);
         this.mContentScrim.draw(canvas);
      }

      this.mCollapsingTextHelper.draw(canvas);
      if(this.mStatusBarScrim != null && this.mScrimAlpha > 0) {
         int topInset = this.mLastInsets != null?this.mLastInsets.getSystemWindowInsetTop():0;
         if(topInset > 0) {
            this.mStatusBarScrim.setBounds(0, -this.mCurrentOffset, this.getWidth(), topInset - this.mCurrentOffset);
            this.mStatusBarScrim.mutate().setAlpha(this.mScrimAlpha);
            this.mStatusBarScrim.draw(canvas);
         }
      }

   }

   protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
      this.ensureToolbar();
      if(child == this.mToolbar && this.mContentScrim != null && this.mScrimAlpha > 0) {
         this.mContentScrim.mutate().setAlpha(this.mScrimAlpha);
         this.mContentScrim.draw(canvas);
      }

      return super.drawChild(canvas, child, drawingTime);
   }

   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
      if(this.mContentScrim != null) {
         this.mContentScrim.setBounds(0, 0, w, h);
      }

   }

   private void ensureToolbar() {
      if(this.mRefreshToolbar) {
         Toolbar fallback = null;
         Toolbar selected = null;
         int i = 0;

         for(int count = this.getChildCount(); i < count; ++i) {
            View child = this.getChildAt(i);
            if(child instanceof Toolbar) {
               if(this.mToolbarId == -1) {
                  selected = (Toolbar)child;
                  break;
               }

               if(this.mToolbarId == child.getId()) {
                  selected = (Toolbar)child;
                  break;
               }

               if(fallback == null) {
                  fallback = (Toolbar)child;
               }
            }
         }

         if(selected == null) {
            selected = fallback;
         }

         if(selected != null) {
            this.mToolbar = selected;
            this.mDummyView = new View(this.getContext());
            this.mToolbar.addView(this.mDummyView, -1, -1);
         } else {
            this.mToolbar = null;
            this.mDummyView = null;
         }

         this.mRefreshToolbar = false;
      }
   }

   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      this.ensureToolbar();
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
   }

   protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      super.onLayout(changed, left, top, right, bottom);
      int i = 0;

      for(int z = this.getChildCount(); i < z; ++i) {
         View child = this.getChildAt(i);
         if(this.mLastInsets != null && !ViewCompat.getFitsSystemWindows(child)) {
            int insetTop = this.mLastInsets.getSystemWindowInsetTop();
            if(child.getTop() < insetTop) {
               child.offsetTopAndBottom(insetTop);
            }
         }

         getViewOffsetHelper(child).onViewLayout();
      }

      if(this.mDummyView != null) {
         ViewGroupUtils.getDescendantRect(this, this.mDummyView, this.mTmpRect);
         this.mCollapsingTextHelper.setCollapsedBounds(this.mTmpRect.left, bottom - this.mTmpRect.height(), this.mTmpRect.right, bottom);
         this.mCollapsingTextHelper.setExpandedBounds(left + this.mExpandedMarginLeft, this.mTmpRect.bottom + this.mExpandedMarginTop, right - this.mExpandedMarginRight, bottom - this.mExpandedMarginBottom);
         this.mCollapsingTextHelper.recalculate();
      }

      if(this.mToolbar != null) {
         this.setMinimumHeight(this.mToolbar.getHeight());
      }

   }

   private static ViewOffsetHelper getViewOffsetHelper(View view) {
      ViewOffsetHelper offsetHelper = (ViewOffsetHelper)view.getTag(id.view_offset_helper);
      if(offsetHelper == null) {
         offsetHelper = new ViewOffsetHelper(view);
         view.setTag(id.view_offset_helper, offsetHelper);
      }

      return offsetHelper;
   }

   public void setTitle(CharSequence title) {
      this.mCollapsingTextHelper.setText(title);
   }

   private void showScrim() {
      if(!this.mScrimsAreShown) {
         if(ViewCompat.isLaidOut(this) && !this.isInEditMode()) {
            this.animateScrim(255);
         } else {
            this.setScrimAlpha(255);
         }

         this.mScrimsAreShown = true;
      }

   }

   private void hideScrim() {
      if(this.mScrimsAreShown) {
         if(ViewCompat.isLaidOut(this) && !this.isInEditMode()) {
            this.animateScrim(0);
         } else {
            this.setScrimAlpha(0);
         }

         this.mScrimsAreShown = false;
      }

   }

   private void animateScrim(int targetAlpha) {
      this.ensureToolbar();
      if(this.mScrimAnimator == null) {
         this.mScrimAnimator = ViewUtils.createAnimator();
         this.mScrimAnimator.setDuration(600);
         this.mScrimAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
         this.mScrimAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimatorCompat animator) {
               CollapsingToolbarLayout.this.setScrimAlpha(animator.getAnimatedIntValue());
            }
         });
      } else if(this.mScrimAnimator.isRunning()) {
         this.mScrimAnimator.cancel();
      }

      this.mScrimAnimator.setIntValues(this.mScrimAlpha, targetAlpha);
      this.mScrimAnimator.start();
   }

   private void setScrimAlpha(int alpha) {
      if(alpha != this.mScrimAlpha) {
         Drawable contentScrim = this.mContentScrim;
         if(contentScrim != null && this.mToolbar != null) {
            ViewCompat.postInvalidateOnAnimation(this.mToolbar);
         }

         this.mScrimAlpha = alpha;
         ViewCompat.postInvalidateOnAnimation(this);
      }

   }

   public void setContentScrim(@Nullable Drawable drawable) {
      if(this.mContentScrim != drawable) {
         if(this.mContentScrim != null) {
            this.mContentScrim.setCallback((Callback)null);
         }

         this.mContentScrim = drawable;
         drawable.setBounds(0, 0, this.getWidth(), this.getHeight());
         drawable.setCallback(this);
         drawable.mutate().setAlpha(this.mScrimAlpha);
         ViewCompat.postInvalidateOnAnimation(this);
      }

   }

   public void setContentScrimColor(int color) {
      this.setContentScrim(new ColorDrawable(color));
   }

   public void setContentScrimResource(@DrawableRes int resId) {
      this.setContentScrim(ContextCompat.getDrawable(this.getContext(), resId));
   }

   public Drawable getContentScrim() {
      return this.mContentScrim;
   }

   public void setStatusBarScrim(@Nullable Drawable drawable) {
      if(this.mStatusBarScrim != drawable) {
         if(this.mStatusBarScrim != null) {
            this.mStatusBarScrim.setCallback((Callback)null);
         }

         this.mStatusBarScrim = drawable;
         drawable.setCallback(this);
         drawable.mutate().setAlpha(this.mScrimAlpha);
         ViewCompat.postInvalidateOnAnimation(this);
      }

   }

   public void setStatusBarScrimColor(int color) {
      this.setStatusBarScrim(new ColorDrawable(color));
   }

   public void setStatusBarScrimResource(@DrawableRes int resId) {
      this.setStatusBarScrim(ContextCompat.getDrawable(this.getContext(), resId));
   }

   public Drawable getStatusBarScrim() {
      return this.mStatusBarScrim;
   }

   public void setCollapsedTitleTextAppearance(int resId) {
      this.mCollapsingTextHelper.setCollapsedTextAppearance(resId);
   }

   public void setCollapsedTitleTextColor(int color) {
      this.mCollapsingTextHelper.setCollapsedTextColor(color);
   }

   public void setExpandedTitleTextAppearance(int resId) {
      this.mCollapsingTextHelper.setExpandedTextAppearance(resId);
   }

   public void setExpandedTitleColor(int color) {
      this.mCollapsingTextHelper.setExpandedTextColor(color);
   }

   final int getScrimTriggerOffset() {
      return 2 * ViewCompat.getMinimumHeight(this);
   }

   protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
      return p instanceof CollapsingToolbarLayout.LayoutParams;
   }

   protected CollapsingToolbarLayout.LayoutParams generateDefaultLayoutParams() {
      return new CollapsingToolbarLayout.LayoutParams(super.generateDefaultLayoutParams());
   }

   public android.widget.FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
      return new CollapsingToolbarLayout.LayoutParams(this.getContext(), attrs);
   }

   protected android.widget.FrameLayout.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
      return new CollapsingToolbarLayout.LayoutParams(p);
   }

   private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
      private OffsetUpdateListener() {
      }

      public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
         CollapsingToolbarLayout.this.mCurrentOffset = verticalOffset;
         int insetTop = CollapsingToolbarLayout.this.mLastInsets != null?CollapsingToolbarLayout.this.mLastInsets.getSystemWindowInsetTop():0;
         int scrollRange = layout.getTotalScrollRange();
         int expandRange = 0;

         for(int z = CollapsingToolbarLayout.this.getChildCount(); expandRange < z; ++expandRange) {
            View child = CollapsingToolbarLayout.this.getChildAt(expandRange);
            CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams)child.getLayoutParams();
            ViewOffsetHelper offsetHelper = CollapsingToolbarLayout.getViewOffsetHelper(child);
            switch(lp.mCollapseMode) {
            case 1:
               if(CollapsingToolbarLayout.this.getHeight() - insetTop + verticalOffset >= child.getHeight()) {
                  offsetHelper.setTopAndBottomOffset(-verticalOffset);
               }
               break;
            case 2:
               offsetHelper.setTopAndBottomOffset(Math.round((float)(-verticalOffset) * lp.mParallaxMult));
            }
         }

         if(CollapsingToolbarLayout.this.mContentScrim != null || CollapsingToolbarLayout.this.mStatusBarScrim != null) {
            if(CollapsingToolbarLayout.this.getHeight() + verticalOffset < CollapsingToolbarLayout.this.getScrimTriggerOffset() + insetTop) {
               CollapsingToolbarLayout.this.showScrim();
            } else {
               CollapsingToolbarLayout.this.hideScrim();
            }
         }

         if(CollapsingToolbarLayout.this.mStatusBarScrim != null && insetTop > 0) {
            ViewCompat.postInvalidateOnAnimation(CollapsingToolbarLayout.this);
         }

         expandRange = CollapsingToolbarLayout.this.getHeight() - ViewCompat.getMinimumHeight(CollapsingToolbarLayout.this) - insetTop;
         CollapsingToolbarLayout.this.mCollapsingTextHelper.setExpansionFraction((float)Math.abs(verticalOffset) / (float)expandRange);
         if(Math.abs(verticalOffset) == scrollRange) {
            ViewCompat.setElevation(layout, layout.getTargetElevation());
         } else {
            ViewCompat.setElevation(layout, 0.0F);
         }

      }

      // $FF: synthetic method
      OffsetUpdateListener(Object x1) {
         this();
      }
   }

   public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
      private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5F;
      public static final int COLLAPSE_MODE_OFF = 0;
      public static final int COLLAPSE_MODE_PIN = 1;
      public static final int COLLAPSE_MODE_PARALLAX = 2;
      int mCollapseMode = 0;
      float mParallaxMult = 0.5F;

      public LayoutParams(Context c, AttributeSet attrs) {
         super(c, attrs);
         TypedArray a = c.obtainStyledAttributes(attrs, styleable.CollapsingAppBarLayout_LayoutParams);
         this.mCollapseMode = a.getInt(styleable.CollapsingAppBarLayout_LayoutParams_layout_collapseMode, 0);
         this.setParallaxMultiplier(a.getFloat(styleable.CollapsingAppBarLayout_LayoutParams_layout_collapseParallaxMultiplier, 0.5F));
         a.recycle();
      }

      public LayoutParams(int width, int height) {
         super(width, height);
      }

      public LayoutParams(int width, int height, int gravity) {
         super(width, height, gravity);
      }

      public LayoutParams(android.view.ViewGroup.LayoutParams p) {
         super(p);
      }

      public LayoutParams(MarginLayoutParams source) {
         super(source);
      }

      public LayoutParams(android.widget.FrameLayout.LayoutParams source) {
         super(source);
      }

      public void setCollapseMode(int collapseMode) {
         this.mCollapseMode = collapseMode;
      }

      public int getCollapseMode() {
         return this.mCollapseMode;
      }

      public void setParallaxMultiplier(float multiplier) {
         this.mParallaxMult = multiplier;
      }

      public float getParallaxMultiplier() {
         return this.mParallaxMult;
      }

      @Retention(RetentionPolicy.SOURCE)
      @interface CollapseMode {
      }
   }
}
