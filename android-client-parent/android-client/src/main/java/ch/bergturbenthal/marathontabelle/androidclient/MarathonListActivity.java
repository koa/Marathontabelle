package ch.bergturbenthal.marathontabelle.androidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;

public class MarathonListActivity extends FragmentActivity implements MarathonListFragment.Callbacks {

  private boolean mTwoPane;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_marathon_list);

    if (findViewById(R.id.marathon_detail_container) != null) {
      mTwoPane = true;
      ((MarathonListFragment) getSupportFragmentManager().findFragmentById(R.id.marathon_list)).setActivateOnItemClick(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.overview_menu, menu);
    return true;
  }

  @Override
  public void onItemSelected(final String id) {
    if (mTwoPane) {
      final Bundle arguments = new Bundle();
      arguments.putString(MarathonDetailFragment.ARG_ITEM_ID, id);
      final MarathonDetailFragment fragment = new MarathonDetailFragment();
      fragment.setArguments(arguments);
      getSupportFragmentManager().beginTransaction().replace(R.id.marathon_detail_container, fragment).commit();

    } else {
      final Intent detailIntent = new Intent(this, MarathonDetailActivity.class);
      detailIntent.putExtra(MarathonDetailFragment.ARG_ITEM_ID, id);
      startActivity(detailIntent);
    }
  }
}
