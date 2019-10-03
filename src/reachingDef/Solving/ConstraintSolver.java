package reachingDef.Solving;


import reachingDef.Constraint.Constraint;
import reachingDef.Constraint.Term.ConstraintTerm;
import reachingDef.Constraint.Term.DefinitionLiteral;
import reachingDef.Constraint.Term.EntryLabel;
import reachingDef.Constraint.Term.SetDifference;

import java.util.*;

public class ConstraintSolver {

    private boolean change;
    private ConstraintGraph graph;
    private ArrayList<Constraint> constraints;
    private Set<String> variables;

    public ConstraintSolver(ArrayList<Constraint> constraints, Set<String> variables) {
        this.constraints = constraints;
        this.variables = variables;
    }

    public void buildConstraintGraph() {
        graph = new ConstraintGraph(constraints);
        graph.initialize();
    }

    public void initializeDefinitionSet() {
        for (ConstraintTerm term : graph.getAllTerms()) {
            term.setDefinitionSet(variables);
        }
    }

    public ArrayList<EntryLabel> processWorkList() {
        List<ConstraintTerm> workList = graph.getAllTerms();
        int iteration = 0;
        change = true;
        while (change) {
            change = false;
            System.out.println("Starting iteration " + ++iteration
                    + "\n--------------------");
            for (int j = 0; j < workList.size(); j++) {
                ConstraintTerm t = workList.get(j);
                System.out.println("Constraint.Term:" + t);
                int h = 0;
                for (Constraint c : graph.getConstraintsInvolving(t)) {
                    System.out.println(++h + ": checking Constraint " + c + "...");
                    satisfyConstraint(c);
                }
                System.out.println("\n");
            }
        }

        ArrayList<EntryLabel> entryLabels = new ArrayList<>();

        for (int j = 0; j < workList.size(); j++) {
            ConstraintTerm t = workList.get(j);
            if (t instanceof EntryLabel) {
                entryLabels.add((EntryLabel) t);
                System.out.println(t + "\n--------------\n" + t.getDefinitionSet());
                System.out.println();
            }

            if (t instanceof SetDifference) {
                ConstraintTerm entry = ((SetDifference) t).getEntryTerm();
                entryLabels.add((EntryLabel) entry);
                System.out.println(entry + "\n--------------\n" + entry.getDefinitionSet());
                System.out.println();
          }
        }

        return entryLabels;
    }

    private void satisfyConstraint(Constraint constraint) {
        ConstraintTerm lhs = constraint.getLhs();
        ConstraintTerm rhs = constraint.getRhs();

        DefinitionSet lhsEst = lhs.getDefinitionSet();
        DefinitionSet rhsEst = rhs.getDefinitionSet();

        if (!rhsEst.containsAll(lhsEst)) {
            System.out.println(lhs.getDefinitionSet() + " is not in " + rhs.getDefinitionSet());
            System.out.println("Performing union operation...");

            // copy the previous definition set for change detection
            HashMap<String, List<DefinitionLiteral>> prev = new HashMap<>();
            for (String var : rhsEst.getVariables()) {
                prev.put(var, rhsEst.get(var));
            }

            DefinitionSet union = rhsEst.unionWith(lhsEst);

            rhs.updateDefinitionSet(union);

            if (changed(prev, rhs.getDefinitionSet().getVarMap())) {
                System.out.println("set was changed");
                change = true;
            }
            System.out.println(lhs.getDefinitionSet() + " is now in " + rhs.getDefinitionSet());

        } else {
            System.out.println(lhsEst + " is in " + rhsEst);
            System.out.println("Constraint already satisfied.");
        }
    }

    private boolean changed(HashMap<String, List<DefinitionLiteral>> prevMap, HashMap<String, List<DefinitionLiteral>> newMap) {

        for (String var : newMap.keySet()) {
            List<DefinitionLiteral> l1 = prevMap.get(var);
            List<DefinitionLiteral> l2 = newMap.get(var);

            if (l1 == null || l2 == null) {
                return true;
            }
            for (DefinitionLiteral def : l2) {
                if (!l1.contains(def)) {
                    return true;
                }
            }

            for (DefinitionLiteral def : l1) {
                if (!l2.contains(def)) {
                    return true;
                }
            }
        }

        return false;
    }

}
