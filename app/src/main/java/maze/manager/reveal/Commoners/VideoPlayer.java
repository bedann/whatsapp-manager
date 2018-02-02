package maze.manager.reveal.Commoners;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.halilibo.bettervideoplayer.BetterVideoPlayer;

/**
 * Created by Orion Technologies on 10/13/2017.
 */

public class VideoPlayer extends BetterVideoPlayer {

    Context context;
    ImageView thumb;

    public VideoPlayer(Context context) {
        super(context);
        this.context = context;
        thumb = new ImageView(context);
        thumb.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        thumb = new ImageView(context);
        thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }



    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(thumb,0, rel_btn);
    }


    public ImageView getThumb() {
        return thumb;
    }
}
