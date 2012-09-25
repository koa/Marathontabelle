/*
 * (c) 2012 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.androidclient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ch.bergturbenthal.marathontabelle.model.MarathonData;

/**
 * TODO: add type comment.
 * 
 */
final class MarathonListAdapter extends BaseAdapter {
  private final List<MarathonData> data;
  private final Context context;
  private final LayoutInflater inflater;
  private final Drawable okIcon;

  MarathonListAdapter(final Context context, final Collection<MarathonData> data) {
    this.context = context;
    this.data = new ArrayList<MarathonData>(data);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    okIcon = context.getResources().getDrawable(R.drawable.ic_ok);

  }

  @Override
  public int getCount() {
    return data.size();
  }

  @Override
  public MarathonData getItem(final int position) {
    return data.get(position);
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {

    View view;
    final TextView text;

    if (convertView == null) {
      view = inflater.inflate(R.layout.activity_marathon_list_item, parent, false);
    } else {
      view = convertView;
    }

    final MarathonData item = getItem(position);
    final TextView textView = (TextView) view.findViewById(R.id.list_text);
    textView.setText(item.getMarathonName());
    final ImageView iconView = (ImageView) view.findViewById(R.id.list_icon);
    iconView.setImageDrawable(okIcon);

    return view;
  }

}