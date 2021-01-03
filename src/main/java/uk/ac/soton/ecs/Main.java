package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import java.io.File;
import java.util.Map;

public class Main
{
    public static void main( String[] args ) throws FileSystemException
    {
        // Load datasets
        GroupedDataset trainingData = loadTrainingData();
        ListDataset testingData = loadTestingData();

        // Run #1 (K Nearest Neighbour)
        KNN knn = new KNN(10);
        knn.train(trainingData);
        Map<String, String> result = knn.test(testingData);
    }

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
