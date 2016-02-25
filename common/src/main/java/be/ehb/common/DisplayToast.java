package be.ehb.common;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by davy.van.belle on 22/01/2016.
 */
public class DisplayToast implements Runnable {
    private final Context context;
    private final CharSequence text;
    private final int duration;

    private DisplayToast(){
        context = null;
        this.text = null;
        this.duration = 0;
    };

    public DisplayToast(Context context, CharSequence text, int duration) {
        this.context = context;
        this.text = text;
        this.duration = duration;
    }

    public DisplayToast(Context context, int ResId ,int duration){
        this.context = context;
        this.text = context.getString(ResId);
        this.duration = duration;
    }


    @Override
    public void run() {
        Toast.makeText(this.context, this.text, this.duration).show();
    }
}

