package reachingDef.Constraint.Term;

import reachingDef.Solving.DefinitionSet;

import java.util.Set;

/**
 * Subtracting definition from the entry term set
 */
public class SetDifference extends ConstraintTerm {

    private ConstraintTerm entryTerm;
    private DefinitionLiteral defWild;
    private String varKilled;

    public SetDifference(ConstraintTerm entryTerm, DefinitionLiteral defWild) {
        this.entryTerm = entryTerm;
        this.defWild = defWild;
        this.varKilled = defWild.getName();
    }

    public void setDefinitionSet(Set<String> variables){
        definitionSet = new DefinitionSet(variables);
        entryTerm.setDefinitionSet(variables);
    }

    public void updateDefinitionSet(DefinitionSet ds2) {
        entryTerm.updateDefinitionSet(ds2);

        DefinitionSet copy = new DefinitionSet(definitionSet.getVariables());

        for (String var : copy.getVariables()) {
            copy.put(var, ds2.get(var));
        }

        definitionSet = copy;

        definitionSet.killDefinitions(varKilled);

    }

    public DefinitionSet getDefinitionSet() {
        return definitionSet;
    }

    public ConstraintTerm getEntryTerm() {
        return entryTerm;
    }

    public DefinitionLiteral getDefWild() {
        return defWild;
    }

    @Override
    public String toString() {
        return entryTerm + "\\" + defWild;
    }

}

