package ch.bergturbenthal.marathontabelle.web.store;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import ch.bergturbenthal.filestore.core.FileBackend;
import ch.bergturbenthal.filestore.core.FileStorage;
import ch.bergturbenthal.filestore.core.FileStorage.ReadPolicy;
import ch.bergturbenthal.filestore.jackson.JacksonBackend;
import ch.bergturbenthal.marathontabelle.model.MarathonData;

public class Storage {
	private final FileStorage store;

	public Storage() {
		final File dataDir = new File(System.getProperty("user.home"), "marathon-data");
		final Collection<?> backends = Arrays.asList(((FileBackend<?>) new JacksonBackend<MarathonData>(dataDir, MarathonData.class, 0)));
		store = new FileStorage((Collection<FileBackend<?>>) backends);
	}

	public <V> V callInTransaction(final Callable<V> callable) {
		return store.callInTransaction(callable);
	}

	public MarathonData getMarathon(final String name, final ReadPolicy policy) {
		final MarathonData value = store.getObject(name, MarathonData.class, policy);
		value.setMarathonName(name);
		return value;
	}

	public Collection<String> listMarathons() {
		return store.listRelativePath(Arrays.asList(Pattern.compile(".*")), MarathonData.class);
	}

	public void saveMaraton(final MarathonData data) {
		store.putObject(data.getMarathonName(), data);
	}
}
