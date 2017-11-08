import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;
import cc.mallet.types.*;

public class LDATopicModel
{

	public static void main(String[] args) throws Exception
	{
		runLDA( "articles.txt", 10, 200 );
	}

	public static void runLDA( String filePath, int numTopics, int numIterations  ) throws Exception
	{
		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// STEP 1: PRE-PROCESS
		
		// 1. Lowercase
		pipeList.add(new CharSequenceLowercase());
		// 2. Tokenize, using the standard settings
		pipeList.add(new CharSequence2TokenSequence());
		// 3. Stopwords removal
		pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplist.txt"), "UTF-8", false, false, false));
		// 4. Generate features
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		// read the input file
		Reader fileReader = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8");
		// read plain text lines
		instances.addThruPipe(new SelectiveFileLineIterator(fileReader, "^\\s*#.+"));

		// Create a model with parameters alpha_t = 0.01, beta_w = 0.01
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

		// Add the documents
		System.out.printf("Read %d documents\n", instances.size() );
		model.addInstances(instances);

		// STEP 2: RUN TOPIC MODEL

		// Use two parallel samplers
		model.setNumThreads(2);
		// Set the maximum number of iterations, controls the speed
		model.setNumIterations(numIterations);
		// Run the model
		model.estimate();

		// STEP 3: DISPLAY THE RESULTS
		
		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = instances.getDataAlphabet();
		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		// Show top ranked words in topics with proportions for the first document
		int topWords = 10;
		for (int topic = 0; topic < numTopics; topic++)
		{
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
			System.out.printf("Topic %02d: ", topic+1);
			int rank = 0;
			while (iterator.hasNext() && rank < topWords)
			{
				IDSorter idCountPair = iterator.next();
				if( rank > 0 )
				{
					System.out.print(", ");
				}
				System.out.printf("%s", dataAlphabet.lookupObject(idCountPair.getID()));
				rank++;
			}
			System.out.println();
		}


	}

}
