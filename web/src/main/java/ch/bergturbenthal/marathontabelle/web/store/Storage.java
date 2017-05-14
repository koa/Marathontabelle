package ch.bergturbenthal.marathontabelle.web.store;

import java.util.Collection;
import java.util.concurrent.Callable;

import ch.bergturbenthal.marathontabelle.model.MarathonData;

public interface Storage {

	<V> V callInTransaction(Callable<V> callable);

	MarathonData getMarathon(String name);

	Collection<String> listMarathons();

	void saveMaraton(MarathonData data);

}