/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.reports.parts;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Named counters, autoflush.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ReportCounter<T> extends AbstractReportPart {

	Logger log = LoggerFactory.getLogger(getClass().getName());

	private CounterMap<T> counters = new CounterMap<T>();

	/**
	 * Named counters, should be flushed at the end. In the meantime, when too
	 * many counters are collected, they are saved to a file. This is a
	 * partially aggregated file, as the same counter may occur there many
	 * times..
	 * 
	 * @param dir
	 *            file to save counters to
	 * @param maxSize
	 *            maximal number of counters to keep in memory
	 * @throws Exception
	 */
	public ReportCounter(File dir, String file, int maxSize) throws Exception {
		super(new File(dir, file), maxSize);
	}

	public void inc(T category) throws IOException {
		inc(category, 1);
	}

	public void inc(T category, Integer offset) throws IOException {
		counters.inc(category, offset);
	}

	@Override
	public void flush() throws IOException {
		XStream xStream = new XStream();
		OutputStream fos = new FileOutputStream(getFile(), true);
		ObjectOutputStream out = xStream.createObjectOutputStream(fos);
		try {
			for (ObjectCountPair<T> pair : asSorted()) {
				out.writeObject(pair);
			}
			empty();
		} finally {
			out.close();
			fos.close();
		}
	}

	public Set<Entry<T, Integer>> entrySet() {
		return counters.entrySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load() throws IOException {
		XStream xStream = new XStream();
		CountingInputStream cis = new CountingInputStream(new FileInputStream(
				getFile()));
		ObjectInputStream in = xStream.createObjectInputStream(cis);
		try {
			int mb = 1;
			// how ugly but it should throw exception at the end
			while (true) {
				ObjectCountPair<T> ocp = (ObjectCountPair<T>) in.readObject();
				inc(ocp.getObject(), ocp.getCount());
				if (cis.getByteCount() > FileUtils.ONE_MB * 100 * mb) {
					log.info("Passed "
							+ (cis.getByteCount() / FileUtils.ONE_MB) + " MB");
					mb++;
				}
			}
		} catch (EOFException e) {
			// normal end of file
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} finally {
			in.close();
			cis.close();
		}
	}

	public List<ObjectCountPair<T>> asList() {
		List<ObjectCountPair<T>> list = new ArrayList<ObjectCountPair<T>>();
		for (Entry<T, Integer> entry : counters.entrySet()) {
			list.add(new ObjectCountPair<T>(entry.getKey(), entry.getValue()));
		}
		return list;
	}

	public List<ObjectCountPair<T>> asSorted() {
		List<ObjectCountPair<T>> list = asList();
		Collections.sort(list);
		return list;
	}

	public static class ObjectCountPair<T> implements
			Comparable<ObjectCountPair<T>> {
		private T object;
		private Integer count;

		public ObjectCountPair(T object, Integer count) {
			super();
			this.object = object;
			this.count = count;
		}

		public T getObject() {
			return object;
		}

		public Integer getCount() {
			return count;
		}

		@Override
		public int compareTo(ObjectCountPair<T> o) {
			return o.getCount().compareTo(getCount());
		}

		@Override
		public String toString() {
			return "" + object + "=" + count;
		}

	}

}
