package ch.bergturbenthal.marathontabelle.androidclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.bergturbenthal.marathontabelle.androidclient.dummy.DummyContent;

public class MarathonDetailFragment extends Fragment {

  public static final String ARG_ITEM_ID = "item_id";

  DummyContent.DummyItem mItem;

  public MarathonDetailFragment() {
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments().containsKey(ARG_ITEM_ID)) {
      mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.fragment_marathon_detail, container, false);
    if (mItem != null) {
      ((TextView) rootView.findViewById(R.id.marathon_detail)).setText(mItem.content);
    }
    return rootView;
  }
}
