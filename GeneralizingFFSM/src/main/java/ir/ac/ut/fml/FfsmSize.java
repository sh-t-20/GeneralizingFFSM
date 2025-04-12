package ir.ac.ut.fml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.IConfigurableFSM;
import uk.le.ac.ffsm.SimplifiedTransition;

public class FfsmSize {
	private static final String FM = "fm";
	public static final String DIR = "dir";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			File feature_model_file = new File(line.getOptionValue(FM));
			IFeatureModel feature_model = FeatureModelManager.load(feature_model_file.toPath()).getObject();

			File dir_1 = new File(line.getOptionValue(DIR));
			System.out.println(dir_1.toString());

			File[] ffsm_files = dir_1.listFiles();

			for (File a : ffsm_files) {
				String file_name = a.getName();
				if (file_name.endsWith("txt")) {
					System.out.println("\n" + file_name);
					IConfigurableFSM<String, Word<String>> ffsm = FeaturedMealyUtils.getInstance().loadFeaturedMealy(a,
							feature_model);
					int states_count = ffsm.getStateIDs().size();
					System.out.println("Number of states = " + states_count);

					int nondeterministic_states_count = nondeterministicStatesCount(ffsm);
					System.out.println(
							"Number of states with nondeterministic transitions = " + nondeterministic_states_count);
					double nondeterministic_states_ratio = (double) nondeterministic_states_count / states_count;
					System.out.println("Ratio of nondeterministic states = "
							+ Math.round(nondeterministic_states_ratio * 1000.0) / 1000.0);

					int transitions_count = effectiveTransitionsCountFFSM(ffsm);
					System.out.println("Number of effective transitions = " + transitions_count);
					int total_transitions_count = totalTransitionsCountFFSM(ffsm);
					System.out.println("Number of all transitions = " + total_transitions_count);

					double nondeterministic_transitions_ratio = nondeterministicTransitionsRatio(ffsm);
					System.out.println("Ratio of nondeterministic transitions = "
							+ Math.round(nondeterministic_transitions_ratio * 1000.0) / 1000.0);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished!");
	}

	private static double nondeterministicTransitionsRatio(IConfigurableFSM<String, Word<String>> ffsm_1) {
		// TODO Auto-generated method stub
		int nondeterministic_transitions_count = 0;
		int transitions_count = 0;
		List<Integer> states_1 = ffsm_1.getStateIDs();

		for (Integer s : states_1) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> s_transitions = ffsm_1
					.getSimplifiedTransitions(s);
//			System.out.println("\nstate:" + s + ", number of inputs:" + s_transitions.size());
			transitions_count += s_transitions.size();
			for (Entry<String, List<SimplifiedTransition<String, Word<String>>>> t : s_transitions.entrySet()) {
				List<SimplifiedTransition<String, Word<String>>> list_1 = t.getValue();
//				System.out.println("number of transitions with this input:" + list_1.size());
				if (list_1.size() > 1)
					nondeterministic_transitions_count += 1;
//				for (SimplifiedTransition<String, Word<String>> transition : list_1) {
//					System.out.println("state:" + s + ", input:" + transition.getIn() + ", Si: " + transition.getSi()
//							+ ", Sj: " + transition.getSj());
//				}
			}
		}
		System.out.println("Number of nondeterministic transitions:" + nondeterministic_transitions_count);
		System.out.println("Number of transitions:" + transitions_count);
		double ratio = 0;
		if (transitions_count != 0) {
			ratio = (double) nondeterministic_transitions_count / transitions_count;
		}
		return ratio;
	}

	private static int nondeterministicStatesCount(IConfigurableFSM<String, Word<String>> ffsm_1) {
		// TODO Auto-generated method stub
		Set<Integer> nondeterministaic_states = new HashSet<>();
		List<Integer> states_1 = ffsm_1.getStateIDs();
		for (Integer s : states_1) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> s_transitions = ffsm_1
					.getSimplifiedTransitions(s);
			for (Entry<String, List<SimplifiedTransition<String, Word<String>>>> t : s_transitions.entrySet()) {
				List<SimplifiedTransition<String, Word<String>>> list_1 = t.getValue();
				SimplifiedTransition<String, Word<String>>[] array_1 = nonloopTransitions(list_1);
				for (int i = 0; i < array_1.length; i++) {
					for (int j = i + 1; j < array_1.length; j++) {
						if (array_1[i].getIn().equals(array_1[j].getIn())) {
//							System.out.println("state:" + s + ", input:" + array_1[i].getIn() + ", Si: "
//									+ array_1[i].getSi() + ", Sj: " + array_1[i].getSj() + ", duplicate found");
							nondeterministaic_states.add(s);
						}
					}
				}
			}
		}
		return nondeterministaic_states.size();
	}

	private static SimplifiedTransition<String, Word<String>>[] nonloopTransitions(
			List<SimplifiedTransition<String, Word<String>>> list_1) {
		// TODO Auto-generated method stub
		List<SimplifiedTransition<String, Word<String>>> list_2 = new ArrayList<>();
		for (SimplifiedTransition<String, Word<String>> t : list_1) {
			if (!t.getSi().equals(t.getSj()))
				list_2.add(t);
		}
		SimplifiedTransition<String, Word<String>>[] array_1 = new SimplifiedTransition[list_2.size()];
		for (int i = 0; i < array_1.length; i++)
			array_1[i] = list_2.get(i);
		return array_1;
	}

	private static int totalTransitionsCountFFSM(IConfigurableFSM<String, Word<String>> ffsm_1) {
		// TODO Auto-generated method stub
		int count = 0;
		List<Integer> states_1 = ffsm_1.getStateIDs();
		for (Integer s : states_1) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> s_transitions = ffsm_1
					.getSimplifiedTransitions(s);
			for (Entry<String, List<SimplifiedTransition<String, Word<String>>>> t : s_transitions.entrySet()) {
				List<SimplifiedTransition<String, Word<String>>> list_1 = t.getValue();
				for (SimplifiedTransition<String, Word<String>> element : list_1) {
					count += 1;
				}
			}
		}
		return count;
	}

	private static int effectiveTransitionsCountFFSM(IConfigurableFSM<String, Word<String>> ffsm_1) {
		// TODO Auto-generated method stub
		int count = 0;
		List<Integer> states_1 = ffsm_1.getStateIDs();
		for (Integer s : states_1) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> s_transitions = ffsm_1
					.getSimplifiedTransitions(s);
			for (Entry<String, List<SimplifiedTransition<String, Word<String>>>> t : s_transitions.entrySet()) {
				List<SimplifiedTransition<String, Word<String>>> list_1 = t.getValue();
				for (SimplifiedTransition<String, Word<String>> element : list_1) {
					if (!element.getSi().equals(element.getSj())) {
						count += 1;
					}
				}
			}
		}
		return count;
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FM, true, "Feature model");
		options.addOption(DIR, true, "Directory for FFSMs");
		return options;
	}
}
