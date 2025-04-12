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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.prop4j.Literal;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.ConditionalState;
import uk.le.ac.ffsm.ConditionalTransition;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.SimplifiedTransition;

public class GeneralizeFFSM {
	private static final String HELP = "h";
	private static final String FM = "fm";
	public static final String FFSM = "ffsm";
	public static final String OUT = "out";
	public static final String ALPHABET = "alphabet";
	public static final String NO_LOOP = "no_loop";

	@SuppressWarnings("unchecked")
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

			// 1: each label generalized separately
			// 2: the feature expressions of the next labels are taken into account as well
//			int generalization_method = 1;
			int generalization_method = 3;

			File fm_file = new File(line.getOptionValue(FM));
			IFeatureModel fm = FeatureModelManager.load(fm_file.toPath()).getObject();
//			Iterable<IFeature> all_features = fm.getFeatures();

			File ffsm_file_1 = new File(line.getOptionValue(FFSM));
			FeaturedMealy<String, Word<String>> ffsm_1 = FeaturedMealyUtils.getInstance().loadFeaturedMealy(ffsm_file_1,
					fm);

			File features_alphabet_file = new File(line.getOptionValue(ALPHABET));
			String features_alphabet_string = ConvertToString(features_alphabet_file);
//			System.out.println("features_alphabet_string: " + features_alphabet_string);
			Map<String, List<String>> features_alphabet = GetFeaturesAlphabet(features_alphabet_string);
			List<String> all_features = features_alphabet.get("All Features");
			Set<String> features = features_alphabet.keySet();

			File output_dir = new File(line.getOptionValue(OUT));

			Alphabet<String> alphabet = ffsm_1.getInputAlphabet();

			for (ConditionalState<ConditionalTransition<String, Word<String>>> s : ffsm_1.getStates()) {
//				s.setCondition(null);
				Node c_1 = s.getCondition();
				Node[] true_condition = new Node[0];
				c_1.setChildren(true_condition);
				s.setCondition(c_1);
			}

			List<Integer> state_list = new ArrayList<>(ffsm_1.getStateIDs());
			int n = state_list.size();
			List<SimplifiedTransition<String, Word<String>>> t_list = new ArrayList<>();

			List<SimplifiedTransition<String, Word<String>>> same_input_transitions = new ArrayList<>();

			for (int i = 0; i < n; i++) {
				Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = ffsm_1
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

//						List<String> removable_features = new ArrayList<>(
//								Arrays.asList("Start", "High", "Command", "WaterRegulation"));

						String input = transition.getIn();
						System.out.println("input: " + input);

						// Computing the number of transitions from Si with this input which are not
						// self loops
						Map<String, List<SimplifiedTransition<String, Word<String>>>> si_transitions = ffsm_1
								.getSimplifiedTransitions(transition.getSi());
						List<SimplifiedTransition<String, Word<String>>> si_input_transitions = si_transitions
								.get(input);
						int count = 0;
						for (SimplifiedTransition<String, Word<String>> t_1 : si_input_transitions) {
							if (t_1.getSi() != t_1.getSj()) {
								count += 1;
							}
						}

						List<String> removable_features = new ArrayList<>();

						// When generalization_method == 1 or when the transition is deterministic
						if (generalization_method == 1 || (generalization_method != 1 && count <= 1)) {
							Set<String> features_set = new HashSet<>(features);
							removable_features = FindRemovableFeatures(input, features_alphabet, features_set);
							System.out.println("removable_features:\n" + removable_features);
							Node condition_1_2 = CompleteCondition(condition_1_1, all_features, features_alphabet);
							Node condition_2 = SimplifyTransition(condition_1_2, removable_features);
							c_transition.setCondition(condition_2);
						}

						// When generalization_method != 1 and the transition is non-deterministic
						if (generalization_method != 1 && count > 1) {
							same_input_transitions.add(transition);
							System.out.println("Repeated input: " + count);
						}
					}
				}
			}

			if (generalization_method == 2 || generalization_method == 3) {
				List<SimplifiedTransition<String, Word<String>>> same_input_transitions_copy = copyList(
						same_input_transitions);
				System.out.println("non-deterministic transitions count:" + same_input_transitions.size());
				int iterations_num = 1;
				if (generalization_method == 2)
					iterations_num = same_input_transitions_copy.size();
				else if (generalization_method == 3)
					iterations_num = 1;
				for (int i = 0; i < iterations_num; i++) {
					same_input_transitions = copyList(same_input_transitions_copy);
					while (same_input_transitions.size() > 0) {
						SimplifiedTransition<String, Word<String>> t1 = same_input_transitions.get(0);
						System.out.println(t1.getSi() + ", " + t1.getIn() + ", " + t1.getOut() + ", " + t1.getSj());
						List<SimplifiedTransition<String, Word<String>>> t1_same_input_transitions = new ArrayList<>();
						for (SimplifiedTransition<String, Word<String>> t2 : same_input_transitions) {
							if (t1.getSi() == t2.getSi() && t1.getIn().equals(t2.getIn())) {
								t1_same_input_transitions.add(t2);
							}
						}

						List<SimplifiedTransition<String, Word<String>>> next_transitions = new ArrayList<>();
						Set<String> non_removable_features = new HashSet<>();
						for (SimplifiedTransition<String, Word<String>> t : t1_same_input_transitions) {
							if (t.getSi() != t.getSj()) {
								System.out.println("next");
								System.out.println(t.getSi() + ", " + t.getIn() + ", " + t.getOut() + ", " + t.getSj());
								Integer Sj = t.getSj();
								Map<String, List<SimplifiedTransition<String, Word<String>>>> sj_transitions = ffsm_1
										.getSimplifiedTransitions(Sj);
								for (String input_next : alphabet) {
									List<SimplifiedTransition<String, Word<String>>> sj_input2_transitions = sj_transitions
											.get(input_next);
									if (sj_input2_transitions != null) {
										for (SimplifiedTransition<String, Word<String>> t_next : sj_input2_transitions) {
											if (t_next.getSi() != t_next.getSj()) {
												next_transitions.add(t_next);
											}
										}
									}
								}
							}
						}
						switch (generalization_method) {
						case 2:
							for (SimplifiedTransition<String, Word<String>> t_1 : next_transitions) {
								ConditionalTransition<String, Word<String>> c_t_1 = (ConditionalTransition<String, Word<String>>) t_1
										.getTransition();
								Set<String> c_t_1_non_removable_features = getExclusiveFeatures(c_t_1.getCondition());
								non_removable_features.addAll(c_t_1_non_removable_features);
							}
							break;
						case 3:
							List<String> removable_features_next = new ArrayList<>();
							for (SimplifiedTransition<String, Word<String>> t_1 : next_transitions) {
								String input_next = t_1.getIn();
								Set<String> features_set = new HashSet<>(features);
								removable_features_next = FindRemovableFeatures(input_next, features_alphabet,
										features_set);
								for (String feature : all_features) {
									if (!removable_features_next.contains(feature)) {
										non_removable_features.add(feature);
									}
								}
							}
							break;
						}

						System.out.println("non-removable_features:\n" + non_removable_features);

						for (SimplifiedTransition<String, Word<String>> t : t1_same_input_transitions) {
							System.out.println("transition: " + t.getSi() + ", " + t.getIn() + ", " + t.getOut() + ", "
									+ t.getSj());
							String input = (String) t.getIn();
							ConditionalTransition<String, Word<String>> c_t = (ConditionalTransition<String, Word<String>>) t
									.getTransition();
							Node condition_1_1 = c_t.getCondition();
							List<String> removable_features = new ArrayList<>();
							Set<String> features_set = new HashSet<>(features);
							removable_features = FindRemovableFeatures(input, features_alphabet, features_set);
							removable_features.removeAll(non_removable_features);
							System.out.println("removable_features:\n" + removable_features);
							Node condition_1_2 = CompleteCondition(condition_1_1, all_features, features_alphabet);
							Node condition_2 = SimplifyTransition(condition_1_2, removable_features);
							c_t.setCondition(condition_2);
							same_input_transitions.remove(t);
						}
					}
				}
			}

			String ffsm_name = ffsm_file_1.getName().replaceFirst("[.][^.]+$", "") + "_generalized_"
					+ generalization_method;

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
		System.out.println("Finished!");
	}

	private static Set<String> getExclusiveFeatures(Node c) {
		// TODO Auto-generated method stub
		System.out.println("Finding exclusive features:");
		Set<String> result = new HashSet<>();
//		System.out.println(c);
		Set<Literal> literals = new HashSet<>(c.getLiterals());
		Set<String> literals_string = new HashSet<>();
		for (Literal literal : literals) {
			literals_string.add(literal.toString());
		}
//		System.out.println(literals_string);
		for (Literal literal : literals) {
			int exclusive = 1;
			if (literal.positive == true && literals_string.contains("-" + literal.toString())) {
				exclusive = 0;
			} else if (literal.positive == false && literals_string.contains(literal.toString().substring(1))) {
				exclusive = 0;
			}
			if (exclusive == 1) {
				result.add(literal.getContainedFeatures().get(0).toString());
			}
		}
		System.out.println("result:" + result);
		return result;
	}

	private static <T> List<T> copyList(List<T> list_1) {
		// TODO Auto-generated method stub
		List<T> list_2 = new ArrayList<>();
		for (T a : list_1) {
			list_2.add(a);
		}
		return list_2;
	}

	private static Node CompleteCondition(Node c_1, List<String> all_features,
			Map<String, List<String>> features_alphabet_1) {
		// TODO Auto-generated method stub
//		System.out.println("Complete condition:");
//		System.out.println("all_features:\n" + all_features.toString());

		List<String> list_0 = features_alphabet_1.get("Common Features");

		c_1 = c_1.toDNF();
		System.out.println("Initial feature expression:\n" + c_1);

		Node c_2 = c_1.clone();
		Node[] disjuncts_1 = c_2.getChildren();

		List<Node> complete_nodes_list = new ArrayList<>();
		for (Node d_1 : disjuncts_1) {
			Node[] d_terms_1 = d_1.getChildren();
			List<Node> d_terms_2_list = new ArrayList<Node>(Arrays.asList(d_terms_1));
//			System.out.println("d_terms_2_list:\n" + d_terms_2_list.toString());
			for (String f : all_features) {
				if (!d_1.getContainedFeatures().contains(f) && !list_0.contains(f.toString())) {
//					System.out.println("f:\n" + f.toString());
					Literal f_literal = new Literal(f.toString(), false);
					Node f_node = f_literal;
//					System.out.println("f_node:\n" + f_node);
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
		return c_2;
	}

	private static List<String> FindRemovableFeatures(String input_1, Map<String, List<String>> features_alphabet_1,
			Set<String> features_1) {
		// TODO Auto-generated method stub
//		System.out.println("features_1:\n" + features_1);
		features_1.remove("Common Features");
		features_1.remove("Common Alphabet");
		features_1.remove("All Features");
		List<String> remaining_features_list = new ArrayList<>();
		List<String> common_alphabet_list = features_alphabet_1.get("Common Alphabet");
		List<String> common_features_list = features_alphabet_1.get("Common Features");
		List<String> only_common_alphabet_list = new ArrayList<>();

		for (String c : common_alphabet_list) {
			int visited = 0;
			for (String e : features_1) {
				List<String> list_1 = features_alphabet_1.get(e);
				if (list_1.contains(c)) {
					visited = 1;
					break;
				}
			}
			if (visited == 0) {
				only_common_alphabet_list.add(c);
			}
		}
//		System.out.println("only_common_alphabet_list:\n" + only_common_alphabet_list);
		
		if (features_1.contains("LED_Power_Window") && features_1.contains("LED_Finger_Protection")) {
			features_1.add("Status_LED");
		}

		if (common_alphabet_list != null && only_common_alphabet_list.contains(input_1)) {
			List<String> result = new ArrayList<>(features_1);
//			System.out.println("features_2:\n" + features_1);
			return result;
		}
		for (String e : features_1) {
			List<String> list_1 = features_alphabet_1.get(e);
			if (list_1 != null && list_1.contains(input_1)) {
				remaining_features_list.add(e);
			}
		}
		Set<String> remaining_features = new HashSet<String>(remaining_features_list);
		features_1.removeAll(remaining_features);
		List<String> result = new ArrayList<>(features_1);

//		if (result.contains("LED_Power_Window") && result.contains("LED_Finger_Protection")) {
//			result.add("Status_LED");
//		}
		result.addAll(common_features_list);
//		System.out.println("features_2:\n" + features_1);
		return result;
	}

	private static Map<String, List<String>> GetFeaturesAlphabet(String f_alphabet_string) {
		// TODO Auto-generated method stub
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

	private static String ConvertToString(File string_file) {
		// TODO Auto-generated method stub
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

	private static Node SimplifyTransition(Node c_1, List<String> r_features) {
		// TODO Auto-generated method stub
		c_1 = c_1.toDNF();
		System.out.println("c_1:\n" + c_1);
//		System.out.println("removable features:\n" + r_features);
		Node c_2 = c_1.clone();
		Node[] disjuncts_1 = c_2.getChildren();
//		System.out.println("desjunct_1:" + disjuncts_1);
		List<Node> remained_nodes_list = new ArrayList<>();
		for (Node d_1 : disjuncts_1) {
//			System.out.println("d_1:" + d_1);
			Node[] d_terms_1 = d_1.getChildren();
//			System.out.println("d_terms_1:" + Arrays.toString(d_terms_1));
			List<Node> d_terms_2_list = new ArrayList<>();
			for (Node t_1 : d_terms_1) {
//				System.out.println(t_1);
				if (!r_features.contains(t_1.getContainedFeatures().get(0).toString())) {
					d_terms_2_list.add(t_1);
				}
			}
//			System.out.println("d_terms_2_list:" + d_terms_2_list);
			Node[] d_terms_2 = new Node[d_terms_2_list.size()];
			int index = 0;
			for (Node e : d_terms_2_list) {
				d_terms_2[index] = e;
				index += 1;
			}
			d_1.setChildren(d_terms_2);
//			System.out.println("d_1:" + d_1);
			if (d_terms_2.length > 0) {
				remained_nodes_list.add(d_1);
			}
		}
		Node[] remained_nodes = new Node[remained_nodes_list.size()];
		int index_r = 0;
		for (Node e : remained_nodes_list) {
			remained_nodes[index_r] = e;
			index_r += 1;
		}
		c_2.setChildren(remained_nodes);
//		System.out.println("c_2:\n" + c_2);

		Node c_3 = c_2.toCNF().toDNF();
		System.out.println("simplified condition:\n" + c_3);
//		System.out.println(c_3.getSatisfyingAssignments());
		return c_3;
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
