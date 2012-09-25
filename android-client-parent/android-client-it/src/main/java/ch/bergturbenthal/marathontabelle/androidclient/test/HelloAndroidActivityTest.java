package ch.bergturbenthal.marathontabelle.androidclient.test;

import android.test.ActivityInstrumentationTestCase2;
import ch.bergturbenthal.marathontabelle.androidclient.HelloAndroidActivity;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {

  public HelloAndroidActivityTest() {
    super(HelloAndroidActivity.class);
  }

  public void testActivity() {
    final HelloAndroidActivity activity = getActivity();
    assertNotNull(activity);
  }
}
