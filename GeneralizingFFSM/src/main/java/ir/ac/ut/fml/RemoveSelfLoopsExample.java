package ir.ac.ut.fml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;

public class RemoveSelfLoopsExample {

	private static final String FM = "fm";
	private static final String FSM = "fsm";
	private static final String NFA = "nfa";
	public static final String FFSM = "ffsm";
	public static final String OUT = "out";
	public static final String REMOVE_CONDITIONS = "remove_conditions";

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

			File output_dir = new File(line.getOptionValue(OUT));
			String header = "";

			if (line.hasOption(FSM)) {
				File fsm_file_1 = new File(line.getOptionValue(FSM));
				CompactMealy<String, Word<String>> fsm_1 = Utils.getInstance().loadMealyMachine(fsm_file_1);
				CompactMealy<String, Word<String>> fsm_2 = StateMachineUtils.removeSelfLoopsFSM(fsm_1);

				String file_name = fsm_file_1.getName().replaceFirst("[.][^.]+$", "");
				String dot_file_path = output_dir.getPath() + File.separator + file_name + "_LoopsRemoved.dot";
				BufferedWriter bw = new BufferedWriter(new FileWriter(dot_file_path));
				GraphDOT.write(fsm_2, bw);
				bw.close();

				Scanner scanner = new Scanner(new File(fsm_file_1.getPath()));
				scanner.useDelimiter("s0");
				int s0_visited = 0;
				while (scanner.hasNext() && s0_visited == 0) {
					header += scanner.next();
					s0_visited = 1;
				}
				FeaturedMealyUtils.getInstance().saveFSM_kiss(fsm_2,
						new File(output_dir.getPath() + File.separator + file_name + "_LoopsRemoved.txt"), header);
			}

			if (line.hasOption(NFA)) {
				File nfa_file_1 = new File(line.getOptionValue(NFA));
			}

			if (line.hasOption(FFSM)) {
				File fm_file = new File(line.getOptionValue(FM));
				IFeatureModel fm = FeatureModelManager.load(fm_file.toPath()).getObject();
				File ffsm_file_1 = new File(line.getOptionValue(FFSM));
				FeaturedMealy<String, Word<String>> ffsm_1 = FeaturedMealyUtils.getInstance()
						.loadFeaturedMealy(ffsm_file_1, fm);
				FeaturedMealy<String, Word<String>> ffsm_2 = StateMachineUtils.removeSelfLoopsFFSM(ffsm_1);
				
				if (line.getOptionValue(REMOVE_CONDITIONS).equals("true")) {
					ffsm_2 = StateMachineUtils.removeConditions(ffsm_2);
				}

				String file_name = ffsm_file_1.getName().replaceFirst("[.][^.]+$", "");
				String dot_file_path = output_dir.getPath() + File.separator + file_name + "_LoopsRemoved.dot";
				FeaturedMealyUtils.getInstance().saveFFSM(ffsm_2, new File(dot_file_path));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished!");
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FM, true, "Feature model");
		options.addOption(FSM, true, "FSM");
		options.addOption(NFA, true, "NFA");
		options.addOption(FFSM, true, "FFSM");
		options.addOption(OUT, true, "Output directory");
		options.addOption(REMOVE_CONDITIONS, true, "Remove conditions of FFSM states and transitions");
		return options;
	}
}
