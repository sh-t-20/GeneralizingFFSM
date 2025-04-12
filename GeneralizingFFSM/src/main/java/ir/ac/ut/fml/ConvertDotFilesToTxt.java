package ir.ac.ut.fml;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.checkerframework.checker.nullness.qual.Nullable;

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.SimpleConfiguration;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.fts.FtsUtils;

public class ConvertDotFilesToTxt {

	public static final String DIR = "dir";

	public static final Function<Map<String, String>, Pair<@Nullable String, @Nullable Word<String>>> MEALY_EDGE_WORD_STR_PARSER = attr -> {
		final String label = attr.get(EdgeAttrs.LABEL);
		if (label == null) {
			return Pair.of(null, null);
		}

		final String[] tokens = label.split("/");

		if (tokens.length != 2) {
			return Pair.of(null, null);
		}

		Word<String> token2 = Word.epsilon();
		token2 = token2.append(tokens[1]);
		return Pair.of(tokens[0], token2);
	};

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

			File dir_1 = new File(line.getOptionValue(DIR));

			File[] files = dir_1.listFiles();
			for (File a : files) {
				String file_name = a.getName();
				if (file_name.endsWith("config")) {
					String file_path = a.getParent();
					System.out.println(file_path);

					String config_name = file_name.replaceFirst("[.][^.]+$", "");
					System.out.println(config_name);
					String fsm_file_name = config_name + "_fsm.dot";

					File fsm_file = new File(file_path + File.separator + fsm_file_name);
					CompactMealy<String, Word<String>> mealy_1 = LoadMealy(fsm_file);

					SimpleConfiguration config = FtsUtils.getInstance().loadConfiguration(a.getPath());

					String header = "";

					for (int i = 0; i < config.getFeatures().length; i++) {
						header += config.getFeatures()[i].toString();
						if (i < config.getFeatures().length - 1) {
							header += "\t";
						}
					}

					FeaturedMealyUtils.getInstance().saveFSM_kiss(mealy_1,
							new File(dir_1.getPath().toString() + File.separator + config_name + "_text.txt"),
							header);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Finished!");
	}

	private static CompactMealy<String, Word<String>> LoadMealy(File fsm_file) {
		// TODO Auto-generated method stub
		InputModelDeserializer<String, CompactMealy<String, Word<String>>> parser_1 = DOTParsers
				.mealy(MEALY_EDGE_WORD_STR_PARSER);
		CompactMealy<String, Word<String>> mealy = null;
		String file_name = fsm_file.getName();
		if (file_name.endsWith("txt")) {
			try {
				mealy = Utils.getInstance().loadMealyMachine(fsm_file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return mealy;
		} else if (file_name.endsWith("dot")) {
			try {
				mealy = parser_1.readModel(fsm_file).model;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return mealy;
		}

		return null;
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(DIR, true, "Directory of FSM files");
		return options;
	}

}
