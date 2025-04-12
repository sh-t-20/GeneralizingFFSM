package ir.ac.ut.fml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.ConditionalState;
import uk.le.ac.ffsm.ConditionalTransition;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.SimplifiedTransition;

public class StateMachineUtils {

	static CompactMealy<String, Word<String>> removeSelfLoopsFSM(CompactMealy<String, Word<String>> fsm) {
		Collection<Integer> states_1 = fsm.getStates();
		Alphabet<String> inputs_1 = fsm.getInputAlphabet();
		for (Integer s : states_1) {
			for (String i : inputs_1) {
				@Nullable
				CompactMealyTransition<Word<String>> transition_1 = fsm.getTransition(s, i);
				if (s == transition_1.getSuccId()) {
					fsm.removeTransition(s, i, transition_1);
				}
			}
		}
		System.out.println("Self loops removed.");
		return fsm;
	}

	static FeaturedMealy<String, Word<String>> removeSelfLoopsFFSM(FeaturedMealy<String, Word<String>> ffsm) {
		Collection<ConditionalState<ConditionalTransition<String, Word<String>>>> states_1 = ffsm.getStates();
		Alphabet<String> inputs_1 = ffsm.getInputAlphabet();
		for (ConditionalState<ConditionalTransition<String, Word<String>>> s : states_1) {
			for (String i : inputs_1) {
				Collection<ConditionalTransition<String, Word<String>>> transitions_1 = ffsm.getTransitions(s, i);
				for (ConditionalTransition<String, Word<String>> t : transitions_1) {
					if (t.getPredecessor() == t.getSuccessor()) {
						ffsm.removeTransition(s, i, t);
					}
				}
			}
		}
		System.out.println("Self loops removed.");
		return ffsm;
	}

	public static FeaturedMealy<String, Word<String>> removeConditions(FeaturedMealy<String, Word<String>> ffsm) {
		// TODO Auto-generated method stub
		Collection<ConditionalState<ConditionalTransition<String, Word<String>>>> states_1 = ffsm.getStates();
		Alphabet<String> inputs_1 = ffsm.getInputAlphabet();
		for (ConditionalState<ConditionalTransition<String, Word<String>>> s : states_1) {
			s.setCondition(null);
			for (String i : inputs_1) {
				Collection<ConditionalTransition<String, Word<String>>> transitions_1 = ffsm.getTransitions(s, i);
				for (ConditionalTransition<String, Word<String>> t : transitions_1) {
					t.setCondition(null);
				}
			}
		}
		System.out.println("Conditions removed.");
		return ffsm;
	}
}
