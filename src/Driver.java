import exprFinder.expr.ExpressionLiteral;
import exprFinder.expr.KillSet;
import exprFinder.visitor.AEVisitor;
import exprFinder.visitor.ExpressionVisitor;
import exprFinder.visitor.RewriteExprVisitor;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import reachingDef.Constraint.Constraint;
import reachingDef.Constraint.Term.DefinitionLiteral;
import reachingDef.Constraint.Term.EntryLabel;
import reachingDef.Solving.ConstraintSolver;
import reachingDef.visitor.ConstraintVisitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Driver {
    private AST ast;
    private ASTRewrite rewriter;
    private CompilationUnit cu;
    private String source;
    private List<ASTNode> symbVarDec;
    private List<ASTNode> reachingDef;

    public static void main(String[] args) throws IOException {

        File file = new File("./tests/If.java");

        Driver driver = new Driver();

        driver.setupAST(file);
        driver.replaceExpressions();
        driver.applyEdits();

        driver.setupAST(file);
        driver.findReachingDefinitions();
        driver.removeNonreachingDefinitions();
        driver.applyEdits();
    }

    private void removeNonreachingDefinitions() {
        System.out.println(reachingDef);
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(Assignment node) {
                System.out.println(node);
                if (reachingDef.contains(node)) {

                    System.out.println("definition " + node + " is reaching");
                }
                return true;
            }
        });

/*        for (ASTNode node : symbVarDec) {
            if (!reachingDef.contains(node)) {
                System.out.println(node + " is not reaching");
                //rewriter.remove(node, null);
            }
        }*/
    }

    private void applyEdits() throws IOException {
        Document document = new Document(source);
        TextEdit edits = rewriter.rewriteAST(document, null);
        try {
            edits.apply(document);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        System.out.println(document.get());

        BufferedWriter out = new BufferedWriter(new FileWriter(new File("./tests/If.java")));
        out.write(document.get());
        out.flush();
        out.close();
    }

    private void findReachingDefinitions() {
        reachingDef = new ArrayList<>();
        ConstraintVisitor visitor = new ConstraintVisitor();
        cu.accept(visitor);

        Set<String> variables = new HashSet<String>();

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName node) {
                if (node.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
                        node.getLocationInParent() == MethodDeclaration.NAME_PROPERTY ||
                        node.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY ||
                        node.getLocationInParent() == QualifiedName.NAME_PROPERTY ||
                        node.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY ||
                        node.getLocationInParent() == PackageDeclaration.NAME_PROPERTY ||
                        node.getLocationInParent() == SimpleType.NAME_PROPERTY ||
                        node.getLocationInParent() == ImportDeclaration.NAME_PROPERTY ||
                        node.getLocationInParent() == TypeParameter.NAME_PROPERTY) {
                    return true;
                }
                variables.add(node.getIdentifier());
                return true;
            }
        });

        ArrayList<Constraint> constraints = visitor.getConstraints();

        ConstraintSolver solver = new ConstraintSolver(constraints, variables);

        solver.buildConstraintGraph();

        solver.initializeDefinitionSet();

//        List<EntryLabel> entryLabels = solver.processWorkList();
//
//        for (EntryLabel entry : entryLabels) {
//            for (String var: entry.definitionSet.getVariables()) {
//                for (DefinitionLiteral d : entry.definitionSet.get(var)) {
//                    if (!reachingDef.contains(d.getNode())) {
//                        reachingDef.add(d.getNode());
//                    }
//                }
//            }
//        }
    }

    private void replaceExpressions() {
        RewriteExprVisitor rewriteVisitor = null;
        List<TypeDeclaration> types = cu.types();

        for (TypeDeclaration type : types) {

            for (MethodDeclaration methodDeclaration : type.getMethods()) {
                ExpressionVisitor exprVisitor = new ExpressionVisitor();
                methodDeclaration.accept(exprVisitor);
                List<ExpressionLiteral> exprList = exprVisitor.getNonlinearVarExpr();
                HashMap<String, Integer> exprToVarMap = exprVisitor.getExprMap();
                int varCount = exprVisitor.getVarCount();

                AEVisitor aeVisitor = new AEVisitor(exprList);
                methodDeclaration.accept(aeVisitor);
                HashMap<ASTNode, KillSet> killMap = aeVisitor.getKillMap();

                rewriteVisitor = new RewriteExprVisitor(exprToVarMap,
                        killMap, rewriter, ast);
                methodDeclaration.accept(rewriteVisitor);
            }
        }
        symbVarDec = rewriteVisitor.getSymbVarDec();
        
    }

    private void setupAST(File file) throws IOException {
        source = new String(Files.readAllBytes(file.toPath()));
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        cu = (CompilationUnit) parser.createAST(null);
        ast = cu.getAST();

        rewriter = ASTRewrite.create(ast);
    }

}
