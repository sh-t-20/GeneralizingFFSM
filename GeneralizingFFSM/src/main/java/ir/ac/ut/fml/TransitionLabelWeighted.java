package ir.ac.ut.fml;

import net.automatalib.words.Word;

public class TransitionLabelWeighted {

	private String input;
	private Word<String> output;
	private float weight;

	public TransitionLabelWeighted(String i_1, Word<String> o_1, float w_1) {
		// TODO Auto-generated constructor stub
		this.input = i_1;
		this.output = o_1;
		this.weight = w_1;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
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

	public boolean equals(TransitionLabel t) {
		if (this.input.equals(t.getInput()) && this.output.equals(t.getOutput())) {
			return true;
		} else
			return false;
	}

	public void printTransition() {
		System.out.println("input:" + this.getInput() + ", output:" + this.getOutput());
	}

}
