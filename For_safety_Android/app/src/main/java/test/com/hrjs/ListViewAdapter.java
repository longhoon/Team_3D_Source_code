package test.com.hrjs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Heera on 2017-05-01.
 */

public class ListViewAdapter extends BaseAdapter implements Filterable {

    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();
    private ArrayList<ListViewItem> filteredItemList = listViewItemList;

    Filter listFilter;

    public ListViewAdapter(){

    }

    @Override
    public Filter getFilter() {
        if (listFilter == null){
            listFilter = new ListFilter();
        }

        return listFilter;
    }

    private class ListFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length()==0){
                results.values = listViewItemList;
                results.count = listViewItemList.size();
            }
            else {
                ArrayList<ListViewItem> itemList = new ArrayList<ListViewItem>();

                for(ListViewItem item : listViewItemList) {
                    if(item.getTitle().toUpperCase().contains(constraint.toString().toUpperCase())||
                            item.getDesc().toUpperCase().contains(constraint.toString().toUpperCase()))
                    {
                        itemList.add(item);
                    }
                }

                results.values = itemList;
                results.count = itemList.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            filteredItemList = (ArrayList<ListViewItem>) results.values;

            if (results.count > 0){
                notifyDataSetChanged();
            }
            else{
                notifyDataSetInvalidated();
            }

        }
    }

    @Override
    public int getCount() {
        return filteredItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_listview,parent,false);
        }

        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1);
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2);

        ListViewItem listViewItem = filteredItemList.get(position);

        titleTextView.setText(listViewItem.getTitle());
        descTextView.setText(listViewItem.getDesc());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return filteredItemList.get(position);
    }

    public void addItem(String title, String desc, double lat, double lon){
        ListViewItem item = new ListViewItem();

        item.setTitle(title);
        item.setDesc(desc);
        item.setLat(lat);
        item.setLon(lon);

        listViewItemList.add(item);
    }

    public void clearItem(){
        listViewItemList.clear();
    }

    public void nofity(){notifyDataSetChanged();}

}
