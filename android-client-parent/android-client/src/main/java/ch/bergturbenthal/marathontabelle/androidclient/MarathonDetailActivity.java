package ch.bergturbenthal.marathontabelle.androidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class MarathonDetailActivity extends FragmentActivity {

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      NavUtils.navigateUpTo(this, new Intent(this, MarathonListActivity.class));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_marathon_detail);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
      final Bundle arguments = new Bundle();
      arguments.putString(MarathonDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(MarathonDetailFragment.ARG_ITEM_ID));
      final MarathonDetailFragment fragment = new MarathonDetailFragment();
      fragment.setArguments(arguments);
      getSupportFragmentManager().beginTransaction().add(R.id.marathon_detail_container, fragment).commit();
    }
  }
}
