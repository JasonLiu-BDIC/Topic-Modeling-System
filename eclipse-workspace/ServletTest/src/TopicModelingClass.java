
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.io.*;

public class TopicModelingClass{
	private static InputStream inputStream;
	private static Formatter outPut = new Formatter(new StringBuilder(), Locale.US);;
	String path;
	
	
	public TopicModelingClass(InputStream inputStream, String path) {
		this.inputStream = inputStream;
		this.path = path;
//		stopListFile = new File(path);
//		System.out.println(stopListFile.exists() + "" + stopListFile.isFile() + "" + stopListFile.length());
	}
	
	public Formatter processMethod() throws Exception {
		String fileName;
		int topicNumbers = 0;;

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence());
//		pipeList.add(new TokenSequenceRemoveStopwords(stopListFile, "UTF-8", true, false, true));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		Reader fileReader = new InputStreamReader(inputStream, "UTF-8");
		instances.addThruPipe(new SelectiveFileLineIterator(fileReader, "^\\s*#.+")); // data,

		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		// Note that the first parameter is passed as the sum over topics, while
		// the second is alpha sum and the third is beta
		int numTopics = topicNumbers;
		ParallelTopicModel model = new ParallelTopicModel(10, 0.01, 0.5);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and
		// combine
		// statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only,
		// for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(50);
		model.estimate();

		// Show the words and topics in the first instance

		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = instances.getDataAlphabet();

		FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
		LabelSequence topics = model.getData().get(0).topicSequence;

//		Formatter out = new Formatter(new StringBuilder(), Locale.US);

		for (int position = 0; position < tokens.getLength(); position++) {
			outPut.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
					topics.getIndexAtPosition(position));
		}

		// System.out.println(out);

		// Estimate the topic distribution of the first instance,
		// given the current Gibbs state.
		double[] topicDistribution = model.getTopicProbabilities(0);

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		// Get an array of documents in each Topic
		ArrayList<TreeSet<IDSorter>> topicSortedDocuments = model.getTopicDocuments(10.0);

		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> wordIterator = topicSortedWords.get(topic).iterator();

			outPut = new Formatter(new StringBuilder(), Locale.US);

			outPut.format("Topic %d:\t%\t", topic);
			int rank = 0;
			while (wordIterator.hasNext() && rank < 5) {
				IDSorter idCountPair = wordIterator.next();
				outPut.format("%s (%.0f) /n", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}
			
			TreeSet<IDSorter> sortedDocuments = topicSortedDocuments.get(topic);
			int position = 0;
			
			for (IDSorter sorter : sortedDocuments){
				if (position == topic){
					break;
				}
				int documentId = sorter.getID();
				double weight = sorter.getWeight();
				outPut.format("docNo" + "%d. Document ID %d (%.2f)\n", position+1, documentId, weight);
//				System.out.format("docNo" + "%d. Document ID %d (%.2f)\n", position+1, documentId, weight);
				position++;
			}
			
			
			System.out.println(outPut);
			


		

	}
		return outPut;
	}
	

	public static void main(String[] args) throws Exception {
//		String fileName;
		int topicNumbers = 0;;

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence());
		pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplist.txt"), "UTF-8", true, false, true));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		Reader fileReader = new InputStreamReader(inputStream, "UTF-8");
		instances.addThruPipe(new SelectiveFileLineIterator(fileReader, "^\\s*#.+")); // data,

		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		// Note that the first parameter is passed as the sum over topics, while
		// the second is alphasum and the third is beta
		int numTopics = topicNumbers;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.01, 0.5);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and
		// combine
		// statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only,
		// for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(50);
		model.estimate();

		// Show the words and topics in the first instance

		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = instances.getDataAlphabet();

		FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
		LabelSequence topics = model.getData().get(0).topicSequence;

//		Formatter out = new Formatter(new StringBuilder(), Locale.US);

		for (int position = 0; position < tokens.getLength(); position++) {
			outPut.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
					topics.getIndexAtPosition(position));
		}

		// System.out.println(out);

		// Estimate the topic distribution of the first instance,
		// given the current Gibbs state.
		double[] topicDistribution = model.getTopicProbabilities(0);

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		// Get an array of documents in each Topic
		ArrayList<TreeSet<IDSorter>> topicDocuments = model.getTopicDocuments(numTopics);

		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> wordIterator = topicSortedWords.get(topic).iterator();

			outPut = new Formatter(new StringBuilder(), Locale.US);

			outPut.format("%d\t%.3f\t", topic, topicDistribution[topic]);
			int rank = 0;
			while (wordIterator.hasNext() && rank < 5) {
				IDSorter idCountPair = wordIterator.next();
				outPut.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}

			System.out.println(outPut);
//			printTopicDocuments(model, topic);
//			 try {
//			 // Open a file writer, the sencond parameter was set as "true"
//			 // to append to file
//			File writeFile = new File("testWrite.txt");
//			 FileWriter writer = new FileWriter("testWrite.txt", true);
//			 PrintWriter printWriter = new PrintWriter(writeFile);
//			 model.printTopicDocuments(printWriter);
//			 writer.close();
//			 } catch (IOException e) {
//			 e.printStackTrace();
//			 }
//			out.close();
//
//		}

		

	}
		
		

}
	
	
	public void printTopicDocuments(ParallelTopicModel model, int top){
		ArrayList<TreeSet<IDSorter>> topicSortedDocuments = model.getTopicDocuments(10.0);
		for (int topic = 0; topic < model.numTopics; topic++){
			
//			System.out.printf("Topic %02d: ", topic+1);
			
			TreeSet<IDSorter> sortedDocuments = topicSortedDocuments.get(topic);
			int position = 0;
			
			for (IDSorter sorter : sortedDocuments){
				if (position == top){
					break;
				}
				int documentId = sorter.getID();
				double weight = sorter.getWeight();
//				System.out.format("docNo" + "%d. Document ID %d (%.2f)\n", position+1, documentId, weight);
				position++;
			}
		}
		
	}
	
	public static Formatter getFormatter() {
		return outPut;
	}
	

}