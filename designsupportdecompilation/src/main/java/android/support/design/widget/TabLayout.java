package android.support.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
 import nz.xbc.designsupportdecompilation.R.layout; import nz.xbc.designsupportdecompilation.R.style;  import nz.xbc.designsupportdecompilation.R.styleable;
import android.support.design.widget.AnimationUtils;
import android.support.design.widget.MathUtils;
import android.support.design.widget.ValueAnimatorCompat;
import android.support.design.widget.ViewUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.internal.widget.TintManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class TabLayout extends HorizontalScrollView {
   private static final int MAX_TAB_TEXT_LINES = 2;
   private static final int DEFAULT_HEIGHT = 48;
   private static final int TAB_MIN_WIDTH_MARGIN = 56;
   private static final int FIXED_WRAP_GUTTER_MIN = 16;
   private static final int MOTION_NON_ADJACENT_OFFSET = 24;
   private static final int ANIMATION_DURATION = 300;
   public static final int MODE_SCROLLABLE = 0;
   public static final int MODE_FIXED = 1;
   public static final int GRAVITY_FILL = 0;
   public static final int GRAVITY_CENTER = 1;
   private final ArrayList mTabs;
   private TabLayout.Tab mSelectedTab;
   private final TabLayout.SlidingTabStrip mTabStrip;
   private int mTabPaddingStart;
   private int mTabPaddingTop;
   private int mTabPaddingEnd;
   private int mTabPaddingBottom;
   private int mTabTextAppearance;
   private ColorStateList mTabTextColors;
   private final int mTabBackgroundResId;
   private final int mTabMinWidth;
   private int mTabMaxWidth;
   private final int mRequestedTabMaxWidth;
   private int mContentInsetStart;
   private int mTabGravity;
   private int mMode;
   private TabLayout.OnTabSelectedListener mOnTabSelectedListener;
   private OnClickListener mTabClickListener;
   private ValueAnimatorCompat mScrollAnimator;
   private ValueAnimatorCompat mIndicatorAnimator;

   public TabLayout(Context context) {
      this(context, (AttributeSet)null);
   }

   public TabLayout(Context context, AttributeSet attrs) {
      this(context, attrs, 0);
   }

   public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      this.mTabs = new ArrayList();
      this.setHorizontalScrollBarEnabled(false);
      this.setFillViewport(true);
      this.mTabStrip = new TabLayout.SlidingTabStrip(context);
      this.addView(this.mTabStrip, -2, -1);
      TypedArray a = context.obtainStyledAttributes(attrs, styleable.TabLayout, defStyleAttr, style.Widget_Design_TabLayout);
      this.mTabStrip.setSelectedIndicatorHeight(a.getDimensionPixelSize(styleable.TabLayout_tabIndicatorHeight, 0));
      this.mTabStrip.setSelectedIndicatorColor(a.getColor(styleable.TabLayout_tabIndicatorColor, 0));
      this.mTabTextAppearance = a.getResourceId(styleable.TabLayout_tabTextAppearance, style.TextAppearance_Design_Tab);
      this.mTabPaddingStart = this.mTabPaddingTop = this.mTabPaddingEnd = this.mTabPaddingBottom = a.getDimensionPixelSize(styleable.TabLayout_tabPadding, 0);
      this.mTabPaddingStart = a.getDimensionPixelSize(styleable.TabLayout_tabPaddingStart, this.mTabPaddingStart);
      this.mTabPaddingTop = a.getDimensionPixelSize(styleable.TabLayout_tabPaddingTop, this.mTabPaddingTop);
      this.mTabPaddingEnd = a.getDimensionPixelSize(styleable.TabLayout_tabPaddingEnd, this.mTabPaddingEnd);
      this.mTabPaddingBottom = a.getDimensionPixelSize(styleable.TabLayout_tabPaddingBottom, this.mTabPaddingBottom);
      this.mTabTextColors = this.loadTextColorFromTextAppearance(this.mTabTextAppearance);
      if(a.hasValue(styleable.TabLayout_tabTextColor)) {
         this.mTabTextColors = a.getColorStateList(styleable.TabLayout_tabTextColor);
      }

      if(a.hasValue(styleable.TabLayout_tabSelectedTextColor)) {
         int selected = a.getColor(styleable.TabLayout_tabSelectedTextColor, 0);
         this.mTabTextColors = createColorStateList(this.mTabTextColors.getDefaultColor(), selected);
      }

      this.mTabMinWidth = a.getDimensionPixelSize(styleable.TabLayout_tabMinWidth, 0);
      this.mRequestedTabMaxWidth = a.getDimensionPixelSize(styleable.TabLayout_tabMaxWidth, 0);
      this.mTabBackgroundResId = a.getResourceId(styleable.TabLayout_tabBackground, 0);
      this.mContentInsetStart = a.getDimensionPixelSize(styleable.TabLayout_tabContentStart, 0);
      this.mMode = a.getInt(styleable.TabLayout_tabMode, 1);
      this.mTabGravity = a.getInt(styleable.TabLayout_tabGravity, 0);
      a.recycle();
      this.applyModeAndGravity();
   }

   public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
      if(this.mIndicatorAnimator == null || !this.mIndicatorAnimator.isRunning()) {
         if(position >= 0 && position < this.mTabStrip.getChildCount()) {
            this.mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
            this.scrollTo(this.calculateScrollXForTab(position, positionOffset), 0);
            if(updateSelectedText) {
               this.setSelectedTabView(Math.round((float)position + positionOffset));
            }

         }
      }
   }

   public void addTab(@NonNull TabLayout.Tab tab) {
      this.addTab(tab, this.mTabs.isEmpty());
   }

   public void addTab(@NonNull TabLayout.Tab tab, int position) {
      this.addTab(tab, position, this.mTabs.isEmpty());
   }

   public void addTab(@NonNull TabLayout.Tab tab, boolean setSelected) {
      if(tab.mParent != this) {
         throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
      } else {
         this.addTabView(tab, setSelected);
         this.configureTab(tab, this.mTabs.size());
         if(setSelected) {
            tab.select();
         }

      }
   }

   public void addTab(@NonNull TabLayout.Tab tab, int position, boolean setSelected) {
      if(tab.mParent != this) {
         throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
      } else {
         this.addTabView(tab, position, setSelected);
         this.configureTab(tab, position);
         if(setSelected) {
            tab.select();
         }

      }
   }

   public void setOnTabSelectedListener(TabLayout.OnTabSelectedListener onTabSelectedListener) {
      this.mOnTabSelectedListener = onTabSelectedListener;
   }

   @NonNull
   public TabLayout.Tab newTab() {
      return new TabLayout.Tab(this);
   }

   public int getTabCount() {
      return this.mTabs.size();
   }

   @Nullable
   public TabLayout.Tab getTabAt(int index) {
      return (TabLayout.Tab)this.mTabs.get(index);
   }

   public int getSelectedTabPosition() {
      return this.mSelectedTab != null?this.mSelectedTab.getPosition():-1;
   }

   public void removeTab(TabLayout.Tab tab) {
      if(tab.mParent != this) {
         throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
      } else {
         this.removeTabAt(tab.getPosition());
      }
   }

   public void removeTabAt(int position) {
      int selectedTabPosition = this.mSelectedTab != null?this.mSelectedTab.getPosition():0;
      this.removeTabViewAt(position);
      TabLayout.Tab removedTab = (TabLayout.Tab)this.mTabs.remove(position);
      if(removedTab != null) {
         removedTab.setPosition(-1);
      }

      int newTabCount = this.mTabs.size();

      for(int i = position; i < newTabCount; ++i) {
         ((TabLayout.Tab)this.mTabs.get(i)).setPosition(i);
      }

      if(selectedTabPosition == position) {
         this.selectTab(this.mTabs.isEmpty()?null:(TabLayout.Tab)this.mTabs.get(Math.max(0, position - 1)));
      }

   }

   public void removeAllTabs() {
      this.mTabStrip.removeAllViews();
      Iterator i = this.mTabs.iterator();

      while(i.hasNext()) {
         TabLayout.Tab tab = (TabLayout.Tab)i.next();
         tab.setPosition(-1);
         i.remove();
      }

      this.mSelectedTab = null;
   }

   public void setTabMode(int mode) {
      if(mode != this.mMode) {
         this.mMode = mode;
         this.applyModeAndGravity();
      }

   }

   public int getTabMode() {
      return this.mMode;
   }

   public void setTabGravity(int gravity) {
      if(this.mTabGravity != gravity) {
         this.mTabGravity = gravity;
         this.applyModeAndGravity();
      }

   }

   public int getTabGravity() {
      return this.mTabGravity;
   }

   public void setTabTextColors(@Nullable ColorStateList textColor) {
      if(this.mTabTextColors != textColor) {
         this.mTabTextColors = textColor;
         this.updateAllTabs();
      }

   }

   @Nullable
   public ColorStateList getTabTextColors() {
      return this.mTabTextColors;
   }

   public void setTabTextColors(int normalColor, int selectedColor) {
      this.setTabTextColors(createColorStateList(normalColor, selectedColor));
   }

   public void setupWithViewPager(@NonNull ViewPager viewPager) {
      PagerAdapter adapter = viewPager.getAdapter();
      if(adapter == null) {
         throw new IllegalArgumentException("ViewPager does not have a PagerAdapter set");
      } else {
         this.setTabsFromPagerAdapter(adapter);
         viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(this));
         this.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
         if(this.mSelectedTab == null || this.mSelectedTab.getPosition() != viewPager.getCurrentItem()) {
            this.getTabAt(viewPager.getCurrentItem()).select();
         }

      }
   }

   public void setTabsFromPagerAdapter(@NonNull PagerAdapter adapter) {
      this.removeAllTabs();
      int i = 0;

      for(int count = adapter.getCount(); i < count; ++i) {
         this.addTab(this.newTab().setText(adapter.getPageTitle(i)));
      }

   }

   private void updateAllTabs() {
      int i = 0;

      for(int z = this.mTabStrip.getChildCount(); i < z; ++i) {
         this.updateTab(i);
      }

   }

   private TabLayout.TabView createTabView(TabLayout.Tab tab) {
      TabLayout.TabView tabView = new TabLayout.TabView(this.getContext(), tab);
      tabView.setFocusable(true);
      if(this.mTabClickListener == null) {
         this.mTabClickListener = new OnClickListener() {
            public void onClick(View view) {
               TabLayout.TabView tabView = (TabLayout.TabView)view;
               tabView.getTab().select();
            }
         };
      }

      tabView.setOnClickListener(this.mTabClickListener);
      return tabView;
   }

   private void configureTab(TabLayout.Tab tab, int position) {
      tab.setPosition(position);
      this.mTabs.add(position, tab);
      int count = this.mTabs.size();

      for(int i = position + 1; i < count; ++i) {
         ((TabLayout.Tab)this.mTabs.get(i)).setPosition(i);
      }

   }

   private void updateTab(int position) {
      TabLayout.TabView view = (TabLayout.TabView)this.mTabStrip.getChildAt(position);
      if(view != null) {
         view.update();
      }

   }

   private void addTabView(TabLayout.Tab tab, boolean setSelected) {
      TabLayout.TabView tabView = this.createTabView(tab);
      this.mTabStrip.addView(tabView, this.createLayoutParamsForTabs());
      if(setSelected) {
         tabView.setSelected(true);
      }

   }

   private void addTabView(TabLayout.Tab tab, int position, boolean setSelected) {
      TabLayout.TabView tabView = this.createTabView(tab);
      this.mTabStrip.addView(tabView, position, this.createLayoutParamsForTabs());
      if(setSelected) {
         tabView.setSelected(true);
      }

   }

   private LinearLayout.LayoutParams createLayoutParamsForTabs() {
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -1);
      this.updateTabViewLayoutParams(lp);
      return lp;
   }

   private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp) {
      if(this.mMode == 1 && this.mTabGravity == 0) {
         lp.width = 0;
         lp.weight = 1.0F;
      } else {
         lp.width = -2;
         lp.weight = 0.0F;
      }

   }

   private int dpToPx(int dps) {
      return Math.round(this.getResources().getDisplayMetrics().density * (float)dps);
   }

   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int idealHeight = this.dpToPx(48) + this.getPaddingTop() + this.getPaddingBottom();
      switch(MeasureSpec.getMode(heightMeasureSpec)) {
      case Integer.MIN_VALUE:
         heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)), 1073741824);
         break;
      case 0:
         heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, 1073741824);
      }

      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      int defaultTabMaxWidth;
      if(this.mMode == 1 && this.getChildCount() == 1) {
         View maxTabWidth = this.getChildAt(0);
         defaultTabMaxWidth = this.getMeasuredWidth();
         if(maxTabWidth.getMeasuredWidth() > defaultTabMaxWidth) {
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), maxTabWidth.getLayoutParams().height);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(defaultTabMaxWidth, 1073741824);
            maxTabWidth.measure(childWidthMeasureSpec, childHeightMeasureSpec);
         }
      }

      int maxTabWidth1 = this.mRequestedTabMaxWidth;
      defaultTabMaxWidth = this.getMeasuredWidth() - this.dpToPx(56);
      if(maxTabWidth1 == 0 || maxTabWidth1 > defaultTabMaxWidth) {
         maxTabWidth1 = defaultTabMaxWidth;
      }

      this.mTabMaxWidth = maxTabWidth1;
   }

   private void removeTabViewAt(int position) {
      this.mTabStrip.removeViewAt(position);
      this.requestLayout();
   }

   private void animateToTab(int newPosition) {
      if(newPosition != -1) {
         if(this.getWindowToken() != null && ViewCompat.isLaidOut(this) && !this.mTabStrip.childrenNeedLayout()) {
            int startScrollX = this.getScrollX();
            int targetScrollX = this.calculateScrollXForTab(newPosition, 0.0F);
            if(startScrollX != targetScrollX) {
               if(this.mScrollAnimator == null) {
                  this.mScrollAnimator = ViewUtils.createAnimator();
                  this.mScrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                  this.mScrollAnimator.setDuration(300);
                  this.mScrollAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                     public void onAnimationUpdate(ValueAnimatorCompat animator) {
                        TabLayout.this.scrollTo(animator.getAnimatedIntValue(), 0);
                     }
                  });
               }

               this.mScrollAnimator.setIntValues(startScrollX, targetScrollX);
               this.mScrollAnimator.start();
            }

            this.mTabStrip.animateIndicatorToPosition(newPosition, 300);
         } else {
            this.setScrollPosition(newPosition, 0.0F, true);
         }
      }
   }

   private void setSelectedTabView(int position) {
      int tabCount = this.mTabStrip.getChildCount();

      for(int i = 0; i < tabCount; ++i) {
         View child = this.mTabStrip.getChildAt(i);
         child.setSelected(i == position);
      }

   }

   void selectTab(TabLayout.Tab tab) {
      this.selectTab(tab, true);
   }

   void selectTab(TabLayout.Tab tab, boolean updateIndicator) {
      if(this.mSelectedTab == tab) {
         if(this.mSelectedTab != null) {
            if(this.mOnTabSelectedListener != null) {
               this.mOnTabSelectedListener.onTabReselected(this.mSelectedTab);
            }

            this.animateToTab(tab.getPosition());
         }
      } else {
         int newPosition = tab != null?tab.getPosition():-1;
         this.setSelectedTabView(newPosition);
         if(updateIndicator) {
            if((this.mSelectedTab == null || this.mSelectedTab.getPosition() == -1) && newPosition != -1) {
               this.setScrollPosition(newPosition, 0.0F, true);
            } else {
               this.animateToTab(newPosition);
            }
         }

         if(this.mSelectedTab != null && this.mOnTabSelectedListener != null) {
            this.mOnTabSelectedListener.onTabUnselected(this.mSelectedTab);
         }

         this.mSelectedTab = tab;
         if(this.mSelectedTab != null && this.mOnTabSelectedListener != null) {
            this.mOnTabSelectedListener.onTabSelected(this.mSelectedTab);
         }
      }

   }

   private int calculateScrollXForTab(int position, float positionOffset) {
      if(this.mMode == 0) {
         View selectedChild = this.mTabStrip.getChildAt(position);
         View nextChild = position + 1 < this.mTabStrip.getChildCount()?this.mTabStrip.getChildAt(position + 1):null;
         int selectedWidth = selectedChild != null?selectedChild.getWidth():0;
         int nextWidth = nextChild != null?nextChild.getWidth():0;
         return selectedChild.getLeft() + (int)((float)(selectedWidth + nextWidth) * positionOffset * 0.5F) + selectedChild.getWidth() / 2 - this.getWidth() / 2;
      } else {
         return 0;
      }
   }

   private void applyModeAndGravity() {
      int paddingStart = 0;
      if(this.mMode == 0) {
         paddingStart = Math.max(0, this.mContentInsetStart - this.mTabPaddingStart);
      }

      ViewCompat.setPaddingRelative(this.mTabStrip, paddingStart, 0, 0, 0);
      switch(this.mMode) {
      case 0:
         this.mTabStrip.setGravity(8388611);
         break;
      case 1:
         this.mTabStrip.setGravity(1);
      }

      this.updateTabViewsLayoutParams();
   }

   private void updateTabViewsLayoutParams() {
      for(int i = 0; i < this.mTabStrip.getChildCount(); ++i) {
         View child = this.mTabStrip.getChildAt(i);
         this.updateTabViewLayoutParams((LinearLayout.LayoutParams)child.getLayoutParams());
         child.requestLayout();
      }

   }

   private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
      int[][] states = new int[2][];
      int[] colors = new int[2];
      byte i = 0;
      states[i] = SELECTED_STATE_SET;
      colors[i] = selectedColor;
      int var5 = i + 1;
      states[var5] = EMPTY_STATE_SET;
      colors[var5] = defaultColor;
      ++var5;
      return new ColorStateList(states, colors);
   }

   private ColorStateList loadTextColorFromTextAppearance(int textAppearanceResId) {
      TypedArray a = this.getContext().obtainStyledAttributes(textAppearanceResId, styleable.TextAppearance);

      ColorStateList var3;
      try {
         var3 = a.getColorStateList(styleable.TextAppearance_android_textColor);
      } finally {
         a.recycle();
      }

      return var3;
   }

   public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
      private final ViewPager mViewPager;

      public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
         this.mViewPager = viewPager;
      }

      public void onTabSelected(TabLayout.Tab tab) {
         this.mViewPager.setCurrentItem(tab.getPosition());
      }

      public void onTabUnselected(TabLayout.Tab tab) {
      }

      public void onTabReselected(TabLayout.Tab tab) {
      }
   }

   public static class TabLayoutOnPageChangeListener implements OnPageChangeListener {
      private final WeakReference mTabLayoutRef;
      private int mPreviousScrollState;
      private int mScrollState;

      public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
         this.mTabLayoutRef = new WeakReference(tabLayout);
      }

      public void onPageScrollStateChanged(int state) {
         this.mPreviousScrollState = this.mScrollState;
         this.mScrollState = state;
      }

      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
         TabLayout tabLayout = (TabLayout)this.mTabLayoutRef.get();
         if(tabLayout != null) {
            boolean updateText = this.mScrollState == 1 || this.mScrollState == 2 && this.mPreviousScrollState == 1;
            tabLayout.setScrollPosition(position, positionOffset, updateText);
         }

      }

      public void onPageSelected(int position) {
         TabLayout tabLayout = (TabLayout)this.mTabLayoutRef.get();
         if(tabLayout != null) {
            tabLayout.selectTab(tabLayout.getTabAt(position), this.mScrollState == 0);
         }

      }
   }

   private class SlidingTabStrip extends LinearLayout {
      private int mSelectedIndicatorHeight;
      private final Paint mSelectedIndicatorPaint;
      private int mSelectedPosition = -1;
      private float mSelectionOffset;
      private int mIndicatorLeft = -1;
      private int mIndicatorRight = -1;

      SlidingTabStrip(Context context) {
         super(context);
         this.setWillNotDraw(false);
         this.mSelectedIndicatorPaint = new Paint();
      }

      void setSelectedIndicatorColor(int color) {
         this.mSelectedIndicatorPaint.setColor(color);
         ViewCompat.postInvalidateOnAnimation(this);
      }

      void setSelectedIndicatorHeight(int height) {
         this.mSelectedIndicatorHeight = height;
         ViewCompat.postInvalidateOnAnimation(this);
      }

      boolean childrenNeedLayout() {
         int i = 0;

         for(int z = this.getChildCount(); i < z; ++i) {
            View child = this.getChildAt(i);
            if(child.getWidth() <= 0) {
               return true;
            }
         }

         return false;
      }

      void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
         if(TabLayout.this.mIndicatorAnimator == null || !TabLayout.this.mIndicatorAnimator.isRunning()) {
            this.mSelectedPosition = position;
            this.mSelectionOffset = positionOffset;
            this.updateIndicatorPosition();
         }
      }

      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         if(MeasureSpec.getMode(widthMeasureSpec) == 1073741824) {
            if(TabLayout.this.mMode == 1 && TabLayout.this.mTabGravity == 1) {
               int count = this.getChildCount();
               int unspecifiedSpec = MeasureSpec.makeMeasureSpec(0, 0);
               int largestTabWidth = 0;
               int gutter = 0;

               int i;
               View child;
               for(i = count; gutter < i; ++gutter) {
                  child = this.getChildAt(gutter);
                  child.measure(unspecifiedSpec, heightMeasureSpec);
                  largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
               }

               if(largestTabWidth <= 0) {
                  return;
               }

               gutter = TabLayout.this.dpToPx(16);
               if(largestTabWidth * count <= this.getMeasuredWidth() - gutter * 2) {
                  for(i = 0; i < count; ++i) {
                     child = this.getChildAt(i);
                     LayoutParams lp = (LayoutParams)child.getLayoutParams();
                     lp.width = largestTabWidth;
                     lp.weight = 0.0F;
                  }
               } else {
                  TabLayout.this.mTabGravity = 0;
                  TabLayout.this.updateTabViewsLayoutParams();
               }

               super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

         }
      }

      protected void onLayout(boolean changed, int l, int t, int r, int b) {
         super.onLayout(changed, l, t, r, b);
         this.updateIndicatorPosition();
      }

      private void updateIndicatorPosition() {
         View selectedTitle = this.getChildAt(this.mSelectedPosition);
         int left;
         int right;
         if(selectedTitle != null && selectedTitle.getWidth() > 0) {
            left = selectedTitle.getLeft();
            right = selectedTitle.getRight();
            if(this.mSelectionOffset > 0.0F && this.mSelectedPosition < this.getChildCount() - 1) {
               View nextTitle = this.getChildAt(this.mSelectedPosition + 1);
               left = (int)(this.mSelectionOffset * (float)nextTitle.getLeft() + (1.0F - this.mSelectionOffset) * (float)left);
               right = (int)(this.mSelectionOffset * (float)nextTitle.getRight() + (1.0F - this.mSelectionOffset) * (float)right);
            }
         } else {
            right = -1;
            left = -1;
         }

         this.setIndicatorPosition(left, right);
      }

      private void setIndicatorPosition(int left, int right) {
         if(left != this.mIndicatorLeft || right != this.mIndicatorRight) {
            this.mIndicatorLeft = left;
            this.mIndicatorRight = right;
            ViewCompat.postInvalidateOnAnimation(this);
         }

      }

      void animateIndicatorToPosition(final int position, int duration) {
         boolean isRtl = ViewCompat.getLayoutDirection(this) == 1;
         View targetView = this.getChildAt(position);
         final int targetLeft = targetView.getLeft();
         final int targetRight = targetView.getRight();
         final int startLeft;
         final int startRight;
         if(Math.abs(position - this.mSelectedPosition) <= 1) {
            startLeft = this.mIndicatorLeft;
            startRight = this.mIndicatorRight;
         } else {
            int animator = TabLayout.this.dpToPx(24);
            if(position < this.mSelectedPosition) {
               if(isRtl) {
                  startLeft = startRight = targetLeft - animator;
               } else {
                  startLeft = startRight = targetRight + animator;
               }
            } else if(isRtl) {
               startLeft = startRight = targetRight + animator;
            } else {
               startLeft = startRight = targetLeft - animator;
            }
         }

         if(startLeft != targetLeft || startRight != targetRight) {
            ValueAnimatorCompat animator1 = TabLayout.this.mIndicatorAnimator = ViewUtils.createAnimator();
            animator1.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            animator1.setDuration(duration);
            animator1.setFloatValues(0.0F, 1.0F);
            animator1.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
               public void onAnimationUpdate(ValueAnimatorCompat animator) {
                  float fraction = animator.getAnimatedFraction();
                  SlidingTabStrip.this.setIndicatorPosition(AnimationUtils.lerp(startLeft, targetLeft, fraction), AnimationUtils.lerp(startRight, targetRight, fraction));
               }
            });
            animator1.setListener(new ValueAnimatorCompat.AnimatorListenerAdapter() {
               public void onAnimationEnd(ValueAnimatorCompat animator) {
                  SlidingTabStrip.this.mSelectedPosition = position;
                  SlidingTabStrip.this.mSelectionOffset = 0.0F;
               }

               public void onAnimationCancel(ValueAnimatorCompat animator) {
                  SlidingTabStrip.this.mSelectedPosition = position;
                  SlidingTabStrip.this.mSelectionOffset = 0.0F;
               }
            });
            animator1.start();
         }

      }

      public void draw(Canvas canvas) {
         super.draw(canvas);
         if(this.mIndicatorLeft >= 0 && this.mIndicatorRight > this.mIndicatorLeft) {
            canvas.drawRect((float)this.mIndicatorLeft, (float)(this.getHeight() - this.mSelectedIndicatorHeight), (float)this.mIndicatorRight, (float)this.getHeight(), this.mSelectedIndicatorPaint);
         }

      }
   }

   class TabView extends LinearLayout implements OnLongClickListener {
      private final TabLayout.Tab mTab;
      private TextView mTextView;
      private ImageView mIconView;
      private View mCustomView;
      private TextView mCustomTextView;
      private ImageView mCustomIconView;

      public TabView(Context context, TabLayout.Tab tab) {
         super(context);
         this.mTab = tab;
         if(TabLayout.this.mTabBackgroundResId != 0) {
            this.setBackgroundDrawable(TintManager.getDrawable(context, TabLayout.this.mTabBackgroundResId));
         }

         ViewCompat.setPaddingRelative(this, TabLayout.this.mTabPaddingStart, TabLayout.this.mTabPaddingTop, TabLayout.this.mTabPaddingEnd, TabLayout.this.mTabPaddingBottom);
         this.setGravity(17);
         this.update();
      }

      public void setSelected(boolean selected) {
         boolean changed = this.isSelected() != selected;
         super.setSelected(selected);
         if(changed && selected) {
            this.sendAccessibilityEvent(4);
            if(this.mTextView != null) {
               this.mTextView.setSelected(selected);
            }

            if(this.mIconView != null) {
               this.mIconView.setSelected(selected);
            }
         }

      }

      @TargetApi(14)
      public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
         super.onInitializeAccessibilityEvent(event);
         event.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
      }

      @TargetApi(14)
      public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
         super.onInitializeAccessibilityNodeInfo(info);
         info.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
      }

      public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         int measuredWidth = this.getMeasuredWidth();
         if(measuredWidth < TabLayout.this.mTabMinWidth || measuredWidth > TabLayout.this.mTabMaxWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(MathUtils.constrain(measuredWidth, TabLayout.this.mTabMinWidth, TabLayout.this.mTabMaxWidth), 1073741824);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         }

      }

      final void update() {
         TabLayout.Tab tab = this.mTab;
         View custom = tab.getCustomView();
         if(custom != null) {
            ViewParent textView = custom.getParent();
            if(textView != this) {
               if(textView != null) {
                  ((ViewGroup)textView).removeView(custom);
               }

               this.addView(custom);
            }

            this.mCustomView = custom;
            if(this.mTextView != null) {
               this.mTextView.setVisibility(8);
            }

            if(this.mIconView != null) {
               this.mIconView.setVisibility(8);
               this.mIconView.setImageDrawable((Drawable)null);
            }

            this.mCustomTextView = (TextView)custom.findViewById(16908308);
            this.mCustomIconView = (ImageView)custom.findViewById(16908294);
         } else {
            if(this.mCustomView != null) {
               this.removeView(this.mCustomView);
               this.mCustomView = null;
            }

            this.mCustomTextView = null;
            this.mCustomIconView = null;
         }

         if(this.mCustomView == null) {
            if(this.mIconView == null) {
               ImageView textView1 = (ImageView)LayoutInflater.from(this.getContext()).inflate(layout.layout_tab_icon, this, false);
               this.addView(textView1, 0);
               this.mIconView = textView1;
            }

            if(this.mTextView == null) {
               TextView textView2 = (TextView)LayoutInflater.from(this.getContext()).inflate(layout.layout_tab_text, this, false);
               this.addView(textView2);
               this.mTextView = textView2;
            }

            this.mTextView.setTextAppearance(this.getContext(), TabLayout.this.mTabTextAppearance);
            if(TabLayout.this.mTabTextColors != null) {
               this.mTextView.setTextColor(TabLayout.this.mTabTextColors);
            }

            this.updateTextAndIcon(tab, this.mTextView, this.mIconView);
         } else if(this.mCustomTextView != null || this.mCustomIconView != null) {
            this.updateTextAndIcon(tab, this.mCustomTextView, this.mCustomIconView);
         }

      }

      private void updateTextAndIcon(TabLayout.Tab tab, TextView textView, ImageView iconView) {
         Drawable icon = tab.getIcon();
         CharSequence text = tab.getText();
         if(iconView != null) {
            if(icon != null) {
               iconView.setImageDrawable(icon);
               iconView.setVisibility(0);
               this.setVisibility(0);
            } else {
               iconView.setVisibility(8);
               iconView.setImageDrawable((Drawable)null);
            }

            iconView.setContentDescription(tab.getContentDescription());
         }

         boolean hasText = !TextUtils.isEmpty(text);
         if(textView != null) {
            if(hasText) {
               textView.setText(text);
               textView.setContentDescription(tab.getContentDescription());
               textView.setVisibility(0);
               this.setVisibility(0);
            } else {
               textView.setVisibility(8);
               textView.setText((CharSequence)null);
            }
         }

         if(!hasText && !TextUtils.isEmpty(tab.getContentDescription())) {
            this.setOnLongClickListener(this);
         } else {
            this.setOnLongClickListener((OnLongClickListener)null);
            this.setLongClickable(false);
         }

      }

      public boolean onLongClick(View v) {
         int[] screenPos = new int[2];
         this.getLocationOnScreen(screenPos);
         Context context = this.getContext();
         int width = this.getWidth();
         int height = this.getHeight();
         int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
         Toast cheatSheet = Toast.makeText(context, this.mTab.getContentDescription(), 0);
         cheatSheet.setGravity(49, screenPos[0] + width / 2 - screenWidth / 2, height);
         cheatSheet.show();
         return true;
      }

      public TabLayout.Tab getTab() {
         return this.mTab;
      }
   }

   public static final class Tab {
      public static final int INVALID_POSITION = -1;
      private Object mTag;
      private Drawable mIcon;
      private CharSequence mText;
      private CharSequence mContentDesc;
      private int mPosition = -1;
      private View mCustomView;
      private final TabLayout mParent;

      Tab(TabLayout parent) {
         this.mParent = parent;
      }

      @Nullable
      public Object getTag() {
         return this.mTag;
      }

      @NonNull
      public TabLayout.Tab setTag(@Nullable Object tag) {
         this.mTag = tag;
         return this;
      }

      View getCustomView() {
         return this.mCustomView;
      }

      @NonNull
      public TabLayout.Tab setCustomView(@Nullable View view) {
         this.mCustomView = view;
         if(this.mPosition >= 0) {
            this.mParent.updateTab(this.mPosition);
         }

         return this;
      }

      @NonNull
      public TabLayout.Tab setCustomView(@LayoutRes int layoutResId) {
         return this.setCustomView(LayoutInflater.from(this.mParent.getContext()).inflate(layoutResId, (ViewGroup)null));
      }

      @Nullable
      public Drawable getIcon() {
         return this.mIcon;
      }

      public int getPosition() {
         return this.mPosition;
      }

      void setPosition(int position) {
         this.mPosition = position;
      }

      @Nullable
      public CharSequence getText() {
         return this.mText;
      }

      @NonNull
      public TabLayout.Tab setIcon(@Nullable Drawable icon) {
         this.mIcon = icon;
         if(this.mPosition >= 0) {
            this.mParent.updateTab(this.mPosition);
         }

         return this;
      }

      @NonNull
      public TabLayout.Tab setIcon(@DrawableRes int resId) {
         return this.setIcon(TintManager.getDrawable(this.mParent.getContext(), resId));
      }

      @NonNull
      public TabLayout.Tab setText(@Nullable CharSequence text) {
         this.mText = text;
         if(this.mPosition >= 0) {
            this.mParent.updateTab(this.mPosition);
         }

         return this;
      }

      @NonNull
      public TabLayout.Tab setText(@StringRes int resId) {
         return this.setText(this.mParent.getResources().getText(resId));
      }

      public void select() {
         this.mParent.selectTab(this);
      }

      public boolean isSelected() {
         return this.mParent.getSelectedTabPosition() == this.mPosition;
      }

      @NonNull
      public TabLayout.Tab setContentDescription(@StringRes int resId) {
         return this.setContentDescription(this.mParent.getResources().getText(resId));
      }

      @NonNull
      public TabLayout.Tab setContentDescription(@Nullable CharSequence contentDesc) {
         this.mContentDesc = contentDesc;
         if(this.mPosition >= 0) {
            this.mParent.updateTab(this.mPosition);
         }

         return this;
      }

      @Nullable
      public CharSequence getContentDescription() {
         return this.mContentDesc;
      }
   }

   public interface OnTabSelectedListener {
      void onTabSelected(TabLayout.Tab var1);

      void onTabUnselected(TabLayout.Tab var1);

      void onTabReselected(TabLayout.Tab var1);
   }

   @Retention(RetentionPolicy.SOURCE)
   public @interface TabGravity {
   }

   @Retention(RetentionPolicy.SOURCE)
   public @interface Mode {
   }
}
