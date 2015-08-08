package android.support.design.widget;

import android.support.design.widget.CoordinatorLayoutInsetsHelper;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.view.View;

class CoordinatorLayoutInsetsHelperLollipop implements CoordinatorLayoutInsetsHelper {
   public void setupForWindowInsets(View view, OnApplyWindowInsetsListener insetsListener) {
      if(ViewCompat.getFitsSystemWindows(view)) {
         ViewCompat.setOnApplyWindowInsetsListener(view, insetsListener);
         view.setSystemUiVisibility(1280);
      }

   }
}
