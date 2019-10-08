import exprFinder.expr.ExpressionLiteral;
import exprFinder.expr.KillSet;
import exprFinder.visitor.AEVisitor;
import exprFinder.visitor.ExpressionVisitor;
import exprFinder.visitor.ReplaceExpressionVisitor;
import exprFinder.visitor.InitializeReAssignVisitor;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import reachingDef.Constraint.Constraint;
import reachingDef.Constraint.Term.DefinitionLiteral;
import reachingDef.Constraint.Term.EntryLabel;
import reachingDef.ConstraintCreator.ConstraintTermFactory;
import reachingDef.Solving.ConstraintSolver;
import reachingDef.visitor.ConstraintVisitor;
import reachingDef.visitor.NodeLabelExprVisitor;

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
    private List<ASTNode> reachingDef;
    private List<String> symbVars;

    public static void main(String[] args) throws IOException {
        String source = "./tests/PostfixExpr.java";
        File file = new File(source);

        Driver driver = new Driver();

        driver.setupAST(file);
        driver.initializeReassignSymbVar();
        driver.applyEdits(file);

        driver.setupAST(file);
        driver.findReachingDefinitions();
        driver.removeNonreachingDefinitions();

        driver.replaceExpressions();
        driver.applyEdits(file);
    }

    private void removeNonreachingDefinitions() {
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(Assignment node) {

                // only need to worry about removing re-assignments of
                // the symbolic variables we declared
                if(!symbVars.contains(node.getLeftHandSide().toString())){
                    return false;
                }

                ASTNode parent = node.getParent();

                if (!reachingDef.contains(parent)) {
                    rewriter.remove(parent, null);
                }
                return true;
            }
        });
    }

    private void applyEdits(File file) throws IOException {
        Document document = new Document(source);
        TextEdit edits = rewriter.rewriteAST(document, null);
        try {
            edits.apply(document);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        System.out.println(document.get());

        BufferedWriter out = new BufferedWriter(new FileWriter(file));
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

        solver.processWorkList();

        // pick out only the labels that contain variable infix expressions

        ConstraintTermFactory variableFactory = visitor.getVariableFactory();

        NodeLabelExprVisitor nodeLabelExprVisitor = new NodeLabelExprVisitor(variableFactory);
        cu.accept(nodeLabelExprVisitor);

        // we only care about the reaching definitions of statements that contain
        // nonlinear variable expressions
        List<EntryLabel> entryLabels = nodeLabelExprVisitor.getEntryLablesWithExpr();

        for (EntryLabel entry : entryLabels) {
            System.out.println(entry);
            Set<String> prgmVars = entry.reachingDefSet.getVariables();
            for (String var: prgmVars) {
                for (DefinitionLiteral d : entry.reachingDefSet.get(var)) {
                    if (!reachingDef.contains(d.getNode())) {
                        reachingDef.add(d.getNode());
                    }
                }
            }
        }
    }

    private void initializeReassignSymbVar() {
        List<TypeDeclaration> types = cu.types();

        for (TypeDeclaration type : types) {

            for (MethodDeclaration methodDeclaration : type.getMethods()) {

                // get a list of nonlinear variable expressions, each is an ExpressionLiteral object
                // map each to a new symbolic variable (string), ie what it will be replaced with
                ExpressionVisitor exprVisitor = new ExpressionVisitor();
                methodDeclaration.accept(exprVisitor);

                List<ExpressionLiteral> exprList = exprVisitor.getNonlinearVarExpr();
                HashMap<String, Integer> exprToVarMap = exprVisitor.getExprMap();

                // map each applicable node (assignment, var dec, postfix expr, etc) to the expression it kills
                AEVisitor aeVisitor = new AEVisitor(exprList);
                methodDeclaration.accept(aeVisitor);
                HashMap<ASTNode, KillSet> killMap = aeVisitor.getKillMap();

                // (1) initialize each symbolic variable at the beginning of the method
                // (2) Get the expressions killed for each statement. Re-assign the symbolic
                // variable associated with each expression in its kill set.
                InitializeReAssignVisitor visitor = new InitializeReAssignVisitor(exprToVarMap,
                        killMap, rewriter, ast);
                methodDeclaration.accept(visitor);

                symbVars = visitor.getSymbVars();
            }
        }
    }

    private void replaceExpressions() {
        List<TypeDeclaration> types = cu.types();

        for (TypeDeclaration type : types) {

            for (MethodDeclaration methodDeclaration : type.getMethods()) {

                // get a list of nonlinear variable expressions, each is an ExpressionLiteral object
                // map each to a new symbolic variable (string), ie what it will be replaced with
                ExpressionVisitor exprVisitor = new ExpressionVisitor();
                methodDeclaration.accept(exprVisitor);

                HashMap<String, Integer> exprToVarMap = exprVisitor.getExprMap();

                // replace each infix expression with its corresponding symbolic variable
                ReplaceExpressionVisitor visitor = new ReplaceExpressionVisitor(exprToVarMap, rewriter, ast);
                methodDeclaration.accept(visitor);
            }
        }
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
