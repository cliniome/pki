package sa.com.is.utils;

import android.content.Context;

/**
 * Created by snouto on 03/09/15.
 */
public class ContextHolder {

    private Context context;

    private static ContextHolder _instance;

    private ContextHolder(Context context)
    {
        this.context = context;
    }



    public static ContextHolder createInstance(Context context)
    {
        if(_instance == null)
            _instance = new ContextHolder(context);

        return _instance;
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
