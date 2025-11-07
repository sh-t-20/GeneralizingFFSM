package ir.ac.ut.fml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

public class ConvertFtsFormat2 {
	public static final String FTS = "fts";
	public static final String DIR = "dir";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			File FTS_file = new File(line.getOptionValue(FTS));
			File out_dir = new File(line.getOptionValue(DIR));

			List<String> lines = Files.readAllLines(Path.of(FTS_file.getAbsolutePath()), StandardCharsets.UTF_8);

			Map<String, String> nodeIdToLabel = new LinkedHashMap<>();

			Pattern nodePattern = Pattern.compile("\\s*(state\\d+)\\s*\\[\\s*label\\s*=\\s*\"([^\"]+)\".*\\];?");

			String startStateLabel = null;

			for (String line_1 : lines) {
				if (line_1.contains("->")) {
					continue;
				}

				Matcher matcher = nodePattern.matcher(line_1);
				if (matcher.find()) {
					String nodeId = matcher.group(1);
					String label = matcher.group(2).replace("/", "_");
					nodeIdToLabel.put(nodeId, label);

					if (line_1.contains("style=filled") && line_1.contains("color=green")) {
						startStateLabel = label;
					}
				}
			}

			Map<String, String> labelToSimpleId = new LinkedHashMap<>();
			int counter = 0;
			for (String label : nodeIdToLabel.values()) {
				labelToSimpleId.put(label, "s" + counter);
				counter++;
			}

			Pattern edgePattern = Pattern
					.compile("\\s*(state\\d+)\\s*->\\s*(state\\d+)\\s*\\[\\s*label\\s*=\\s*\"([^\"]+)\"\\s*\\];?");

			Map<String, List<Transition>> transitionsByState = new HashMap<>();

			for (String line_1 : lines) {
				if (!line_1.contains("->")) {
					continue;
				}

				Matcher matcher = edgePattern.matcher(line_1);
				if (matcher.find()) {
					String sourceNode = matcher.group(1);
					String targetNode = matcher.group(2);

					String label = matcher.group(3).trim();

					String action;
					String fexpression = null;

					int lastSlashIndex = label.lastIndexOf('/');

					if (lastSlashIndex != -1) {
						action = label.substring(0, lastSlashIndex).trim().replace("/", "_");
						fexpression = label.substring(lastSlashIndex + 1).trim();
					} else {
						action = label.replace("/", "_");
					}

					String sourceLabel = nodeIdToLabel.get(sourceNode);
					String targetLabel = nodeIdToLabel.get(targetNode);

					if (sourceLabel == null || targetLabel == null) {
						continue;
					}

					transitionsByState.computeIfAbsent(sourceLabel, k -> new ArrayList<>())
							.add(new Transition(targetLabel, action, fexpression));
				}
			}

			List<String> allStates = new ArrayList<>(nodeIdToLabel.values());

			StringBuilder sb = new StringBuilder();

			for (String sourceState : allStates) {
				String sourceSimpleId = labelToSimpleId.get(sourceState);

				List<Transition> transitions = transitionsByState.get(sourceState);

				if (transitions != null) {
					for (Transition t : transitions) {
						String targetSimpleId = labelToSimpleId.get(t.target);

						String output = sourceState.equals(t.target) ? "1" : "0";

						String fexprDisplay;
						if (t.fexpression != null && t.fexpression.equals("true")) {
							fexprDisplay = ""; // empty if "true"
						} else if (t.fexpression != null) {
							fexprDisplay = t.fexpression; // keep as is otherwise
						} else {
							fexprDisplay = ""; // also empty if null
						}

						String line_1 = String.format("%s@[()] -- %s@[((%s))]/%s -> %s@[()]", sourceSimpleId, t.action,
								fexprDisplay, output, targetSimpleId);

						sb.append(line_1).append(System.lineSeparator());
					}
				}
			}

			String result = sb.toString();

			Files.write(Path.of(out_dir.getAbsolutePath(), "claroline_fts_1.txt"), result.getBytes(StandardCharsets.UTF_8));

			System.out.println("Finished.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static class Transition {
		String target;
		String action;
		String fexpression;

		Transition(String target, String action, String fexpression) {
			this.target = target;
			this.action = action;
			this.fexpression = fexpression;
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FTS, true, "The FTS file");
		options.addOption(DIR, true, "Set output directory");
		return options;
	}
}
