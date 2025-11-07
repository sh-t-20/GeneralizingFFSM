package ir.ac.ut.fml;

import java.util.Objects;

import net.automatalib.words.Word;

public class TransitionLabel {
	private String input;
	private Word<String> output;

	public TransitionLabel(String i_1, Word<String> o_1) {
		// TODO Auto-generated constructor stub
		this.input = i_1;
		this.output = o_1;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public Word<String> getOutput() {
		return output;
	}

	public void setOutput(Word<String> output) {
		this.output = output;
	}

	public void printTransition() {
		System.out.println("input:" + this.getInput() + ", output:" + this.getOutput());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TransitionLabel))
			return false;

		TransitionLabel t = (TransitionLabel) o;
		return Objects.equals(this.input, t.input) && Objects.equals(this.output, t.output);
	}

	@Override
	public int hashCode() {
		return Objects.hash(input, output);
	}

}
