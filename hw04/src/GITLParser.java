/**
 * Parser class for GITL language.
 *
 * @author Orhan Aksoy
 */

import java.util.*;

public class GITLParser {
  /** The list of expression objects stored during expression registratino */
  private GITLExpressions.GITLExpressionBase [] expressions;

  public enum RunState { RUN_JUMP, RUN_NORMAL };
  /**
   * creates the expression objects.
   */
  public GITLParser() {
      createExpressions();
  }

  public void startFunction(String functionName, GITLContext ctx) {
      ctx.jump(functionName);
      ctx.setActiveFunction(functionName);
  }

    /**
     * Parses a program within a context.
     * @param ctx The context in which the program will run
     * @param program The list of strings of program lines.
     */
    public void parse(GITLContext ctx, LinkedList<String> program)  {

        try {
            // Set the program counter to the first line.
            ctx.currentLine = 0;
            startFunction("main", ctx);
 
            while (ctx.currentLine < program.size()) {
                // Parse the program line string at the currentline location.
                parseLine(program.get(ctx.currentLine), ctx);
                // If the exit status was set during parsing, exit.
                if (ctx.exitStatus) {
                    System.out.println("Program returned with value " + ctx.returnValue);
                    return;
                }
                // Proceed to the next line in the program.
                ++ctx.currentLine;
            }
            if (!ctx.getJumpLabel().isEmpty()) {
                throw new GITLException("Label/Function not found: " + ctx.getJumpLabel());
            }
            if (!ctx.getActiveFunction().isEmpty()) {
                throw new GITLException("Function " + ctx.getActiveFunction() + " not terminated");
            }
        } catch (GITLException e) {
            System.out.println("Error at line " + (ctx.currentLine + 1) + " : " + e.getMessage());
        }




     }

    /**
     * Parses a single code line within a context
     * @param line The code line
     * @param ctx The context in which the line will run
     */
    private void parseLine(String line, GITLContext ctx) throws GITLException {

        // The code lines will be split using space character (' ').
        StringTokenizer tok = new StringTokenizer(line, " ");

        // Check if it is an empty line
        if ( tok.countTokens() == 0 ) {
            return;
        }

        LinkedList<String> tokens = new LinkedList<String>();

        // Put all of the tokens in a linked list
        while (tok.hasMoreTokens()) {
            tokens.add(tok.nextToken());
        }

        // Check for every possible expression for a match with the current
        // line.
        for (GITLExpressions.GITLExpressionBase exp : expressions) {
            if (exp.checkMatch(tokens)) {
                // Set the call parameters in the current context.
                ctx.setParams(tokens);

                // Check if we're in normal run mode, or we're looking\
                // for a label now.

                if ( ctx.getRunState() == RunState.RUN_JUMP) {
                    exp.handleJump(ctx);
                    return;
                }

                // Interpret.
                exp.evaluate(ctx);
                return;
            }
        }

     // We could not match.
       throw new GITLException("Invalid statement");
   }

    
    /**
     * Creates the expression objects that the language interprets.
     */
    private void createExpressions() {

        expressions = new GITLExpressions.GITLExpressionBase[9];

        expressions[0] = new GITLExpressions.GITLComment(this);
        expressions[1] = new GITLExpressions.GITLVariable(this);
        expressions[2] = new GITLExpressions.GITLAssignment(this);
        expressions[3] = new GITLExpressions.GITLCall(this);
        expressions[4] = new GITLExpressions.GITLConditional(this);
        expressions[5] = new GITLExpressions.GITLLabel(this);
        expressions[6] = new GITLExpressions.GITLReturn(this);
        expressions[7] = new GITLExpressions.GITLFunctionDefinition(this);
        expressions[8] = new GITLExpressions.GITLFunctionTerm(this);
    }

    

}
