/*
 * (c) 2012 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.androidclient.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.content.Context;
import ch.bergturbenthal.marathontabelle.model.MarathonData;

public class DataProvider {
  private static final String FILENAME = "data.ser";
  private final Context context;

  public DataProvider(final Context context) {
    this.context = context;
  }

  public MarathonData readData(final String id) {
    final Collection<MarathonData> data = readSavedData();
    for (final MarathonData marathonData : data) {
      if (marathonData.getId().equals(id))
        return marathonData;
    }
    return null;
  }

  public Collection<MarathonData> readSavedData() {
    try {
      final FileInputStream fis = context.openFileInput(FILENAME);
      try {
        final ObjectInputStream inputStream = new ObjectInputStream(fis);
        return (Collection<MarathonData>) inputStream.readObject();
      } finally {
        fis.close();
      }
    } catch (final FileNotFoundException e) {
      final MarathonData data = new MarathonData();
      data.setMarathonName("dummy-Marathon");
      return Collections.singletonList(data);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void saveData(final Collection<MarathonData> data) {
    try {
      final OutputStream os = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
      try {
        new ObjectOutputStream(os).writeObject(data);
      } finally {
        os.close();
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

  }

  public void saveData(final MarathonData marathonData) {
    final Collection<MarathonData> savedData = readSavedData();
    final Collection<MarathonData> newSaveData = new ArrayList<MarathonData>();
    for (final MarathonData savedMarathonData : savedData) {
      if (!savedMarathonData.getId().equals(marathonData.getId()))
        newSaveData.add(savedMarathonData);
    }
    newSaveData.add(marathonData);
    saveData(newSaveData);
  }
}
