package android.support.design.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Build.VERSION;
import android.os.Parcelable.Creator; import nz.xbc.designsupportdecompilation.R.style;  import nz.xbc.designsupportdecompilation.R.styleable;
import android.support.design.widget.CoordinatorLayoutInsetsHelper;
import android.support.design.widget.CoordinatorLayoutInsetsHelperLollipop;
import android.support.design.widget.ViewGroupUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoordinatorLayout extends ViewGroup implements NestedScrollingParent {
   static final String TAG = "CoordinatorLayout";
   static final String WIDGET_PACKAGE_NAME = CoordinatorLayout.class.getPackage().getName();
   static final Class[] CONSTRUCTOR_PARAMS;
   static final ThreadLocal sConstructors;
   final Comparator mLayoutDependencyComparator;
   static final Comparator TOP_SORTED_CHILDREN_COMPARATOR;
   static final CoordinatorLayoutInsetsHelper INSETS_HELPER;
   private final List mDependencySortedChildren;
   private final List mTempList1;
   private final List mTempDependenciesList;
   private final Rect mTempRect1;
   private final Rect mTempRect2;
   private final Rect mTempRect3;
   private final int[] mTempIntPair;
   private Paint mScrimPaint;
   private boolean mIsAttachedToWindow;
   private int[] mKeylines;
   private View mBehaviorTouchView;
   private View mNestedScrollingDirectChild;
   private View mNestedScrollingTarget;
   private CoordinatorLayout.OnPreDrawListener mOnPreDrawListener;
   private boolean mNeedsPreDrawListener;
   private WindowInsetsCompat mLastInsets;
   private boolean mDrawStatusBarBackground;
   private Drawable mStatusBarBackground;
   private OnHierarchyChangeListener mOnHierarchyChangeListener;
   private final NestedScrollingParentHelper mNestedScrollingParentHelper;

   public CoordinatorLayout(Context context) {
      this(context, (AttributeSet)null);
   }

   public CoordinatorLayout(Context context, AttributeSet attrs) {
      this(context, attrs, 0);
   }

   public CoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      this.mLayoutDependencyComparator = new Comparator<View>() {
         public int compare(View lhs, View rhs) {
            return lhs == rhs?0:(((CoordinatorLayout.LayoutParams)lhs.getLayoutParams()).dependsOn(CoordinatorLayout.this, lhs, rhs)?1:(((CoordinatorLayout.LayoutParams)rhs.getLayoutParams()).dependsOn(CoordinatorLayout.this, rhs, lhs)?-1:0));
         }
      };
      this.mDependencySortedChildren = new ArrayList();
      this.mTempList1 = new ArrayList();
      this.mTempDependenciesList = new ArrayList();
      this.mTempRect1 = new Rect();
      this.mTempRect2 = new Rect();
      this.mTempRect3 = new Rect();
      this.mTempIntPair = new int[2];
      this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
      TypedArray a = context.obtainStyledAttributes(attrs, styleable.CoordinatorLayout, defStyleAttr, style.Widget_Design_CoordinatorLayout);
      int keylineArrayRes = a.getResourceId(styleable.CoordinatorLayout_keylines, 0);
      if(keylineArrayRes != 0) {
         Resources res = context.getResources();
         this.mKeylines = res.getIntArray(keylineArrayRes);
         float density = res.getDisplayMetrics().density;
         int count = this.mKeylines.length;

         for(int i = 0; i < count; ++i) {
            this.mKeylines[i] = (int)((float)this.mKeylines[i] * density);
         }
      }

      this.mStatusBarBackground = a.getDrawable(styleable.CoordinatorLayout_statusBarBackground);
      a.recycle();
      if(INSETS_HELPER != null) {
         INSETS_HELPER.setupForWindowInsets(this, new CoordinatorLayout.ApplyInsetsListener());
      }

      super.setOnHierarchyChangeListener(new CoordinatorLayout.HierarchyChangeListener());
   }

   public void setOnHierarchyChangeListener(OnHierarchyChangeListener onHierarchyChangeListener) {
      this.mOnHierarchyChangeListener = onHierarchyChangeListener;
   }

   public void onAttachedToWindow() {
      super.onAttachedToWindow();
      this.resetTouchBehaviors();
      if(this.mNeedsPreDrawListener) {
         if(this.mOnPreDrawListener == null) {
            this.mOnPreDrawListener = new CoordinatorLayout.OnPreDrawListener();
         }

         ViewTreeObserver vto = this.getViewTreeObserver();
         vto.addOnPreDrawListener(this.mOnPreDrawListener);
      }

      this.mIsAttachedToWindow = true;
   }

   public void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      this.resetTouchBehaviors();
      if(this.mNeedsPreDrawListener && this.mOnPreDrawListener != null) {
         ViewTreeObserver vto = this.getViewTreeObserver();
         vto.removeOnPreDrawListener(this.mOnPreDrawListener);
      }

      if(this.mNestedScrollingTarget != null) {
         this.onStopNestedScroll(this.mNestedScrollingTarget);
      }

      this.mIsAttachedToWindow = false;
   }

   public void setStatusBarBackground(Drawable bg) {
      this.mStatusBarBackground = bg;
      this.invalidate();
   }

   public Drawable getStatusBarBackground() {
      return this.mStatusBarBackground;
   }

   public void setStatusBarBackgroundResource(int resId) {
      this.setStatusBarBackground(resId != 0?ContextCompat.getDrawable(this.getContext(), resId):null);
   }

   public void setStatusBarBackgroundColor(int color) {
      this.setStatusBarBackground(new ColorDrawable(color));
   }

   private void setWindowInsets(WindowInsetsCompat insets) {
      if(this.mLastInsets != insets) {
         this.mLastInsets = insets;
         this.mDrawStatusBarBackground = insets != null && insets.getSystemWindowInsetTop() > 0;
         this.setWillNotDraw(!this.mDrawStatusBarBackground && this.getBackground() == null);
         this.dispatchChildApplyWindowInsets(insets);
         this.requestLayout();
      }

   }

   private void resetTouchBehaviors() {
      if(this.mBehaviorTouchView != null) {
         CoordinatorLayout.Behavior childCount = ((CoordinatorLayout.LayoutParams)this.mBehaviorTouchView.getLayoutParams()).getBehavior();
         if(childCount != null) {
            long i = SystemClock.uptimeMillis();
            MotionEvent lp = MotionEvent.obtain(i, i, 3, 0.0F, 0.0F, 0);
            childCount.onTouchEvent(this, this.mBehaviorTouchView, lp);
            lp.recycle();
         }

         this.mBehaviorTouchView = null;
      }

      int var5 = this.getChildCount();

      for(int var6 = 0; var6 < var5; ++var6) {
         View child = this.getChildAt(var6);
         CoordinatorLayout.LayoutParams var7 = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         var7.resetTouchBehaviorTracking();
      }

   }

   private void getTopSortedChildren(List out) {
      out.clear();
      boolean useCustomOrder = this.isChildrenDrawingOrderEnabled();
      int childCount = this.getChildCount();

      for(int i = childCount - 1; i >= 0; --i) {
         int childIndex = useCustomOrder?this.getChildDrawingOrder(childCount, i):i;
         View child = this.getChildAt(childIndex);
         out.add(child);
      }

      if(TOP_SORTED_CHILDREN_COMPARATOR != null) {
         Collections.sort(out, TOP_SORTED_CHILDREN_COMPARATOR);
      }

   }

   private boolean performIntercept(MotionEvent ev) {
      boolean intercepted = false;
      boolean newBlock = false;
      MotionEvent cancelEvent = null;
      int action = MotionEventCompat.getActionMasked(ev);
      List topmostChildList = this.mTempList1;
      this.getTopSortedChildren(topmostChildList);
      int childCount = topmostChildList.size();

      for(int i = 0; i < childCount; ++i) {
         View child = (View)topmostChildList.get(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         CoordinatorLayout.Behavior b = lp.getBehavior();
         if((intercepted || newBlock) && action != 0) {
            if(b != null) {
               if(cancelEvent != null) {
                  long var14 = SystemClock.uptimeMillis();
                  cancelEvent = MotionEvent.obtain(var14, var14, 3, 0.0F, 0.0F, 0);
               }

               b.onInterceptTouchEvent(this, child, cancelEvent);
            }
         } else {
            if(!intercepted && b != null && (intercepted = b.onInterceptTouchEvent(this, child, ev))) {
               this.mBehaviorTouchView = child;
            }

            boolean wasBlocking = lp.didBlockInteraction();
            boolean isBlocking = lp.isBlockingInteractionBelow(this, child);
            newBlock = isBlocking && !wasBlocking;
            if(isBlocking && !newBlock) {
               break;
            }
         }
      }

      topmostChildList.clear();
      return intercepted;
   }

   public boolean onInterceptTouchEvent(MotionEvent ev) {
      Object cancelEvent = null;
      int action = MotionEventCompat.getActionMasked(ev);
      if(action == 0) {
         this.resetTouchBehaviors();
      }

      boolean intercepted = this.performIntercept(ev);
      if(cancelEvent != null) {
         ((MotionEvent)cancelEvent).recycle();
      }

      if(action == 1 || action == 3) {
         this.resetTouchBehaviors();
      }

      return intercepted;
   }

   public boolean onTouchEvent(MotionEvent ev) {
      boolean handled = false;
      boolean cancelSuper = false;
      MotionEvent cancelEvent = null;
      int action = MotionEventCompat.getActionMasked(ev);
      if(this.mBehaviorTouchView != null || (cancelSuper = this.performIntercept(ev))) {
         CoordinatorLayout.LayoutParams now = (CoordinatorLayout.LayoutParams)this.mBehaviorTouchView.getLayoutParams();
         CoordinatorLayout.Behavior b = now.getBehavior();
         if(b != null) {
            b.onTouchEvent(this, this.mBehaviorTouchView, ev);
         }
      }

      if(this.mBehaviorTouchView == null) {
         handled |= super.onTouchEvent(ev);
      } else if(cancelSuper) {
         if(cancelEvent != null) {
            long now1 = SystemClock.uptimeMillis();
            cancelEvent = MotionEvent.obtain(now1, now1, 3, 0.0F, 0.0F, 0);
         }

         super.onTouchEvent(cancelEvent);
      }

      if(!handled && action == 0) {
         ;
      }

      if(cancelEvent != null) {
         cancelEvent.recycle();
      }

      if(action == 1 || action == 3) {
         this.resetTouchBehaviors();
      }

      return handled;
   }

   public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
      super.requestDisallowInterceptTouchEvent(disallowIntercept);
      if(disallowIntercept) {
         this.resetTouchBehaviors();
      }

   }

   private int getKeyline(int index) {
      if(this.mKeylines == null) {
         Log.e("CoordinatorLayout", "No keylines defined for " + this + " - attempted index lookup " + index);
         return 0;
      } else if(index >= 0 && index < this.mKeylines.length) {
         return this.mKeylines[index];
      } else {
         Log.e("CoordinatorLayout", "Keyline index " + index + " out of range for " + this);
         return 0;
      }
   }

   static CoordinatorLayout.Behavior parseBehavior(Context context, AttributeSet attrs, String name) {
      if(TextUtils.isEmpty(name)) {
         return null;
      } else {
         String fullName;
         if(name.startsWith(".")) {
            fullName = context.getPackageName() + name;
         } else if(name.indexOf(46) >= 0) {
            fullName = name;
         } else {
            fullName = WIDGET_PACKAGE_NAME + '.' + name;
         }

         try {
            Object e = (Map)sConstructors.get();
            if(e == null) {
               e = new HashMap();
               sConstructors.set(e);
            }

            Constructor c = (Constructor)((Map)e).get(fullName);
            if(c == null) {
               Class clazz = Class.forName(fullName, true, context.getClassLoader());
               c = clazz.getConstructor(CONSTRUCTOR_PARAMS);
               ((Map)e).put(fullName, c);
            }

            return (CoordinatorLayout.Behavior)c.newInstance(new Object[]{context, attrs});
         } catch (Exception var7) {
            throw new RuntimeException("Could not inflate Behavior subclass " + fullName, var7);
         }
      }
   }

   CoordinatorLayout.LayoutParams getResolvedLayoutParams(View child) {
      CoordinatorLayout.LayoutParams result = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      if(!result.mBehaviorResolved) {
         Class childClass = child.getClass();

         CoordinatorLayout.DefaultBehavior defaultBehavior;
         for(defaultBehavior = null; childClass != null && (defaultBehavior = (CoordinatorLayout.DefaultBehavior)childClass.getAnnotation(CoordinatorLayout.DefaultBehavior.class)) == null; childClass = childClass.getSuperclass()) {
            ;
         }

         if(defaultBehavior != null) {
            try {
               result.setBehavior((CoordinatorLayout.Behavior)defaultBehavior.value().newInstance());
            } catch (Exception var6) {
               Log.e("CoordinatorLayout", "Default behavior class " + defaultBehavior.value().getName() + " could not be instantiated. Did you forget a default constructor?", var6);
            }
         }

         result.mBehaviorResolved = true;
      }

      return result;
   }

   private void prepareChildren() {
      int childCount = this.getChildCount();
      boolean resortRequired = this.mDependencySortedChildren.size() != childCount;

      int i;
      for(i = 0; i < childCount; ++i) {
         View child = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = this.getResolvedLayoutParams(child);
         if(!resortRequired && lp.isDirty(this, child)) {
            resortRequired = true;
         }

         lp.findAnchorView(this, child);
      }

      if(resortRequired) {
         this.mDependencySortedChildren.clear();

         for(i = 0; i < childCount; ++i) {
            this.mDependencySortedChildren.add(this.getChildAt(i));
         }

         Collections.sort(this.mDependencySortedChildren, this.mLayoutDependencyComparator);
      }

   }

   void getDescendantRect(View descendant, Rect out) {
      ViewGroupUtils.getDescendantRect(this, descendant, out);
   }

   protected int getSuggestedMinimumWidth() {
      return Math.max(super.getSuggestedMinimumWidth(), this.getPaddingLeft() + this.getPaddingRight());
   }

   protected int getSuggestedMinimumHeight() {
      return Math.max(super.getSuggestedMinimumHeight(), this.getPaddingTop() + this.getPaddingBottom());
   }

   public void onMeasureChild(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
      this.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
   }

   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      this.prepareChildren();
      this.ensurePreDrawListener();
      int paddingLeft = this.getPaddingLeft();
      int paddingTop = this.getPaddingTop();
      int paddingRight = this.getPaddingRight();
      int paddingBottom = this.getPaddingBottom();
      int layoutDirection = ViewCompat.getLayoutDirection(this);
      boolean isRtl = layoutDirection == 1;
      int widthMode = MeasureSpec.getMode(widthMeasureSpec);
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);
      int heightMode = MeasureSpec.getMode(heightMeasureSpec);
      int heightSize = MeasureSpec.getSize(heightMeasureSpec);
      int widthPadding = paddingLeft + paddingRight;
      int heightPadding = paddingTop + paddingBottom;
      int widthUsed = this.getSuggestedMinimumWidth();
      int heightUsed = this.getSuggestedMinimumHeight();
      int childState = 0;
      boolean applyInsets = this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this);
      int childCount = this.mDependencySortedChildren.size();

      int width;
      for(width = 0; width < childCount; ++width) {
         View height = (View)this.mDependencySortedChildren.get(width);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)height.getLayoutParams();
         int keylineWidthUsed = 0;
         int childHeightMeasureSpec;
         int childWidthMeasureSpec;
         if(lp.keyline >= 0 && widthMode != 0) {
            childWidthMeasureSpec = this.getKeyline(lp.keyline);
            childHeightMeasureSpec = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(lp.gravity), layoutDirection) & 7;
            if((childHeightMeasureSpec != 3 || isRtl) && (childHeightMeasureSpec != 5 || !isRtl)) {
               if(childHeightMeasureSpec == 5 && !isRtl || childHeightMeasureSpec == 3 && isRtl) {
                  keylineWidthUsed = Math.max(0, childWidthMeasureSpec - paddingLeft);
               }
            } else {
               keylineWidthUsed = Math.max(0, widthSize - paddingRight - childWidthMeasureSpec);
            }
         }

         childWidthMeasureSpec = widthMeasureSpec;
         childHeightMeasureSpec = heightMeasureSpec;
         if(applyInsets && !ViewCompat.getFitsSystemWindows(height)) {
            int b = this.mLastInsets.getSystemWindowInsetLeft() + this.mLastInsets.getSystemWindowInsetRight();
            int vertInsets = this.mLastInsets.getSystemWindowInsetTop() + this.mLastInsets.getSystemWindowInsetBottom();
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - b, widthMode);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize - vertInsets, heightMode);
         }

         CoordinatorLayout.Behavior var29 = lp.getBehavior();
         if(var29 == null || !var29.onMeasureChild(this, height, childWidthMeasureSpec, keylineWidthUsed, childHeightMeasureSpec, 0)) {
            this.onMeasureChild(height, childWidthMeasureSpec, keylineWidthUsed, childHeightMeasureSpec, 0);
         }

         widthUsed = Math.max(widthUsed, widthPadding + height.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
         heightUsed = Math.max(heightUsed, heightPadding + height.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
         childState = ViewCompat.combineMeasuredStates(childState, ViewCompat.getMeasuredState(height));
      }

      width = ViewCompat.resolveSizeAndState(widthUsed, widthMeasureSpec, childState & -16777216);
      int var28 = ViewCompat.resolveSizeAndState(heightUsed, heightMeasureSpec, childState << 16);
      this.setMeasuredDimension(width, var28);
   }

   private void dispatchChildApplyWindowInsets(WindowInsetsCompat insets) {
      if(!insets.isConsumed()) {
         int i = 0;

         for(int z = this.getChildCount(); i < z; ++i) {
            View child = this.getChildAt(i);
            if(ViewCompat.getFitsSystemWindows(child)) {
               CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
               CoordinatorLayout.Behavior b = lp.getBehavior();
               if(b != null) {
                  insets = b.onApplyWindowInsets(this, child, insets);
                  if(insets.isConsumed()) {
                     break;
                  }
               }

               insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
               if(insets.isConsumed()) {
                  break;
               }
            }
         }

      }
   }

   public void onLayoutChild(View child, int layoutDirection) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      if(lp.checkAnchorChanged()) {
         throw new IllegalStateException("An anchor may not be changed after CoordinatorLayout measurement begins before layout is complete.");
      } else {
         if(lp.mAnchorView != null) {
            this.layoutChildWithAnchor(child, lp.mAnchorView, layoutDirection);
         } else if(lp.keyline >= 0) {
            this.layoutChildWithKeyline(child, lp.keyline, layoutDirection);
         } else {
            this.layoutChild(child, layoutDirection);
         }

      }
   }

   protected void onLayout(boolean changed, int l, int t, int r, int b) {
      int layoutDirection = ViewCompat.getLayoutDirection(this);
      int childCount = this.mDependencySortedChildren.size();

      for(int i = 0; i < childCount; ++i) {
         View child = (View)this.mDependencySortedChildren.get(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         CoordinatorLayout.Behavior behavior = lp.getBehavior();
         if(behavior == null || !behavior.onLayoutChild(this, child, layoutDirection)) {
            this.onLayoutChild(child, layoutDirection);
         }
      }

   }

   public void onDraw(Canvas c) {
      super.onDraw(c);
      if(this.mDrawStatusBarBackground && this.mStatusBarBackground != null) {
         int inset = this.mLastInsets != null?this.mLastInsets.getSystemWindowInsetTop():0;
         if(inset > 0) {
            this.mStatusBarBackground.setBounds(0, 0, this.getWidth(), inset);
            this.mStatusBarBackground.draw(c);
         }
      }

   }

   void recordLastChildRect(View child, Rect r) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      lp.setLastChildRect(r);
   }

   void getLastChildRect(View child, Rect out) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      out.set(lp.getLastChildRect());
   }

   void getChildRect(View child, boolean transform, Rect out) {
      if(!child.isLayoutRequested() && child.getVisibility() != 8) {
         if(transform) {
            this.getDescendantRect(child, out);
         } else {
            out.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
         }

      } else {
         out.set(0, 0, 0, 0);
      }
   }

   void getDesiredAnchoredChildRect(View child, int layoutDirection, Rect anchorRect, Rect out) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      int absGravity = GravityCompat.getAbsoluteGravity(resolveAnchoredChildGravity(lp.gravity), layoutDirection);
      int absAnchorGravity = GravityCompat.getAbsoluteGravity(resolveGravity(lp.anchorGravity), layoutDirection);
      int hgrav = absGravity & 7;
      int vgrav = absGravity & 112;
      int anchorHgrav = absAnchorGravity & 7;
      int anchorVgrav = absAnchorGravity & 112;
      int childWidth = child.getMeasuredWidth();
      int childHeight = child.getMeasuredHeight();
      int left;
      switch(anchorHgrav) {
      case 1:
         left = anchorRect.left + anchorRect.width() / 2;
         break;
      case 2:
      case 3:
      case 4:
      default:
         left = anchorRect.left;
         break;
      case 5:
         left = anchorRect.right;
      }

      int top;
      switch(anchorVgrav) {
      case 16:
         top = anchorRect.top + anchorRect.height() / 2;
         break;
      case 48:
      default:
         top = anchorRect.top;
         break;
      case 80:
         top = anchorRect.bottom;
      }

      switch(hgrav) {
      case 1:
         left -= childWidth / 2;
         break;
      case 2:
      case 3:
      case 4:
      default:
         left -= childWidth;
      case 5:
      }

      switch(vgrav) {
      case 16:
         top -= childHeight / 2;
         break;
      case 48:
      default:
         top -= childHeight;
      case 80:
      }

      int width = this.getWidth();
      int height = this.getHeight();
      left = Math.max(this.getPaddingLeft() + lp.leftMargin, Math.min(left, width - this.getPaddingRight() - childWidth - lp.rightMargin));
      top = Math.max(this.getPaddingTop() + lp.topMargin, Math.min(top, height - this.getPaddingBottom() - childHeight - lp.bottomMargin));
      out.set(left, top, left + childWidth, top + childHeight);
   }

   private void layoutChildWithAnchor(View child, View anchor, int layoutDirection) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      Rect anchorRect = this.mTempRect1;
      Rect childRect = this.mTempRect2;
      this.getDescendantRect(anchor, anchorRect);
      this.getDesiredAnchoredChildRect(child, layoutDirection, anchorRect, childRect);
      child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
   }

   private void layoutChildWithKeyline(View child, int keyline, int layoutDirection) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      int absGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(lp.gravity), layoutDirection);
      int hgrav = absGravity & 7;
      int vgrav = absGravity & 112;
      int width = this.getWidth();
      int height = this.getHeight();
      int childWidth = child.getMeasuredWidth();
      int childHeight = child.getMeasuredHeight();
      if(layoutDirection == 1) {
         keyline = width - keyline;
      }

      int left = this.getKeyline(keyline) - childWidth;
      int top = 0;
      switch(hgrav) {
      case 1:
         left += childWidth / 2;
      case 2:
      case 3:
      case 4:
      default:
         break;
      case 5:
         left += childWidth;
      }

      switch(vgrav) {
      case 16:
         top += childHeight / 2;
      case 48:
      default:
         break;
      case 80:
         top += childHeight;
      }

      left = Math.max(this.getPaddingLeft() + lp.leftMargin, Math.min(left, width - this.getPaddingRight() - childWidth - lp.rightMargin));
      top = Math.max(this.getPaddingTop() + lp.topMargin, Math.min(top, height - this.getPaddingBottom() - childHeight - lp.bottomMargin));
      child.layout(left, top, left + childWidth, top + childHeight);
   }

   private void layoutChild(View child, int layoutDirection) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      Rect parent = this.mTempRect1;
      parent.set(this.getPaddingLeft() + lp.leftMargin, this.getPaddingTop() + lp.topMargin, this.getWidth() - this.getPaddingRight() - lp.rightMargin, this.getHeight() - this.getPaddingBottom() - lp.bottomMargin);
      if(this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this) && !ViewCompat.getFitsSystemWindows(child)) {
         parent.left += this.mLastInsets.getSystemWindowInsetLeft();
         parent.top += this.mLastInsets.getSystemWindowInsetTop();
         parent.right -= this.mLastInsets.getSystemWindowInsetRight();
         parent.bottom -= this.mLastInsets.getSystemWindowInsetBottom();
      }

      Rect out = this.mTempRect2;
      GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(), child.getMeasuredHeight(), parent, out, layoutDirection);
      child.layout(out.left, out.top, out.right, out.bottom);
   }

   private static int resolveGravity(int gravity) {
      return gravity == 0?8388659:gravity;
   }

   private static int resolveKeylineGravity(int gravity) {
      return gravity == 0?8388661:gravity;
   }

   private static int resolveAnchoredChildGravity(int gravity) {
      return gravity == 0?17:gravity;
   }

   protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      if(lp.mBehavior != null && lp.mBehavior.getScrimOpacity(this, child) > 0.0F) {
         if(this.mScrimPaint == null) {
            this.mScrimPaint = new Paint();
         }

         this.mScrimPaint.setColor(lp.mBehavior.getScrimColor(this, child));
         canvas.drawRect((float)this.getPaddingLeft(), (float)this.getPaddingTop(), (float)(this.getWidth() - this.getPaddingRight()), (float)(this.getHeight() - this.getPaddingBottom()), this.mScrimPaint);
      }

      return super.drawChild(canvas, child, drawingTime);
   }

   void dispatchOnDependentViewChanged(boolean fromNestedScroll) {
      int layoutDirection = ViewCompat.getLayoutDirection(this);
      int childCount = this.mDependencySortedChildren.size();

      for(int i = 0; i < childCount; ++i) {
         View child = (View)this.mDependencySortedChildren.get(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();

         for(int oldRect = 0; oldRect < i; ++oldRect) {
            View newRect = (View)this.mDependencySortedChildren.get(oldRect);
            if(lp.mAnchorDirectChild == newRect) {
               this.offsetChildToAnchor(child, layoutDirection);
            }
         }

         Rect var14 = this.mTempRect1;
         Rect var15 = this.mTempRect2;
         this.getLastChildRect(child, var14);
         this.getChildRect(child, true, var15);
         if(!var14.equals(var15)) {
            this.recordLastChildRect(child, var15);

            for(int j = i + 1; j < childCount; ++j) {
               View checkChild = (View)this.mDependencySortedChildren.get(j);
               CoordinatorLayout.LayoutParams checkLp = (CoordinatorLayout.LayoutParams)checkChild.getLayoutParams();
               CoordinatorLayout.Behavior b = checkLp.getBehavior();
               if(b != null && b.layoutDependsOn(this, checkChild, child)) {
                  if(!fromNestedScroll && checkLp.getChangedAfterNestedScroll()) {
                     checkLp.resetChangedAfterNestedScroll();
                  } else {
                     boolean handled = b.onDependentViewChanged(this, checkChild, child);
                     if(fromNestedScroll) {
                        checkLp.setChangedAfterNestedScroll(handled);
                     }
                  }
               }
            }
         }
      }

   }

   void dispatchDependentViewRemoved(View removedChild) {
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View child = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         CoordinatorLayout.Behavior b = lp.getBehavior();
         if(b != null && b.layoutDependsOn(this, child, removedChild)) {
            b.onDependentViewRemoved(this, child, removedChild);
         }
      }

   }

   public void dispatchDependentViewsChanged(View view) {
      int childCount = this.mDependencySortedChildren.size();
      boolean viewSeen = false;

      for(int i = 0; i < childCount; ++i) {
         View child = (View)this.mDependencySortedChildren.get(i);
         if(child == view) {
            viewSeen = true;
         } else if(viewSeen) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
            CoordinatorLayout.Behavior b = lp.getBehavior();
            if(b != null && lp.dependsOn(this, child, view)) {
               b.onDependentViewChanged(this, child, view);
            }
         }
      }

   }

   public List getDependencies(View child) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      List list = this.mTempDependenciesList;
      list.clear();
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View other = this.getChildAt(i);
         if(other != child && lp.dependsOn(this, child, other)) {
            list.add(other);
         }
      }

      return list;
   }

   void ensurePreDrawListener() {
      boolean hasDependencies = false;
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View child = this.getChildAt(i);
         if(this.hasDependencies(child)) {
            hasDependencies = true;
            break;
         }
      }

      if(hasDependencies != this.mNeedsPreDrawListener) {
         if(hasDependencies) {
            this.addPreDrawListener();
         } else {
            this.removePreDrawListener();
         }
      }

   }

   boolean hasDependencies(View child) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      if(lp.mAnchorView != null) {
         return true;
      } else {
         int childCount = this.getChildCount();

         for(int i = 0; i < childCount; ++i) {
            View other = this.getChildAt(i);
            if(other != child && lp.dependsOn(this, child, other)) {
               return true;
            }
         }

         return false;
      }
   }

   void addPreDrawListener() {
      if(this.mIsAttachedToWindow) {
         if(this.mOnPreDrawListener == null) {
            this.mOnPreDrawListener = new CoordinatorLayout.OnPreDrawListener();
         }

         ViewTreeObserver vto = this.getViewTreeObserver();
         vto.addOnPreDrawListener(this.mOnPreDrawListener);
      }

      this.mNeedsPreDrawListener = true;
   }

   void removePreDrawListener() {
      if(this.mIsAttachedToWindow && this.mOnPreDrawListener != null) {
         ViewTreeObserver vto = this.getViewTreeObserver();
         vto.removeOnPreDrawListener(this.mOnPreDrawListener);
      }

      this.mNeedsPreDrawListener = false;
   }

   void offsetChildToAnchor(View child, int layoutDirection) {
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
      if(lp.mAnchorView != null) {
         Rect anchorRect = this.mTempRect1;
         Rect childRect = this.mTempRect2;
         Rect desiredChildRect = this.mTempRect3;
         this.getDescendantRect(lp.mAnchorView, anchorRect);
         this.getChildRect(child, false, childRect);
         this.getDesiredAnchoredChildRect(child, layoutDirection, anchorRect, desiredChildRect);
         int dx = desiredChildRect.left - childRect.left;
         int dy = desiredChildRect.top - childRect.top;
         if(dx != 0) {
            child.offsetLeftAndRight(dx);
         }

         if(dy != 0) {
            child.offsetTopAndBottom(dy);
         }

         if(dx != 0 || dy != 0) {
            CoordinatorLayout.Behavior b = lp.getBehavior();
            if(b != null) {
               b.onDependentViewChanged(this, child, lp.mAnchorView);
            }
         }
      }

   }

   public boolean isPointInChildBounds(View child, int x, int y) {
      Rect r = this.mTempRect1;
      this.getDescendantRect(child, r);
      return r.contains(x, y);
   }

   public boolean doViewsOverlap(View first, View second) {
      if(first.getVisibility() == 0 && second.getVisibility() == 0) {
         Rect firstRect = this.mTempRect1;
         this.getChildRect(first, first.getParent() != this, firstRect);
         Rect secondRect = this.mTempRect2;
         this.getChildRect(second, second.getParent() != this, secondRect);
         return firstRect.left <= secondRect.right && firstRect.top <= secondRect.bottom && firstRect.right >= secondRect.left && firstRect.bottom >= secondRect.top;
      } else {
         return false;
      }
   }

   public CoordinatorLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
      return new CoordinatorLayout.LayoutParams(this.getContext(), attrs);
   }

   protected CoordinatorLayout.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
      return p instanceof CoordinatorLayout.LayoutParams?new CoordinatorLayout.LayoutParams((CoordinatorLayout.LayoutParams)p):(p instanceof MarginLayoutParams?new CoordinatorLayout.LayoutParams((MarginLayoutParams)p):new CoordinatorLayout.LayoutParams(p));
   }

   protected CoordinatorLayout.LayoutParams generateDefaultLayoutParams() {
      return new CoordinatorLayout.LayoutParams(-2, -2);
   }

   protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
      return p instanceof CoordinatorLayout.LayoutParams && super.checkLayoutParams(p);
   }

   public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
      boolean handled = false;
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View view = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
         CoordinatorLayout.Behavior viewBehavior = lp.getBehavior();
         if(viewBehavior != null) {
            boolean accepted = viewBehavior.onStartNestedScroll(this, view, child, target, nestedScrollAxes);
            handled |= accepted;
            lp.acceptNestedScroll(accepted);
         } else {
            lp.acceptNestedScroll(false);
         }
      }

      return handled;
   }

   public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
      this.mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
      this.mNestedScrollingDirectChild = child;
      this.mNestedScrollingTarget = target;
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View view = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
         if(lp.isNestedScrollAccepted()) {
            CoordinatorLayout.Behavior viewBehavior = lp.getBehavior();
            if(viewBehavior != null) {
               viewBehavior.onNestedScrollAccepted(this, view, child, target, nestedScrollAxes);
            }
         }
      }

   }

   public void onStopNestedScroll(View target) {
      this.mNestedScrollingParentHelper.onStopNestedScroll(target);
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View view = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
         if(lp.isNestedScrollAccepted()) {
            CoordinatorLayout.Behavior viewBehavior = lp.getBehavior();
            if(viewBehavior != null) {
               viewBehavior.onStopNestedScroll(this, view, target);
            }

            lp.resetNestedScroll();
            lp.resetChangedAfterNestedScroll();
         }
      }

      this.mNestedScrollingDirectChild = null;
      this.mNestedScrollingTarget = null;
   }

   public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
      int childCount = this.getChildCount();
      boolean accepted = false;

      for(int i = 0; i < childCount; ++i) {
         View view = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
         if(lp.isNestedScrollAccepted()) {
            CoordinatorLayout.Behavior viewBehavior = lp.getBehavior();
            if(viewBehavior != null) {
               viewBehavior.onNestedScroll(this, view, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
               accepted = true;
            }
         }
      }

      if(accepted) {
         this.dispatchOnDependentViewChanged(true);
      }

   }

   public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
      int xConsumed = 0;
      int yConsumed = 0;
      boolean accepted = false;
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View view = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
         if(lp.isNestedScrollAccepted()) {
            CoordinatorLayout.Behavior viewBehavior = lp.getBehavior();
            if(viewBehavior != null) {
               this.mTempIntPair[0] = this.mTempIntPair[1] = 0;
               viewBehavior.onNestedPreScroll(this, view, target, dx, dy, this.mTempIntPair);
               xConsumed = dx > 0?Math.max(xConsumed, this.mTempIntPair[0]):Math.min(xConsumed, this.mTempIntPair[0]);
               yConsumed = dy > 0?Math.max(yConsumed, this.mTempIntPair[1]):Math.min(yConsumed, this.mTempIntPair[1]);
               accepted = true;
            }
         }
      }

      consumed[0] = xConsumed;
      consumed[1] = yConsumed;
      if(accepted) {
         this.dispatchOnDependentViewChanged(true);
      }

   }

   public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
      boolean handled = false;
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View view = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
         if(lp.isNestedScrollAccepted()) {
            CoordinatorLayout.Behavior viewBehavior = lp.getBehavior();
            if(viewBehavior != null) {
               handled |= viewBehavior.onNestedFling(this, view, target, velocityX, velocityY, consumed);
            }
         }
      }

      if(handled) {
         this.dispatchOnDependentViewChanged(true);
      }

      return handled;
   }

   public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
      boolean handled = false;
      int childCount = this.getChildCount();

      for(int i = 0; i < childCount; ++i) {
         View view = this.getChildAt(i);
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
         if(lp.isNestedScrollAccepted()) {
            CoordinatorLayout.Behavior viewBehavior = lp.getBehavior();
            if(viewBehavior != null) {
               handled |= viewBehavior.onNestedPreFling(this, view, target, velocityX, velocityY);
            }
         }
      }

      return handled;
   }

   public int getNestedScrollAxes() {
      return this.mNestedScrollingParentHelper.getNestedScrollAxes();
   }

   protected void onRestoreInstanceState(Parcelable state) {
      CoordinatorLayout.SavedState ss = (CoordinatorLayout.SavedState)state;
      super.onRestoreInstanceState(ss.getSuperState());
      SparseArray behaviorStates = ss.behaviorStates;
      int i = 0;

      for(int count = this.getChildCount(); i < count; ++i) {
         View child = this.getChildAt(i);
         int childId = child.getId();
         CoordinatorLayout.LayoutParams lp = this.getResolvedLayoutParams(child);
         CoordinatorLayout.Behavior b = lp.getBehavior();
         if(childId != -1 && b != null) {
            Parcelable savedState = (Parcelable)behaviorStates.get(childId);
            if(savedState != null) {
               b.onRestoreInstanceState(this, child, savedState);
            }
         }
      }

   }

   protected Parcelable onSaveInstanceState() {
      CoordinatorLayout.SavedState ss = new CoordinatorLayout.SavedState(super.onSaveInstanceState());
      SparseArray behaviorStates = new SparseArray();
      int i = 0;

      for(int count = this.getChildCount(); i < count; ++i) {
         View child = this.getChildAt(i);
         int childId = child.getId();
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         CoordinatorLayout.Behavior b = lp.getBehavior();
         if(childId != -1 && b != null) {
            Parcelable state = b.onSaveInstanceState(this, child);
            if(state != null) {
               behaviorStates.append(childId, state);
            }
         }
      }

      ss.behaviorStates = behaviorStates;
      return ss;
   }

   static {
      if(VERSION.SDK_INT >= 21) {
         TOP_SORTED_CHILDREN_COMPARATOR = new CoordinatorLayout.ViewElevationComparator();
         INSETS_HELPER = new CoordinatorLayoutInsetsHelperLollipop();
      } else {
         TOP_SORTED_CHILDREN_COMPARATOR = null;
         INSETS_HELPER = null;
      }

      CONSTRUCTOR_PARAMS = new Class[]{Context.class, AttributeSet.class};
      sConstructors = new ThreadLocal();
   }

   protected static class SavedState extends BaseSavedState {
      SparseArray behaviorStates;
      public static final Creator CREATOR = new Creator() {
         public CoordinatorLayout.SavedState createFromParcel(Parcel source) {
            return new CoordinatorLayout.SavedState(source);
         }

         public CoordinatorLayout.SavedState[] newArray(int size) {
            return new CoordinatorLayout.SavedState[size];
         }
      };

      public SavedState(Parcel source) {
         super(source);
         int size = source.readInt();
         int[] ids = new int[size];
         source.readIntArray(ids);
         Parcelable[] states = source.readParcelableArray(CoordinatorLayout.class.getClassLoader());
         this.behaviorStates = new SparseArray(size);

         for(int i = 0; i < size; ++i) {
            this.behaviorStates.append(ids[i], states[i]);
         }

      }

      public SavedState(Parcelable superState) {
         super(superState);
      }

      public void writeToParcel(Parcel dest, int flags) {
         super.writeToParcel(dest, flags);
         int size = this.behaviorStates != null?this.behaviorStates.size():0;
         dest.writeInt(size);
         int[] ids = new int[size];
         Parcelable[] states = new Parcelable[size];

         for(int i = 0; i < size; ++i) {
            ids[i] = this.behaviorStates.keyAt(i);
            states[i] = (Parcelable)this.behaviorStates.valueAt(i);
         }

         dest.writeIntArray(ids);
         dest.writeParcelableArray(states, flags);
      }
   }

   final class HierarchyChangeListener implements OnHierarchyChangeListener {
      public void onChildViewAdded(View parent, View child) {
         if(CoordinatorLayout.this.mOnHierarchyChangeListener != null) {
            CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewAdded(parent, child);
         }

      }

      public void onChildViewRemoved(View parent, View child) {
         CoordinatorLayout.this.dispatchDependentViewRemoved(child);
         if(CoordinatorLayout.this.mOnHierarchyChangeListener != null) {
            CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
         }

      }
   }

   final class ApplyInsetsListener implements android.support.v4.view.OnApplyWindowInsetsListener {
      public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
         CoordinatorLayout.this.setWindowInsets(insets);
         return insets.consumeSystemWindowInsets();
      }
   }

   public static class LayoutParams extends MarginLayoutParams {
      CoordinatorLayout.Behavior mBehavior;
      boolean mBehaviorResolved = false;
      public int gravity = 0;
      public int anchorGravity = 0;
      public int keyline = -1;
      int mAnchorId = -1;
      View mAnchorView;
      View mAnchorDirectChild;
      private boolean mDidBlockInteraction;
      private boolean mDidAcceptNestedScroll;
      private boolean mDidChangeAfterNestedScroll;
      final Rect mLastChildRect = new Rect();
      Object mBehaviorTag;

      public LayoutParams(int width, int height) {
         super(width, height);
      }

      LayoutParams(Context context, AttributeSet attrs) {
         super(context, attrs);
         TypedArray a = context.obtainStyledAttributes(attrs, styleable.CoordinatorLayout_LayoutParams);
         this.gravity = a.getInteger(styleable.CoordinatorLayout_LayoutParams_android_layout_gravity, 0);
         this.mAnchorId = a.getResourceId(styleable.CoordinatorLayout_LayoutParams_layout_anchor, -1);
         this.anchorGravity = a.getInteger(styleable.CoordinatorLayout_LayoutParams_layout_anchorGravity, 0);
         this.keyline = a.getInteger(styleable.CoordinatorLayout_LayoutParams_layout_keyline, -1);
         this.mBehaviorResolved = a.hasValue(styleable.CoordinatorLayout_LayoutParams_layout_behavior);
         if(this.mBehaviorResolved) {
            this.mBehavior = CoordinatorLayout.parseBehavior(context, attrs, a.getString(styleable.CoordinatorLayout_LayoutParams_layout_behavior));
         }

         a.recycle();
      }

      public LayoutParams(CoordinatorLayout.LayoutParams p) {
         super(p);
      }

      public LayoutParams(MarginLayoutParams p) {
         super(p);
      }

      public LayoutParams(android.view.ViewGroup.LayoutParams p) {
         super(p);
      }

      public int getAnchorId() {
         return this.mAnchorId;
      }

      public void setAnchorId(int id) {
         this.invalidateAnchor();
         this.mAnchorId = id;
      }

      public CoordinatorLayout.Behavior getBehavior() {
         return this.mBehavior;
      }

      public void setBehavior(CoordinatorLayout.Behavior behavior) {
         if(this.mBehavior != behavior) {
            this.mBehavior = behavior;
            this.mBehaviorTag = null;
            this.mBehaviorResolved = true;
         }

      }

      void setLastChildRect(Rect r) {
         this.mLastChildRect.set(r);
      }

      Rect getLastChildRect() {
         return this.mLastChildRect;
      }

      boolean checkAnchorChanged() {
         return this.mAnchorView == null && this.mAnchorId != -1;
      }

      boolean didBlockInteraction() {
         if(this.mBehavior == null) {
            this.mDidBlockInteraction = false;
         }

         return this.mDidBlockInteraction;
      }

      boolean isBlockingInteractionBelow(CoordinatorLayout parent, View child) {
         return this.mDidBlockInteraction?true:(this.mDidBlockInteraction |= this.mBehavior != null?this.mBehavior.blocksInteractionBelow(parent, child):false);
      }

      void resetTouchBehaviorTracking() {
         this.mDidBlockInteraction = false;
      }

      void resetNestedScroll() {
         this.mDidAcceptNestedScroll = false;
      }

      void acceptNestedScroll(boolean accept) {
         this.mDidAcceptNestedScroll = accept;
      }

      boolean isNestedScrollAccepted() {
         return this.mDidAcceptNestedScroll;
      }

      boolean getChangedAfterNestedScroll() {
         return this.mDidChangeAfterNestedScroll;
      }

      void setChangedAfterNestedScroll(boolean changed) {
         this.mDidChangeAfterNestedScroll = changed;
      }

      void resetChangedAfterNestedScroll() {
         this.mDidChangeAfterNestedScroll = false;
      }

      boolean dependsOn(CoordinatorLayout parent, View child, View dependency) {
         return dependency == this.mAnchorDirectChild || this.mBehavior != null && this.mBehavior.layoutDependsOn(parent, child, dependency);
      }

      void invalidateAnchor() {
         this.mAnchorView = this.mAnchorDirectChild = null;
      }

      View findAnchorView(CoordinatorLayout parent, View forChild) {
         if(this.mAnchorId == -1) {
            this.mAnchorView = this.mAnchorDirectChild = null;
            return null;
         } else {
            if(this.mAnchorView == null || !this.verifyAnchorView(forChild, parent)) {
               this.resolveAnchorView(forChild, parent);
            }

            return this.mAnchorView;
         }
      }

      boolean isDirty(CoordinatorLayout parent, View child) {
         return this.mBehavior != null && this.mBehavior.isDirty(parent, child);
      }

      private void resolveAnchorView(View forChild, CoordinatorLayout parent) {
         this.mAnchorView = parent.findViewById(this.mAnchorId);
         if(this.mAnchorView == null) {
            if(parent.isInEditMode()) {
               this.mAnchorView = this.mAnchorDirectChild = null;
            } else {
               throw new IllegalStateException("Could not find CoordinatorLayout descendant view with id " + parent.getResources().getResourceName(this.mAnchorId) + " to anchor view " + forChild);
            }
         } else {
            View directChild = this.mAnchorView;

            for(ViewParent p = this.mAnchorView.getParent(); p != parent && p != null; p = p.getParent()) {
               if(p == forChild) {
                  if(parent.isInEditMode()) {
                     this.mAnchorView = this.mAnchorDirectChild = null;
                     return;
                  }

                  throw new IllegalStateException("Anchor must not be a descendant of the anchored view");
               }

               if(p instanceof View) {
                  directChild = (View)p;
               }
            }

            this.mAnchorDirectChild = directChild;
         }
      }

      private boolean verifyAnchorView(View forChild, CoordinatorLayout parent) {
         if(this.mAnchorView.getId() != this.mAnchorId) {
            return false;
         } else {
            View directChild = this.mAnchorView;

            for(ViewParent p = this.mAnchorView.getParent(); p != parent; p = p.getParent()) {
               if(p == null || p == forChild) {
                  this.mAnchorView = this.mAnchorDirectChild = null;
                  return false;
               }

               if(p instanceof View) {
                  directChild = (View)p;
               }
            }

            this.mAnchorDirectChild = directChild;
            return true;
         }
      }
   }

   public abstract static class Behavior {
      public Behavior() {
      }

      public Behavior(Context context, AttributeSet attrs) {
      }

      public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
         return false;
      }

      public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
         return false;
      }

      public final int getScrimColor(CoordinatorLayout parent, View child) {
         return -16777216;
      }

      public final float getScrimOpacity(CoordinatorLayout parent, View child) {
         return 0.0F;
      }

      public boolean blocksInteractionBelow(CoordinatorLayout parent, View child) {
         return this.getScrimOpacity(parent, child) > 0.0F;
      }

      public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
         return false;
      }

      public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
         return false;
      }

      public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency) {
      }

      public boolean isDirty(CoordinatorLayout parent, View child) {
         return false;
      }

      public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
         return false;
      }

      public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
         return false;
      }

      public static void setTag(View child, Object tag) {
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         lp.mBehaviorTag = tag;
      }

      public static Object getTag(View child) {
         CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
         return lp.mBehaviorTag;
      }

      public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
         return false;
      }

      public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
      }

      public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
      }

      public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
      }

      public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
      }

      public boolean onNestedFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY, boolean consumed) {
         return false;
      }

      public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
         return false;
      }

      public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, View child, WindowInsetsCompat insets) {
         return insets;
      }

      public void onRestoreInstanceState(CoordinatorLayout parent, View child, Parcelable state) {
      }

      public Parcelable onSaveInstanceState(CoordinatorLayout parent, View child) {
         return BaseSavedState.EMPTY_STATE;
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   public @interface DefaultBehavior {
      Class value();
   }

   static class ViewElevationComparator implements Comparator<View> {
      public int compare(View lhs, View rhs) {
         float lz = ViewCompat.getZ(lhs);
         float rz = ViewCompat.getZ(rhs);
         return lz > rz?-1:(lz < rz?1:0);
      }
   }

   class OnPreDrawListener implements android.view.ViewTreeObserver.OnPreDrawListener {
      public boolean onPreDraw() {
         CoordinatorLayout.this.dispatchOnDependentViewChanged(false);
         return true;
      }
   }
}
