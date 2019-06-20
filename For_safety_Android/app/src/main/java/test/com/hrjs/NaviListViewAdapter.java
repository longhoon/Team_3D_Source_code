package test.com.hrjs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Heera on 2017-06-06.
 */


public class NaviListViewAdapter extends BaseAdapter {

    private ArrayList<NaviListViewItem> navilistItemList = new ArrayList<NaviListViewItem>();

    public NaviListViewAdapter() {

    }

    @Override
    public int getCount() {
        return navilistItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_navilist,parent, false);
        }

        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.direction);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.desc);

        NaviListViewItem navilistViewItem = navilistItemList.get(position);

        iconImageView.setImageDrawable(navilistViewItem.getImage());
        contentTextView.setText(navilistViewItem.getContent());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return navilistItemList.get(position);
    }

    public void addItem(Drawable icon, String content){
        NaviListViewItem item = new NaviListViewItem();

        item.setImage(icon);
        item.setContent(content);

        navilistItemList.add(item);
    }

    public void clearItem(){
        navilistItemList.clear();
    }
}