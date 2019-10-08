package exprFinder.visitor;

import exprFinder.expr.KillSet;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import javax.xml.bind.SchemaOutputResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplaceExpressionsVisitor extends ASTVisitor {
    private HashMap<String, Integer> exprToVarmap;
    private AST ast;
    private ASTRewrite rewriter;


    public ReplaceExpressionsVisitor(HashMap<String, Integer> exprToVarmap,
                              ASTRewrite rewriter, AST ast) {
        this.exprToVarmap = exprToVarmap;
        this.rewriter = rewriter;
        this.ast = ast;
    }

    @Override
    public void endVisit(InfixExpression node) {
        Integer symbVarNum = exprToVarmap.get(node.toString());
        if (symbVarNum == null) {
            return;
        }

        String name = "x" + symbVarNum;
        SimpleName exprSymbVar = ast.newSimpleName(name);

        System.out.println("replacing " + node + " with " + exprSymbVar.getIdentifier());
        rewriter.replace(node, exprSymbVar, null);
    }

}
