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
package eu.annocultor.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

/**
 * Checks if an image is marked as existing on the Web.
 * 
 * @see ImageDownloader
 * @author Borys Omelayenko
 * 
 */
public final class ImageOnlineChecker
{
	private Properties existingImages = new Properties();
	private Properties allImages = new Properties();
	private boolean debugMode = false;

	/**
	 * If image exists on the Web.
	 * 
	 * @param imageRef
	 * @return
	 * @throws Exception
	 */
	public final boolean isExisting(String imageRef) throws Exception
	{
		if (imageRef == null)
			return false;
		allImages.setProperty(imageRef, "unknown");
		if (debugMode)
			return true;
		return existingImages.getProperty(imageRef, "bad").equals("ok");
	}

	/**
	 * If there is one image that exists on the Web.
	 * 
	 * @param imageRef
	 * @return
	 * @throws Exception
	 */
	public final boolean isExisting(List<String> imageRef) throws Exception
	{
		boolean result = false;
		for (String image : imageRef)
		{
			result |= isExisting(image);
		}
		return result;
	}

	/**
	 * Loads list with existing images.
	 * 
	 * @throws Exception
	 */
	public final void load() throws Exception
	{
		if (debugMode)
			return;
		existingImages.load(new FileInputStream(new File("./img/"
			+ ImageDownloader.EXISTING_IMAGES_LOCAL_FILENAME)));
	}

	/**
	 * Saves the list with all images. Typically used to run
	 * <code>ImageDownloader</code> to download and check them and convert ot the
	 * list of existing images.
	 * 
	 * @throws Exception
	 */
	public final void save() throws Exception
	{
		File img = new File("img");
		if (!img.exists())
		{
			img.mkdir();
		}
		allImages
				.store(new FileOutputStream(new File(img, "all" + ImageDownloader.EXISTING_IMAGES_LOCAL_FILENAME)),
						"All images that are mentioned in the dataset, not all of them may be actually shipped by the image server.");
	}

	/**
	 * Imitates that all images exist. Debug only.
	 * 
	 * @param debugMode
	 */
	public void setDebugMode(boolean debugMode)
	{
		this.debugMode = debugMode;
	}
}
