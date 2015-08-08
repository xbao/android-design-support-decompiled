package android.support.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Build.VERSION;
import android.os.Handler.Callback;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import nz.xbc.designsupportdecompilation.R;
import nz.xbc.designsupportdecompilation.R.anim;
 import nz.xbc.designsupportdecompilation.R.dimen;
 import nz.xbc.designsupportdecompilation.R.id;
 import nz.xbc.designsupportdecompilation.R.layout;
 import nz.xbc.designsupportdecompilation.R.styleable;
import android.support.design.widget.AnimationUtils;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SnackbarManager;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Snackbar {
   public static final int LENGTH_INDEFINITE = -2;
   public static final int LENGTH_SHORT = -1;
   public static final int LENGTH_LONG = 0;
   private static final int ANIMATION_DURATION = 250;
   private static final int ANIMATION_FADE_DURATION = 180;
   private static final Handler sHandler = new Handler(Looper.getMainLooper(), new Callback() {
      public boolean handleMessage(Message message) {
         switch(message.what) {
         case 0:
            ((Snackbar)message.obj).showView();
            return true;
         case 1:
            ((Snackbar)message.obj).hideView();
            return true;
         default:
            return false;
         }
      }
   });
   private static final int MSG_SHOW = 0;
   private static final int MSG_DISMISS = 1;
   private final ViewGroup mParent;
   private final Context mContext;
   private final Snackbar.SnackbarLayout mView;
   private int mDuration;
   private final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
      public void show() {
         Snackbar.sHandler.sendMessage(Snackbar.sHandler.obtainMessage(0, Snackbar.this));
      }

      public void dismiss() {
         Snackbar.sHandler.sendMessage(Snackbar.sHandler.obtainMessage(1, Snackbar.this));
      }
   };

   Snackbar(ViewGroup parent) {
      this.mParent = parent;
      this.mContext = parent.getContext();
      LayoutInflater inflater = LayoutInflater.from(this.mContext);
      this.mView = (Snackbar.SnackbarLayout)inflater.inflate(layout.layout_snackbar, this.mParent, false);
   }

   public static Snackbar make(View view, CharSequence text, int duration) {
      Snackbar snackbar = new Snackbar(findSuitableParent(view));
      snackbar.setText(text);
      snackbar.setDuration(duration);
      return snackbar;
   }

   public static Snackbar make(View view, int resId, int duration) {
      return make(view, view.getResources().getText(resId), duration);
   }

   @Nullable
   private static ViewGroup findSuitableParent(View view) {
      ViewGroup fallback = null;

      do {
         if(view instanceof CoordinatorLayout) {
            return (ViewGroup)view;
         }

         if(view instanceof FrameLayout) {
            if(view.getId() == 16908290) {
               return (ViewGroup)view;
            }

            fallback = (ViewGroup)view;
         }

         if(view != null) {
            ViewParent parent = view.getParent();
            view = parent instanceof View?(View)parent:null;
         }
      } while(view != null);

      return fallback;
   }

   public Snackbar setAction(@StringRes int resId, OnClickListener listener) {
      return this.setAction(this.mContext.getText(resId), listener);
   }

   public Snackbar setAction(CharSequence text, final OnClickListener listener) {
      TextView tv = this.mView.getActionView();
      if(!TextUtils.isEmpty(text) && listener != null) {
         tv.setVisibility(0);
         tv.setText(text);
         tv.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
               listener.onClick(view);
               Snackbar.this.dismiss();
            }
         });
      } else {
         tv.setVisibility(8);
         tv.setOnClickListener((OnClickListener)null);
      }

      return this;
   }

   public Snackbar setActionTextColor(ColorStateList colors) {
      TextView tv = this.mView.getActionView();
      tv.setTextColor(colors);
      return this;
   }

   public Snackbar setActionTextColor(int color) {
      TextView tv = this.mView.getActionView();
      tv.setTextColor(color);
      return this;
   }

   public Snackbar setText(CharSequence message) {
      TextView tv = this.mView.getMessageView();
      tv.setText(message);
      return this;
   }

   public Snackbar setText(@StringRes int resId) {
      return this.setText(this.mContext.getText(resId));
   }

   public Snackbar setDuration(int duration) {
      this.mDuration = duration;
      return this;
   }

   public int getDuration() {
      return this.mDuration;
   }

   public View getView() {
      return this.mView;
   }

   public void show() {
      SnackbarManager.getInstance().show(this.mDuration, this.mManagerCallback);
   }

   public void dismiss() {
      SnackbarManager.getInstance().dismiss(this.mManagerCallback);
   }

   final void showView() {
      if(this.mView.getParent() == null) {
         LayoutParams lp = this.mView.getLayoutParams();
         if(lp instanceof CoordinatorLayout.LayoutParams) {
            Snackbar.Behavior behavior = new Snackbar.Behavior();
            behavior.setStartAlphaSwipeDistance(0.1F);
            behavior.setEndAlphaSwipeDistance(0.6F);
            behavior.setSwipeDirection(0);
            behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
               public void onDismiss(View view) {
                  Snackbar.this.dismiss();
               }

               public void onDragStateChanged(int state) {
                  switch(state) {
                  case 0:
                     SnackbarManager.getInstance().restoreTimeout(Snackbar.this.mManagerCallback);
                     break;
                  case 1:
                  case 2:
                     SnackbarManager.getInstance().cancelTimeout(Snackbar.this.mManagerCallback);
                  }

               }
            });
            ((CoordinatorLayout.LayoutParams)lp).setBehavior(behavior);
         }

         this.mParent.addView(this.mView);
      }

      if(ViewCompat.isLaidOut(this.mView)) {
         this.animateViewIn();
      } else {
         this.mView.setOnLayoutChangeListener(new Snackbar.SnackbarLayout.OnLayoutChangeListener() {
            public void onLayoutChange(View view, int left, int top, int right, int bottom) {
               Snackbar.this.animateViewIn();
               Snackbar.this.mView.setOnLayoutChangeListener((Snackbar.SnackbarLayout.OnLayoutChangeListener)null);
            }
         });
      }

   }

   private void animateViewIn() {
      if(VERSION.SDK_INT >= 14) {
         ViewCompat.setTranslationY(this.mView, (float)this.mView.getHeight());
         ViewCompat.animate(this.mView).translationY(0.0F).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setDuration(250L).setListener(new ViewPropertyAnimatorListenerAdapter() {
            public void onAnimationStart(View view) {
               Snackbar.this.mView.animateChildrenIn(70, 180);
            }

            public void onAnimationEnd(View view) {
               SnackbarManager.getInstance().onShown(Snackbar.this.mManagerCallback);
            }
         }).start();
      } else {
         Animation anim = android.view.animation.AnimationUtils.loadAnimation(this.mView.getContext(), nz.xbc.designsupportdecompilation.R.anim.snackbar_in);
         anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
         anim.setDuration(250L);
         anim.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
               SnackbarManager.getInstance().onShown(Snackbar.this.mManagerCallback);
            }

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }
         });
         this.mView.startAnimation(anim);
      }

   }

   private void animateViewOut() {
      if(VERSION.SDK_INT >= 14) {
         ViewCompat.animate(this.mView).translationY((float)this.mView.getHeight()).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setDuration(250L).setListener(new ViewPropertyAnimatorListenerAdapter() {
            public void onAnimationStart(View view) {
               Snackbar.this.mView.animateChildrenOut(0, 180);
            }

            public void onAnimationEnd(View view) {
               Snackbar.this.onViewHidden();
            }
         }).start();
      } else {
         Animation anim = android.view.animation.AnimationUtils.loadAnimation(this.mView.getContext(), nz.xbc.designsupportdecompilation.R.anim.snackbar_out);
         anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
         anim.setDuration(250L);
         anim.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
               Snackbar.this.onViewHidden();
            }

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }
         });
         this.mView.startAnimation(anim);
      }

   }

   final void hideView() {
      if(this.mView.getVisibility() == 0 && !this.isBeingDragged()) {
         this.animateViewOut();
      } else {
         this.onViewHidden();
      }

   }

   private void onViewHidden() {
      this.mParent.removeView(this.mView);
      SnackbarManager.getInstance().onDismissed(this.mManagerCallback);
   }

   private boolean isBeingDragged() {
      LayoutParams lp = this.mView.getLayoutParams();
      if(lp instanceof CoordinatorLayout.LayoutParams) {
         CoordinatorLayout.LayoutParams cllp = (CoordinatorLayout.LayoutParams)lp;
         CoordinatorLayout.Behavior behavior = cllp.getBehavior();
         if(behavior instanceof SwipeDismissBehavior) {
            return ((SwipeDismissBehavior)behavior).getDragState() != 0;
         }
      }

      return false;
   }

   final class Behavior extends SwipeDismissBehavior {
      public boolean onInterceptTouchEvent(CoordinatorLayout parent, Snackbar.SnackbarLayout child, MotionEvent event) {
         if(parent.isPointInChildBounds(child, (int)event.getX(), (int)event.getY())) {
            switch(event.getActionMasked()) {
            case 0:
               SnackbarManager.getInstance().cancelTimeout(Snackbar.this.mManagerCallback);
               break;
            case 1:
            case 3:
               SnackbarManager.getInstance().restoreTimeout(Snackbar.this.mManagerCallback);
            case 2:
            }
         }

         return super.onInterceptTouchEvent(parent, child, event);
      }
   }

   public static class SnackbarLayout extends LinearLayout {
      private TextView mMessageView;
      private TextView mActionView;
      private int mMaxWidth;
      private int mMaxInlineActionWidth;
      private Snackbar.SnackbarLayout.OnLayoutChangeListener mOnLayoutChangeListener;

      public SnackbarLayout(Context context) {
         this(context, (AttributeSet)null);
      }

      public SnackbarLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
         TypedArray a = context.obtainStyledAttributes(attrs, styleable.SnackbarLayout);
         this.mMaxWidth = a.getDimensionPixelSize(styleable.SnackbarLayout_android_maxWidth, -1);
         this.mMaxInlineActionWidth = a.getDimensionPixelSize(styleable.SnackbarLayout_maxActionInlineWidth, -1);
         if(a.hasValue(styleable.SnackbarLayout_elevation)) {
            ViewCompat.setElevation(this, (float)a.getDimensionPixelSize(styleable.SnackbarLayout_elevation, 0));
         }

         a.recycle();
         this.setClickable(true);
         LayoutInflater.from(context).inflate(layout.layout_snackbar_include, this);
      }

      protected void onFinishInflate() {
         super.onFinishInflate();
         this.mMessageView = (TextView)this.findViewById(id.snackbar_text);
         this.mActionView = (TextView)this.findViewById(id.snackbar_action);
      }

      TextView getMessageView() {
         return this.mMessageView;
      }

      TextView getActionView() {
         return this.mActionView;
      }

      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         if(this.mMaxWidth > 0 && this.getMeasuredWidth() > this.mMaxWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.mMaxWidth, 1073741824);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         }

         int multiLineVPadding = this.getResources().getDimensionPixelSize(dimen.snackbar_padding_vertical_2lines);
         int singleLineVPadding = this.getResources().getDimensionPixelSize(dimen.snackbar_padding_vertical);
         boolean isMultiLine = this.mMessageView.getLayout().getLineCount() > 1;
         boolean remeasure = false;
         if(isMultiLine && this.mMaxInlineActionWidth > 0 && this.mActionView.getMeasuredWidth() > this.mMaxInlineActionWidth) {
            if(this.updateViewsWithinLayout(1, multiLineVPadding, multiLineVPadding - singleLineVPadding)) {
               remeasure = true;
            }
         } else {
            int messagePadding = isMultiLine?multiLineVPadding:singleLineVPadding;
            if(this.updateViewsWithinLayout(0, messagePadding, messagePadding)) {
               remeasure = true;
            }
         }

         if(remeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         }

      }

      void animateChildrenIn(int delay, int duration) {
         ViewCompat.setAlpha(this.mMessageView, 0.0F);
         ViewCompat.animate(this.mMessageView).alpha(1.0F).setDuration((long)duration).setStartDelay((long)delay).start();
         if(this.mActionView.getVisibility() == 0) {
            ViewCompat.setAlpha(this.mActionView, 0.0F);
            ViewCompat.animate(this.mActionView).alpha(1.0F).setDuration((long)duration).setStartDelay((long)delay).start();
         }

      }

      void animateChildrenOut(int delay, int duration) {
         ViewCompat.setAlpha(this.mMessageView, 1.0F);
         ViewCompat.animate(this.mMessageView).alpha(0.0F).setDuration((long)duration).setStartDelay((long)delay).start();
         if(this.mActionView.getVisibility() == 0) {
            ViewCompat.setAlpha(this.mActionView, 1.0F);
            ViewCompat.animate(this.mActionView).alpha(0.0F).setDuration((long)duration).setStartDelay((long)delay).start();
         }

      }

      protected void onLayout(boolean changed, int l, int t, int r, int b) {
         super.onLayout(changed, l, t, r, b);
         if(changed && this.mOnLayoutChangeListener != null) {
            this.mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
         }

      }

      void setOnLayoutChangeListener(Snackbar.SnackbarLayout.OnLayoutChangeListener onLayoutChangeListener) {
         this.mOnLayoutChangeListener = onLayoutChangeListener;
      }

      private boolean updateViewsWithinLayout(int orientation, int messagePadTop, int messagePadBottom) {
         boolean changed = false;
         if(orientation != this.getOrientation()) {
            this.setOrientation(orientation);
            changed = true;
         }

         if(this.mMessageView.getPaddingTop() != messagePadTop || this.mMessageView.getPaddingBottom() != messagePadBottom) {
            updateTopBottomPadding(this.mMessageView, messagePadTop, messagePadBottom);
            changed = true;
         }

         return changed;
      }

      private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
         if(ViewCompat.isPaddingRelative(view)) {
            ViewCompat.setPaddingRelative(view, ViewCompat.getPaddingStart(view), topPadding, ViewCompat.getPaddingEnd(view), bottomPadding);
         } else {
            view.setPadding(view.getPaddingLeft(), topPadding, view.getPaddingRight(), bottomPadding);
         }

      }

      interface OnLayoutChangeListener {
         void onLayoutChange(View var1, int var2, int var3, int var4, int var5);
      }
   }

   @Retention(RetentionPolicy.SOURCE)
   public @interface Duration {
   }
}
