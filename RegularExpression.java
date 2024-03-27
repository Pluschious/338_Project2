import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author yaw
 */
public class RegularExpression {

    private String regularExpression;
    private NFA nfa;

    // You are not allowed to change the name of this class or this constructor at all.
    public RegularExpression(String regularExpression) {
        this.regularExpression = regularExpression.replaceAll("\\s+", "");
        nfa = generateNFA();
    }

    // TODO: Complete this method so that it returns the nfa resulting from unioning the two input nfas.
    private NFA union(NFA nfa1, NFA nfa2) {
        String[] states = new String[nfa1.getStates().length + nfa2.getStates().length + 1];
        System.arraycopy(nfa1.getStates(), 0, states, 0, nfa1.getStates().length);
        System.arraycopy(nfa2.getStates(), 0, states, nfa1.getStates().length, nfa2.getStates().length);
        states[states.length -1] = "SU"; //creates a new start state

        char[] alphabet = nfa1.getAlphabet();

        HashMap<String, HashMap<Character, HashSet<String>>> transitions = new HashMap<>(); 

        //Adds transitions from the new start state to the start state of nfa1 and nfa2
        HashMap<Character, HashSet<String>> transition = new HashMap<>();
        transition.put('e', new HashSet<>(Arrays.asList(nfa1.getStartState(), nfa2.getStartState())));
        transitions.put("SU", transition);

        //copies the transitions from nfa1 and nfa2
        for (String state : nfa1.getStates()) {
            transitions.put(state, nfa1.getTransitions().get(state));
        }

        for (String state : nfa2.getStates()) {
        transitions.put(state, nfa2.getTransitions().get(state));
        }

    return new NFA(states, alphabet, transitions, "SU", nfa1.getAcceptStates());

}
    

    // TODO: Complete this method so that it returns the nfa resulting from concatenating the two input nfas.
    private NFA concatenate(NFA nfa1, NFA nfa2) {
        String[] states = new String[nfa1.getStates().length + nfa2.getStates().length];
        System.arraycopy(nfa1.getStates(), 0, states, 0, nfa1.getStates().length);
        System.arraycopy(nfa2.getStates(), 0, states, nfa1.getStates().length, nfa2.getStates().length);
        //copies states, alphabet, and transitions from nfa1 and nfa2

        char[] alphabet = nfa1.getAlphabet();

        HashMap<String, HashMap<Character, HashSet<String>>> transitions = new HashMap<>(nfa1.getTransitions());

        //adds epsilon transition from the accept state of nfa1 to start state of nfa2
        for (String acceptState : nfa1.getAcceptStates()) {
            transitions.get(acceptState).computeIfAbsent('e', k -> new HashSet<>()).add(nfa2.getStartState());
        }

        //copies transitions from nfa2
        for (String state : nfa2.getStates()) {
            transitions.put(state, nfa2.getTransitions().get(state));
        }

        return new NFA(states, alphabet, transitions, nfa1.getStartState(), nfa2.getAcceptStates());
    }

    // TODO: Complete this method so that it returns the nfa resulting from "staring" the input nfa.
    private NFA star(NFA nfa) {
        String[] states = new String[nfa.getStates().length +1];
        System.arraycopy(nfa.getStates(),0, states, 0, nfa.getStates().length);
        states[states.length - 1] = "SS"; //new start state
        //Creates a new start state and connects to the start state of original nfa

        char[] alphabet = nfa.getAlphabet();

        HashMap<String, HashMap<Character, HashSet<String>>> transitions = new HashMap<>(nfa.getTransitions());

        //epsilon transition from new start state to origional start state and each accept state to original start state
        transitions.computeIfAbsent("SS", k -> new HashMap<>()).put('e', new HashSet<>(Arrays.asList(nfa.getStartState(), "SS")));
        for (String acceptState : nfa.getAcceptStates()) {
            transitions.get(acceptState).computeIfAbsent('e', k -> new HashSet<>()).add(nfa.getStartState());

        }

        return new NFA(states, alphabet, transitions, "SS", new String[]{"SS"});
    }

    // TODO: Complete this method so that it returns the nfa resulting from "plussing" the input nfa.
    private NFA plus(NFA nfa) {
        String[] states = new String[nfa.getStates().length + 1];
        System.arraycopy(nfa.getStates(), 0, states, 0, nfa.getStates().length);
        states[states.length -1] = "SP"; //new start state
        //creates new start state and connects to the start state of the original nfa

        char[] alphabet = nfa.getAlphabet();

        HashMap<String, HashMap<Character, HashSet<String>>> transitions = new HashMap<>(nfa.getTransitions());

        //epsilon transition from the new start state to original start state
        transitions.computeIfAbsent("SP", k -> new HashMap<>()).put('e', new HashSet<>(Arrays.asList(nfa.getStartState())));

        return new NFA(states, alphabet, transitions, "SP", nfa.getAcceptStates());
    }

    // TODO: Complete this method so that it returns the nfa that only accepts the character c.
    private NFA singleCharNFA(char c) {
        String[] states = new String[]{"A", "B"};
        char[] alphabet = new char[]{c};
        HashMap<String, HashMap<Character, HashSet<String>>> transitions = new HashMap<>();
        transitions.put("A", new HashMap<>());
        transitions.get("A").put(c, new HashSet<>(Arrays.asList("B")));
        transitions.put("B", new HashMap<>());

        return new NFA(states, alphabet, transitions, "A", new String[]{"B"});

    }

    // You are not allowed to change this method's header at all.
    public boolean test(String string) {
        return nfa.accepts(string);
    }

    // Parser. I strongly recommend you do not change any code below this line.
    // Do not change any of the characters recognized in the regex (e.g., U, *, +, 0, 1)
    private int position = -1, ch;

    public NFA generateNFA() {
        nextChar();
        return parseExpression();
    }

    public void nextChar() {
        ch = (++position < regularExpression.length()) ? regularExpression.charAt(position) : -1;
    }

    public boolean eat(int charToEat) {
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }

    public NFA parseExpression() {
        NFA nfa = parseTerm();
        while (true) {
            if (eat('U')) {
                // Create the nfa that is the union of the two passed nfas.
                nfa = union(nfa, parseTerm());
            } else {
                return nfa;
            }
        }
    }

    public NFA parseTerm() {
        NFA nfa = parseFactor();
        while (true) {
            // Concatenate NFAs.
            if (ch == '0' || ch == '1' || ch == '(') {
                // Create the nfa that is the concatentaion of the two passed nfas.
                nfa = concatenate(nfa, parseFactor());
            } else {
                return nfa;
            }
        }
    }

    public NFA parseFactor() {
        NFA nfa = null;
        if (eat('(')) {
            nfa = parseExpression();
            if (!eat(')')) {
               throw new RuntimeException("Missing ')'");
            }
        } else if (ch == '0' || ch == '1') {
            // Create the nfa that only accepts the character being passed (regularExpression.charAt(position) == '0' or '1').
            nfa = singleCharNFA(regularExpression.charAt(position));
            nextChar();
        }

        if (eat('*')) {
            // Create the nfa that is the star of the passed nfa.
            nfa = star(nfa);
        } else if (eat('+')) {
            // Create the nfa that is the plus of the passed nfa.
            nfa = plus(nfa);
        }

        return nfa;
    }
}
