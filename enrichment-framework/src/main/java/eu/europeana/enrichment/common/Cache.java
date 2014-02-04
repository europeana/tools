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
package eu.europeana.enrichment.common;

import java.util.HashMap;

/**
 * Cache
 * 
 * @author Borys Omelayenko
 * 
 */
@Deprecated
public class Cache<KEY, VALUE>
{

	private final int SIZE_LIMIT = 1024;

	public void clear() {
		cache.clear();
	}

	HashMap<KEY, VALUE> cache = new HashMap<KEY, VALUE>();

	public VALUE put(KEY key, VALUE value) {
		if (cache.size() < SIZE_LIMIT) {
			cache.put(key, value);
		}
		return value;
	}

	public VALUE get(KEY key) {
		return cache.get(key);
	}

	public boolean contains(KEY key) {
		return cache.containsKey(key);
	}
}
