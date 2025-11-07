package ir.ac.ut.fml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.ConditionalState;
import uk.le.ac.ffsm.ConditionalTransition;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.SimplifiedTransition;

public class GeneralizeFfsmEdited {

	private static final String HELP = "h";
	private static final String FM = "fm";
	public static final String FFSM = "ffsm";
	public static final String OUT = "out";
	public static final String ALPHABET = "alphabet";
	public static final String NO_LOOP = "no_loop";

	public static void main(String[] args) {
		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption(HELP) || !line.hasOption(FM) || !line.hasOption(FFSM) || !line.hasOption(OUT)) {
				formatter.printHelp("Generalize FFSM", options);
				System.exit(0);
			}

			// 1: basic generalization method, 3: lookahead generalization method
//			int generalization_method_1 = 1;
			int generalization_method_1 = 3;

			System.out.println("FFSM generalization started:");

			File fm_file = new File(line.getOptionValue(FM));
			IFeatureModel feature_model = FeatureModelManager.load(fm_file.toPath()).getObject();

			File ffsm_file_1 = new File(line.getOptionValue(FFSM));
			FeaturedMealy<String, Word<String>> ffsm_1 = FeaturedMealyUtils.getInstance().loadFeaturedMealy(ffsm_file_1,
					feature_model);

			File features_alphabet_file = new File(line.getOptionValue(ALPHABET));
			String features_alphabet_string = convertToString(features_alphabet_file);

			File output_dir = new File(line.getOptionValue(OUT));

			ffsm_1 = generalizeFFSM(ffsm_1, feature_model, features_alphabet_string, generalization_method_1);

			String ffsm_name = ffsm_file_1.getName().replaceFirst("[.][^.]+$", "") + "_generalized_"
					+ generalization_method_1;

			String no_loop = line.getOptionValue(NO_LOOP);
			if (no_loop.equals("true")) {
				ffsm_1 = StateMachineUtils.removeSelfLoopsFFSM(ffsm_1);
				ffsm_name += "_LoopsRemoved";
			}

			FeaturedMealyUtils.getInstance().saveFFSM_kiss(ffsm_1, new File(output_dir, ffsm_name + ".txt"));
			FeaturedMealyUtils.getInstance().saveFFSM(ffsm_1, new File(output_dir, ffsm_name + ".dot"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("FFSM generalization finished!");
	}

	private static FeaturedMealy<String, Word<String>> generalizeFFSM(FeaturedMealy<String, Word<String>> ffsm_1_1,
			IFeatureModel fm, String features_alphabet_string, int generalization_method) {

		features_alphabet_string = features_alphabet_string.replace("/", "_");
		System.out.println("features_alphabet_string:\n" + features_alphabet_string);
		Map<String, List<String>> features_alphabet = getFeaturesAlphabet(features_alphabet_string);
		List<String> all_features = features_alphabet.get("All Features");
		System.out.println("all_features:\n" + all_features);
		List<String> common_features = features_alphabet.get("Common Features");
		List<String> common_alphabet = features_alphabet.get("Common Alphabet");

		Set<String> features = features_alphabet.keySet();
		features.remove("Common Features");
		features.remove("Common Alphabet");
		features.remove("All Features");

		// setting the condition of conditional states to true
		for (ConditionalState<ConditionalTransition<String, Word<String>>> s : ffsm_1_1.getStates()) {
			Node c_1 = s.getCondition();
			Node[] true_condition = new Node[0];
			c_1.setChildren(true_condition);
			s.setCondition(c_1);
		}

		List<Integer> state_list = new ArrayList<>(ffsm_1_1.getStateIDs());
		int n = state_list.size();
		List<SimplifiedTransition<String, Word<String>>> t_list = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = ffsm_1_1
					.getSimplifiedTransitions(state_list.get(i));
			for (Entry<String, List<SimplifiedTransition<String, Word<String>>>> t : transitions.entrySet()) {
				List<SimplifiedTransition<String, Word<String>>> list_1 = t.getValue();
				for (SimplifiedTransition<String, Word<String>> transition : list_1) {
					t_list.add(transition);
					System.out.println(transition.getSi() + ", " + transition.getIn() + ", " + transition.getOut()
							+ ", " + transition.getSj());

					@SuppressWarnings("unchecked")
					ConditionalTransition<String, Word<String>> c_transition = (ConditionalTransition<String, Word<String>>) transition
							.getTransition();

					Node condition_1_1 = c_transition.getCondition();

					String input = transition.getIn();
					System.out.println("input: " + input);

					List<String> removable_features = new ArrayList<>();

					Set<String> features_set = new HashSet<>(features);
					if (common_alphabet.contains(input)) {
						removable_features = new ArrayList<>(all_features);
					} else {
						removable_features = new ArrayList<>(
								findRemovableFeatures(input, features_alphabet, features_set, common_features));
					}

					if (generalization_method == 1) {
						System.out.println("removable_features:\n" + removable_features);
					} else if (generalization_method == 3) {
						if (isNondeterministic(ffsm_1_1, transition)) {
							System.out.println("Nondeterministic input");
							List<String> next_transitions_features = findNextTransitionsFeatures(ffsm_1_1, transition,
									features_alphabet, common_alphabet);
							System.out.println("Features of next transitions:\n" + next_transitions_features);
							if (next_transitions_features != null)
								removable_features.removeAll(next_transitions_features);
						}
						System.out.println("removable_features:\n" + removable_features);
					}

					Node condition_1_2 = completeCondition(condition_1_1, all_features, features_alphabet);
					Node condition_2 = simplifyTransition(condition_1_2, removable_features, all_features);
					c_transition.setCondition(condition_2);
				}
			}
		}

		return ffsm_1_1;
	}

	private static boolean isNondeterministic(FeaturedMealy<String, Word<String>> ffsm,
			SimplifiedTransition<String, Word<String>> transition) {
		int s_i = transition.getSi();
		String input = transition.getIn();

		Map<String, List<SimplifiedTransition<String, Word<String>>>> transitionsMap = ffsm
				.getSimplifiedTransitions(s_i);

		if (transitionsMap == null) {
			return false;
		}

		List<SimplifiedTransition<String, Word<String>>> transitionsForInput = transitionsMap.get(input);

		if (transitionsForInput == null) {
			return false;
		}

		int countNonLoop = 0;
		for (SimplifiedTransition<String, Word<String>> t : transitionsForInput) {
			if (t.getSj() != s_i) {
				countNonLoop++;
			}
		}

		return countNonLoop > 1;
	}

	private static List<String> findNextTransitionsFeatures(FeaturedMealy<String, Word<String>> ffsm,
			SimplifiedTransition<String, Word<String>> transition, Map<String, List<String>> features_alphabet,
			List<String> common_alphabet) {

		int s_i = transition.getSi();
		String input = transition.getIn();

		Map<String, List<SimplifiedTransition<String, Word<String>>>> transitionsMap = ffsm
				.getSimplifiedTransitions(s_i);
		if (transitionsMap == null) {
			return null;
		}

		List<SimplifiedTransition<String, Word<String>>> sameInputTransitions = transitionsMap.get(input);
		if (sameInputTransitions == null) {
			return null;
		}

		Set<String> nextInputs = new HashSet<>();

		for (SimplifiedTransition<String, Word<String>> t : sameInputTransitions) {
			int s_j = t.getSj();

			Map<String, List<SimplifiedTransition<String, Word<String>>>> nextTransitionsMap = ffsm
					.getSimplifiedTransitions(s_j);
			if (nextTransitionsMap == null) {
				continue;
			}

			for (Map.Entry<String, List<SimplifiedTransition<String, Word<String>>>> entry : nextTransitionsMap
					.entrySet()) {
				String nextInput = entry.getKey();
				List<SimplifiedTransition<String, Word<String>>> nextTransitions = entry.getValue();

				for (SimplifiedTransition<String, Word<String>> nextT : nextTransitions) {
					int nextSj = nextT.getSj();
					if (nextSj != s_j) {
						nextInputs.add(nextInput);
						break;
					}
				}
			}
		}

		System.out.println("Next inputs:\n" + nextInputs);
		Set<String> featuresWithNextInputs = new HashSet<>();
		for (Map.Entry<String, List<String>> entry : features_alphabet.entrySet()) {
			String feature = entry.getKey();
			List<String> alphabet = entry.getValue();
			for (String nextInput : nextInputs) {
				if (!common_alphabet.contains(nextInput) && alphabet.contains(nextInput)) {
					featuresWithNextInputs.add(feature);
					break;
				}
			}
		}

		return new ArrayList<>(featuresWithNextInputs);
	}

	private static Node simplifyTransition(Node c_1, List<String> r_features, List<String> all_features) {
		c_1 = c_1.toDNF();
//		System.out.println("Original condition (DNF):\n" + c_1);

		Node simplified = removeFeatures(c_1, r_features, all_features);

		if (simplified == null) {
			System.out.println("All features removed — returning TRUE node.");
			And result = new And(new Node[0]);
			return result; // empty node representing empty condition
		}

		simplified = simplified.toDNF();
		simplified = removeDuplicateNodes(simplified);

		System.out.println("Simplified condition:\n" + simplified);
		return simplified;
	}

	private static Node removeFeatures(Node node, List<String> r_features, List<String> all_features) {
		if (node == null)
			return null;

		Node[] children = node.getChildren();
		boolean isLeaf = (children == null || children.length == 0);

		if (isLeaf) {
			// Leaf node: check its feature
			List<String> features = node.getContainedFeatures();
			if (features != null && !features.isEmpty()) {
				String feature = features.get(0); // assuming one feature per leaf
				if (r_features.contains(feature) || !all_features.contains(feature)) {
					return null; // remove this node
				}
			}
			return node.clone(); // keep this node
		}

		// Non-leaf: recursively process children
		List<Node> newChildren = new ArrayList<>();
		for (Node child : children) {
			Node cleanedChild = removeFeatures(child, r_features, all_features);
			if (cleanedChild != null) {
				newChildren.add(cleanedChild);
			}
		}

		// If all children were removed, remove this node too
		if (newChildren.isEmpty()) {
			return null;
		}

		// Clone this node and set cleaned children
		Node newNode = node.clone();
		newNode.setChildren(newChildren.toArray(new Node[0]));

		return newNode;
	}

	private static Node completeCondition(Node c_1, List<String> all_features,
			Map<String, List<String>> features_alphabet_1) {

		List<String> list_0 = features_alphabet_1.get("Common Features");

		c_1 = c_1.toDNF();
		System.out.println("Initial feature expression:\n" + c_1);

		Node c_2 = c_1.clone();
		Node[] disjuncts_1 = c_2.getChildren();

		for (Node d_1 : disjuncts_1) {
			Node[] d_terms_1 = d_1.getChildren();
			List<Node> d_terms_2_list = new ArrayList<Node>(Arrays.asList(d_terms_1));
			for (String f : all_features) {
				if (!d_1.getContainedFeatures().contains(f) && !list_0.contains(f.toString())) {
					Literal f_literal = new Literal(f.toString(), false);
					Node f_node = f_literal;
					d_terms_2_list.add(f_node);
				}
			}

			Node[] d_terms_2 = new Node[d_terms_2_list.size()];
			int index = 0;
			for (Node e : d_terms_2_list) {
				d_terms_2[index] = e;
				index += 1;
			}
			d_1.setChildren(d_terms_2);

		}

//		System.out.println("Complete feature expression:\n" + c_2);
		return c_2;
	}

	private static List<String> findRemovableFeatures(String input_1, Map<String, List<String>> features_alphabet_1,
			Set<String> features_1, List<String> common_features) {
		System.out.println("features_1:\n" + features_1);

		if (features_1.contains("LED_Power_Window") && features_1.contains("LED_Finger_Protection")) {
			features_1.add("Status_LED");
		}

		List<String> remaining_features_list = new ArrayList<>();
		for (String e : features_1) {
			List<String> list_1 = features_alphabet_1.get(e);
			if (list_1 != null && list_1.contains(input_1)) {
				remaining_features_list.add(e);
			}
		}
		Set<String> remaining_features = new HashSet<String>(remaining_features_list);
		features_1.removeAll(remaining_features);
		List<String> result = new ArrayList<>(features_1);

		result.addAll(common_features);
		return result;
	}

	private static Map<String, List<String>> getFeaturesAlphabet(String f_alphabet_string) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(f_alphabet_string);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
//			System.out.println("line:\n" + line);
			List<String> list_1 = Arrays.asList(line.split(":"));
//			System.out.println(list_1);
			String key_1 = list_1.get(0);
			list_1.set(1, list_1.get(1).replaceAll("\\s", ""));
			List<String> Value_1 = Arrays.asList(list_1.get(1).split(","));
			result.put(key_1, Value_1);
		}
		return result;
	}

	private static String convertToString(File string_file) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader buffer = new BufferedReader(new FileReader(string_file.getAbsolutePath()))) {
			String result = "";
			while ((result = buffer.readLine()) != null) {
//				System.out.println(result);
				builder.append(result).append("\n");
			}
			return builder.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static Node removeDuplicateNodes(Node node) {
		if (node == null) {
			return null;
		}

		Node[] children = node.getChildren();
		if (children == null || children.length == 0) {
			// Leaf node, return as is
			return node.clone();
		}

		// Recursively simplify children first
		List<Node> simplifiedChildren = new ArrayList<>();
		for (Node child : children) {
			simplifiedChildren.add(removeDuplicateNodes(child));
		}

		// Remove duplicates among children
		LinkedHashSet<Node> uniqueChildren = new LinkedHashSet<>(simplifiedChildren);

		Node newNode = node.clone();
		newNode.setChildren(uniqueChildren.toArray(new Node[0]));

		return newNode;
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(HELP, false, "Help menu");
		options.addOption(FM, true, "Feature model");
		options.addOption(FFSM, true, "FFSM");
		options.addOption(OUT, true, "Output directory");
		options.addOption(ALPHABET, true, "Alphabet of features");
		options.addOption(NO_LOOP, true, "For removing the self loops, this feature is set to true.");
		return options;
	}
}
