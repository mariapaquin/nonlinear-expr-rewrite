package reachingDef.Constraint.Term;

import reachingDef.Solving.DefinitionSet;

import java.util.Set;

/**
 * Represents a node in the constraint graph.
 *
 */
public abstract class ConstraintTerm {

    public DefinitionSet definitionSet;

    public interface TermProcessor {
        void processTerm(ConstraintTerm term);
    }

    public abstract DefinitionSet getDefinitionSet();

    public abstract void updateDefinitionSet(DefinitionSet ds2);

    public abstract void setDefinitionSet(Set<String> variables);

    public void processTerms(TermProcessor processor) {
        processor.processTerm(this);
    }
}
