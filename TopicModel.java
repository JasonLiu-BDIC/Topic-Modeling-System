package cc.mallet.examples;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.io.*;

import java.util.Scanner;

public class TopicModel {

	public static void main(String[] args) throws Exception {
		String fileName;
		int topicNumbers;
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.println("Enter the filename for topic modeling:");
			fileName = sc.nextLine();
			File file = new File(fileName);
			if (file.exists()) {
				break;
			} else {
				System.out.println("File not found");
			}
		}
		System.out.println("Enter topic number:");
		topicNumbers = sc.nextInt();
		sc.close();

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence());
		pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplist.txt"), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
			Reader fileReader = new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8");
			instances.addThruPipe(new SelectiveFileLineIterator(fileReader, "^\\s*#.+")); // data,

		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		// Note that the first parameter is passed as the sum over topics, while
		// the second is alphasum and the third is beta
		int numTopics = topicNumbers;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.01, 0.01);

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

		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int position = 0; position < tokens.getLength(); position++) {
			out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
					topics.getIndexAtPosition(position));
		}
//		System.out.println(out);

		// Estimate the topic distribution of the first instance,
		// given the current Gibbs state.
		double[] topicDistribution = model.getTopicProbabilities(0);

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

			out = new Formatter(new StringBuilder(), Locale.US);
			out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
			int rank = 0;
			while (iterator.hasNext() && rank < 5) {
				IDSorter idCountPair = iterator.next();
				out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}
//			System.out.println(out);
			try {
	            //Open a file writer, the sencond parameter was set as "true" to append to file
	            FileWriter writer = new FileWriter("testWrite.txt", true);
	            writer.write(out.toString() + "\n");
	            writer.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}

		// Create a new instance with high probability of topic 0
		StringBuilder topicZeroText = new StringBuilder();
		Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

		int rank = 0;
		while (iterator.hasNext() && rank < 5) {
			IDSorter idCountPair = iterator.next();
			topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
			rank++;
		}


	}

}
