package reachingDef.Constraint.Term;

import org.eclipse.jdt.core.dom.ASTNode;
import reachingDef.Solving.DefinitionSet;

import java.util.Set;

/**
 * A node in the Constraint graph.
 *
 *
 */
public class NodeLabel extends ConstraintTerm {
	protected ASTNode node;

	public NodeLabel(ASTNode node) {
		this.node = node;
	}

	public void setDefinitionSet(Set<String> variables){
		definitionSet = new DefinitionSet(variables);
	}

	public void updateDefinitionSet(DefinitionSet ds2) {
		definitionSet = ds2;
	}

	public DefinitionSet getDefinitionSet() {
		return definitionSet;
	}

}
