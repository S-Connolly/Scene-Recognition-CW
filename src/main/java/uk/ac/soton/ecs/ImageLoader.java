package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import java.io.File;

public class ImageLoader
{
	/**
	 * Gets all of the images from the training datasets
	 * @return VFSGroupDataset of training images
	 * @throws FileSystemException If there is an error loading any of the image files
	 */
	public static VFSGroupDataset<FImage> loadTrainingData() throws FileSystemException
	{
		String path = new File("training").getAbsolutePath();
		return new VFSGroupDataset<>(path, ImageUtilities.FIMAGE_READER);
	}

	/**
	 * Gets all of the testing images from the testing dataset
	 * @return VFSLListDataset of testing images
	 * @throws FileSystemException If there is an error loading any of the image files
	 */
	public static VFSListDataset<FImage> loadTestingData() throws FileSystemException
	{
		String path = new File("testing").getAbsolutePath();
		return new VFSListDataset<>(path, ImageUtilities.FIMAGE_READER);
	}
}
