package ch.bergturbenthal.marathontabelle.androidclient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import ch.bergturbenthal.marathontabelle.androidclient.data.DataProvider;
import ch.bergturbenthal.marathontabelle.model.MarathonData;

public class MarathonListFragment extends ListFragment {

  public interface Callbacks {

    public void onItemSelected(final MarathonData data);
  }

  private static final String STATE_ACTIVATED_POSITION = "activated_position";
  private Callbacks mCallbacks = sDummyCallbacks;

  private int mActivatedPosition = ListView.INVALID_POSITION;

  private static Callbacks sDummyCallbacks = new Callbacks() {
    @Override
    public void onItemSelected(final MarathonData data) {
    }
  };
  private MarathonListAdapter marathonListAdapter;

  public MarathonListFragment() {
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    if (!(activity instanceof Callbacks)) {
      throw new IllegalStateException("Activity must implement fragment's callbacks.");
    }

    mCallbacks = (Callbacks) activity;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    marathonListAdapter = new MarathonListAdapter(getActivity(), loadData());
    setListAdapter(marathonListAdapter);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mCallbacks = sDummyCallbacks;
  }

  @Override
  public void onListItemClick(final ListView listView, final View view, final int position, final long id) {
    super.onListItemClick(listView, view, position, id);
    mCallbacks.onItemSelected(marathonListAdapter.getItem(position));
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mActivatedPosition != AdapterView.INVALID_POSITION) {
      outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
    }
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
      setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }
  }

  /**
   *
   */
  public void refreshData() {
    marathonListAdapter.replaceData(loadData());
  }

  public void setActivatedPosition(final int position) {
    if (position == AdapterView.INVALID_POSITION) {
      getListView().setItemChecked(mActivatedPosition, false);
    } else {
      getListView().setItemChecked(position, true);
    }

    mActivatedPosition = position;
  }

  public void setActivateOnItemClick(final boolean activateOnItemClick) {
    getListView().setChoiceMode(activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE : AbsListView.CHOICE_MODE_NONE);
  }

  private List<MarathonData> loadData() {
    final DataProvider dataProvider = new DataProvider(getActivity());
    return new ArrayList<MarathonData>(dataProvider.readSavedData());
  }
}
