package sa.com.is.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import sa.com.is.activity.K9ActivityCommon.K9ActivityMagic;
import sa.com.is.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;


public class K9Activity extends Activity implements K9ActivityMagic {

    private K9ActivityCommon mBase;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mBase = K9ActivityCommon.newInstance(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mBase.preDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setupGestureDetector(OnSwipeGestureListener listener) {
        mBase.setupGestureDetector(listener);
    }
}
