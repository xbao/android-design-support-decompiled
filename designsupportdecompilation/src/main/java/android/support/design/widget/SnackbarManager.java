package android.support.design.widget;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;

class SnackbarManager {
   private static final int MSG_TIMEOUT = 0;
   private static final int SHORT_DURATION_MS = 1500;
   private static final int LONG_DURATION_MS = 2750;
   private static SnackbarManager sSnackbarManager;
   private final Object mLock = new Object();
   private final Handler mHandler = new Handler(Looper.getMainLooper(), new android.os.Handler.Callback() {
      public boolean handleMessage(Message message) {
         switch(message.what) {
         case 0:
            SnackbarManager.this.handleTimeout((SnackbarManager.SnackbarRecord)message.obj);
            return true;
         default:
            return false;
         }
      }
   });
   private SnackbarManager.SnackbarRecord mCurrentSnackbar;
   private SnackbarManager.SnackbarRecord mNextSnackbar;

   static SnackbarManager getInstance() {
      if(sSnackbarManager == null) {
         sSnackbarManager = new SnackbarManager();
      }

      return sSnackbarManager;
   }

   public void show(int duration, SnackbarManager.Callback callback) {
      Object var3 = this.mLock;
      synchronized(this.mLock) {
         if(this.isCurrentSnackbar(callback)) {
            this.mCurrentSnackbar.duration = duration;
            this.mHandler.removeCallbacksAndMessages(this.mCurrentSnackbar);
            this.scheduleTimeoutLocked(this.mCurrentSnackbar);
         } else {
            if(this.isNextSnackbar(callback)) {
               this.mNextSnackbar.duration = duration;
            } else {
               this.mNextSnackbar = new SnackbarManager.SnackbarRecord(duration, callback);
            }

            if(this.mCurrentSnackbar == null || !this.cancelSnackbarLocked(this.mCurrentSnackbar)) {
               this.mCurrentSnackbar = null;
               this.showNextSnackbarLocked();
            }
         }
      }
   }

   public void dismiss(SnackbarManager.Callback callback) {
      Object var2 = this.mLock;
      synchronized(this.mLock) {
         if(this.isCurrentSnackbar(callback)) {
            this.cancelSnackbarLocked(this.mCurrentSnackbar);
         }

         if(this.isNextSnackbar(callback)) {
            this.cancelSnackbarLocked(this.mNextSnackbar);
         }

      }
   }

   public void onDismissed(SnackbarManager.Callback callback) {
      Object var2 = this.mLock;
      synchronized(this.mLock) {
         if(this.isCurrentSnackbar(callback)) {
            this.mCurrentSnackbar = null;
            if(this.mNextSnackbar != null) {
               this.showNextSnackbarLocked();
            }
         }

      }
   }

   public void onShown(SnackbarManager.Callback callback) {
      Object var2 = this.mLock;
      synchronized(this.mLock) {
         if(this.isCurrentSnackbar(callback)) {
            this.scheduleTimeoutLocked(this.mCurrentSnackbar);
         }

      }
   }

   public void cancelTimeout(SnackbarManager.Callback callback) {
      Object var2 = this.mLock;
      synchronized(this.mLock) {
         if(this.isCurrentSnackbar(callback)) {
            this.mHandler.removeCallbacksAndMessages(this.mCurrentSnackbar);
         }

      }
   }

   public void restoreTimeout(SnackbarManager.Callback callback) {
      Object var2 = this.mLock;
      synchronized(this.mLock) {
         if(this.isCurrentSnackbar(callback)) {
            this.scheduleTimeoutLocked(this.mCurrentSnackbar);
         }

      }
   }

   private void showNextSnackbarLocked() {
      if(this.mNextSnackbar != null) {
         this.mCurrentSnackbar = this.mNextSnackbar;
         this.mNextSnackbar = null;
         SnackbarManager.Callback callback = (SnackbarManager.Callback)this.mCurrentSnackbar.callback.get();
         if(callback != null) {
            callback.show();
         } else {
            this.mCurrentSnackbar = null;
         }
      }

   }

   private boolean cancelSnackbarLocked(SnackbarManager.SnackbarRecord record) {
      SnackbarManager.Callback callback = (SnackbarManager.Callback)record.callback.get();
      if(callback != null) {
         callback.dismiss();
         return true;
      } else {
         return false;
      }
   }

   private boolean isCurrentSnackbar(SnackbarManager.Callback callback) {
      return this.mCurrentSnackbar != null && this.mCurrentSnackbar.isSnackbar(callback);
   }

   private boolean isNextSnackbar(SnackbarManager.Callback callback) {
      return this.mNextSnackbar != null && this.mNextSnackbar.isSnackbar(callback);
   }

   private void scheduleTimeoutLocked(SnackbarManager.SnackbarRecord r) {
      if(r.duration != -2) {
         int durationMs = 2750;
         if(r.duration > 0) {
            durationMs = r.duration;
         } else if(r.duration == -1) {
            durationMs = 1500;
         }

         this.mHandler.removeCallbacksAndMessages(r);
         this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 0, r), (long)durationMs);
      }
   }

   private void handleTimeout(SnackbarManager.SnackbarRecord record) {
      Object var2 = this.mLock;
      synchronized(this.mLock) {
         if(this.mCurrentSnackbar == record || this.mNextSnackbar == record) {
            this.cancelSnackbarLocked(record);
         }

      }
   }

   private static class SnackbarRecord {
      private final WeakReference callback;
      private int duration;

      SnackbarRecord(int duration, SnackbarManager.Callback callback) {
         this.callback = new WeakReference(callback);
         this.duration = duration;
      }

      boolean isSnackbar(SnackbarManager.Callback callback) {
         return callback != null && this.callback.get() == callback;
      }
   }

   interface Callback {
      void show();

      void dismiss();
   }
}
