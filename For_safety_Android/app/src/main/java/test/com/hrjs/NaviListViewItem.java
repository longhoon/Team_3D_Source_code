package test.com.hrjs;

import android.graphics.drawable.Drawable;

/**
 * Created by Heera on 2017-06-06.
 */

public class NaviListViewItem {

    private Drawable imageStr;
    private String contentStr;

    public void setImage(Drawable ima){
        imageStr = ima;
    }
    public void setContent(String con){
        contentStr = con;
    }

    public Drawable getImage(){
        return this.imageStr;
    }

    public String getContent(){
        return this.contentStr;
    }

}
