package ch.bergturbenthal.marathontabelle.web.store;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.bergturbenthal.marathontabelle.model.MarathonData;

@Component
public class DefaultStorage implements Storage {

	private static final String SUFFIX = ".json";
	private final ObjectReader reader;
	private final ObjectWriter writer;
	private final File dataDir;
	private final File blobRoot;

	public DefaultStorage(final ObjectMapper mapper) {

		reader = mapper.readerFor(MarathonData.class);
		writer = mapper.writerFor(MarathonData.class);
		dataDir = new File(System.getProperty("user.home"), "marathon-data");
		if (!dataDir.exists())
			dataDir.mkdirs();
		blobRoot = new File(dataDir, "blob");
		if (!blobRoot.exists())
			blobRoot.mkdirs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.bergturbenthal.marathontabelle.web.store.Storage#callInTransaction(
	 * java.util.concurrent.Callable)
	 */
	@Override
	public <V> V callInTransaction(final Callable<V> callable) {
		try {
			return callable.call();
		} catch (final Exception e) {
			throw new RuntimeException("Error in transaction", e);
		}
	}

	private File dataFile(final String name) {
		return new File(dataDir, name + SUFFIX);
	}

	@Override
	public File getBlobRoot() {
		return blobRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.bergturbenthal.marathontabelle.web.store.Storage#getMarathon(java.lang
	 * .String)
	 */
	@Override
	public MarathonData getMarathon(final String name) {
		try {
			final File dataFile = dataFile(name);
			final MarathonData value;
			if (!dataFile.exists()) {
				value = new MarathonData();
			} else
				value = reader.readValue(dataFile);
			value.setMarathonName(name);
			return value;
		} catch (final IOException e) {
			throw new RuntimeException("Cannot load file " + name, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.bergturbenthal.marathontabelle.web.store.Storage#listMarathons()
	 */
	@Override
	public Collection<String> listMarathons() {
		return Arrays.asList(dataDir.listFiles()).stream().filter(f -> f.isFile() && f.canWrite()).map(f -> f.getName())
				.filter(name -> name.endsWith(SUFFIX)).map(name -> name.substring(0, name.length() - SUFFIX.length()))
				.collect(Collectors.toList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.bergturbenthal.marathontabelle.web.store.Storage#saveMaraton(ch.
	 * bergturbenthal.marathontabelle.model.MarathonData)
	 */
	@Override
	public void saveMaraton(final MarathonData data) {
		try {
			writer.writeValue(dataFile(data.getMarathonName()), data);
		} catch (final IOException e) {
			throw new RuntimeException("Cannot store file " + data.getMarathonName(), e);
		}
	}

	@Override
	public File createStoreFile(final String filename) {
		final int endingPos = filename.lastIndexOf('.');
		final String ending;
		if (endingPos > 0) {
			ending = filename.substring(endingPos);
		} else
			ending = "";
		final String newFilename = UUID.randomUUID().toString() + ending;
		return new File(blobRoot, newFilename);
	}
}
