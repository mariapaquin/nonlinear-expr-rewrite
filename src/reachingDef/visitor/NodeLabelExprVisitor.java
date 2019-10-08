package reachingDef.visitor;

import org.eclipse.jdt.core.dom.*;
import reachingDef.Constraint.Term.ConstraintTerm;
import reachingDef.Constraint.Term.EntryLabel;
import reachingDef.Constraint.Term.SetDifference;
import reachingDef.ConstraintCreator.ConstraintTermFactory;


import java.util.ArrayList;
import java.util.List;

/**
 * Find entry labels for statements that contain
 * nonlinear variable infix expressions.
 */
public class NodeLabelExprVisitor extends ASTVisitor {
    public List<EntryLabel> getEntryLablesWithExpr() {
        return entryLablesWithExpr;
    }

    private List<EntryLabel> entryLablesWithExpr;
    private ConstraintTerm currStmt;
    private ConstraintTermFactory variableFactory;

    public NodeLabelExprVisitor(ConstraintTermFactory variableFactory) {
        entryLablesWithExpr = new ArrayList<>();
        this.variableFactory = variableFactory;
    }

    @Override
    public boolean visit(Assignment node) {
        ASTNode parent = node.getParent();
        currStmt = variableFactory.createEntryLabel(parent);
        return (currStmt != null);
    }

    @Override
    public boolean visit(DoStatement node) {
        currStmt = variableFactory.createEntryLabel(node);
        return (currStmt != null);
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        currStmt = variableFactory.createEntryLabel(node);
        return (currStmt != null);
    }

    @Override
    public boolean visit(ForStatement node) {
        currStmt = variableFactory.createEntryLabel(node);
        return (currStmt != null);
    }

    @Override
    public boolean visit(IfStatement node) {
        currStmt = variableFactory.createEntryLabel(node);
        return (currStmt != null);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        // TODO: check if parent is expression statement
        ASTNode parent = node.getParent();
        currStmt = variableFactory.createEntryLabel(parent);
        return (currStmt != null);

    }
    @Override
    public boolean visit(PostfixExpression node) {
        // TODO: check if parent is expression statement
        ASTNode parent = node.getParent();
        currStmt = variableFactory.createEntryLabel(parent);
        return (currStmt != null);
    }

    @Override
    public boolean visit(PrefixExpression node) {
        // TODO: check if parent is expression statement
        ASTNode parent = node.getParent();
        currStmt = variableFactory.createEntryLabel(parent);
        return (currStmt != null);
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        currStmt = variableFactory.createEntryLabel(node);
        return (currStmt != null);
    }

    @Override
    public boolean visit(WhileStatement node) {
        currStmt = variableFactory.createEntryLabel(node);
        return (currStmt != null);
    }



    @Override
    public boolean visit(InfixExpression node) {
        Expression lhs = node.getLeftOperand();
        Expression rhs = node.getRightOperand();
        InfixExpression.Operator op = node.getOperator();

        if (!(lhs instanceof SimpleName) || !(rhs instanceof SimpleName)) {
            return true;
        }

        if((op != InfixExpression.Operator.TIMES) &&
                (op != InfixExpression.Operator.DIVIDE) &&
                (op != InfixExpression.Operator.REMAINDER)){
            return true;
        }

        if (currStmt instanceof EntryLabel) {
            entryLablesWithExpr.add((EntryLabel) currStmt);
        } else if (currStmt instanceof SetDifference) {
            ConstraintTerm entry = ((SetDifference) currStmt).getEntryTerm();
            entryLablesWithExpr.add((EntryLabel) entry);
        }

        return true;
    }

    private List<String> getVarsUsed(InfixExpression node) {
        List<String> vars = new ArrayList<>();
        ASTVisitor visitor= new ASTVisitor() {
            @Override
            public boolean visit(SimpleName name) {
                vars.add(name.getIdentifier());
                return false;
            }
        };
        node.accept(visitor);
        return vars;
    }
}
