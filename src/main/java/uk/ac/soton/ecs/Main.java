package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Main
{
    public static void main( String[] args ) throws IOException
    {
        // Load datasets
        GroupedDataset trainingData = loadTrainingData();
        ListDataset testingData = loadTestingData();

        // Run #1 (K Nearest Neighbour)
        KNN knn = new KNN(10);
        knn.train(trainingData);
        Map<String, String> knnPredictions = knn.test(testingData);
        makePredictionFile("run1", knnPredictions);
    }

    /**
     * Gets all of the images from the training datasets
     * @return VFSGroupDataset of training images
     * @throws FileSystemException If there is an error loading any of the image files
     */
    private static VFSGroupDataset<FImage> loadTrainingData() throws FileSystemException
    {
        String path = new File("training").getAbsolutePath();
        return new VFSGroupDataset<>(path, ImageUtilities.FIMAGE_READER);
    }

    /**
     * Gets all of the testing images from the testing dataset
     * @return VFSLListDataset of testing images
     * @throws FileSystemException If there is an error loading any of the image files
     */
    private static VFSListDataset<FImage> loadTestingData() throws FileSystemException
    {
        String path = new File("testing").getAbsolutePath();
        return new VFSListDataset<>(path, ImageUtilities.FIMAGE_READER);
    }

    /**
     * Writes the predictions to a txt file in the required format
     * @param fileName Name of the file to create (.txt extension added automatically)
     * @param predictions The map of image file to class predictions
     * @throws IOException If there is an error writing to the file
     */
    private static void makePredictionFile(String fileName, Map<String, String> predictions) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + ".txt"));

        for(Map.Entry<String, String> pair : predictions.entrySet())
        {
            writer.write(pair.getKey() + " " + pair.toString());
        }

        writer.close();
    }
}
