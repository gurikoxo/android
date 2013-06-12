/**
 * GITLContext class represents the context in which the expressions are
 * evaluated. Contains global variables, function call parameters, etc.
 * 
 * @author Orhan Aksoy - 09104302
 */
import java.util.*;

public class GITLContext {
    /** The list of parameters passed to each line */
   private LinkedList<String> paramList;
   /** The list of encountered labels */
   private LinkedList<LabelDef> labelList;
   /** The list of variables */
   private LinkedList<VariableDef> variableList;
   /** The list of functions */
   private LinkedList<FunctionDef> functionList;
   
   /** the label string that is being seeked
    * by the parser at the moment. When this variable is a nonempty string,
    * this means that the parser has just received a jump statement, but could
    * not find the label in its label list. This means that this label is further
    * down in the code listing. So we keep on parsing the program listing, without
    * actually processing them, until we find the corresponding label. When the
    * label is found, tis variable is set to an empty string again
    */
   private String jumpToLabel = "";

   /** The name of the function being interpreted at the moment */
   private String activeFunction = "";

   /** return value from the program */
   public int returnValue = -1;
   /** program counter */
   public int currentLine = -1;
   /** Set by the return statement to tell the compiler to stop parsing and exit.*/
   public boolean exitStatus = false;

   /**
    * Constructs the label and variable lists.
    */
   public GITLContext() {
       labelList = new LinkedList<LabelDef>();
       variableList = new LinkedList<VariableDef>();
   }

   /**
    * Adds a new label at the current line
    * to the current context
    * @param label name of the label.
    * @throws GITLException("Duplicate label")
    */
   public void addLabel(String label) throws GITLException {
      for (LabelDef l : labelList) {
          if (l.label.equals(label)) {
              throw new GITLException("Duplicate label:" + label);
          }
      }
      labelList.add(new LabelDef(currentLine, label));
   }

   /**
    * Returns the jump label that the parser is looking for at the moment.
    * If we're not looking for a label, this function returns "".
    * @return the jumpToLabel string.
    */
   public String getJumpLabel() {
       return jumpToLabel;
   }

   /**
    * Adds a new variable (integer) whose value is set to 0
    * to the current context.
    * @param variable name of the variable.
    * @throws GITLException("Duplicate variable")
    */
   public void addVariable(String variable) throws GITLException {
      for (VariableDef v : variableList) {
          if (v.variable.equals(variable)) {
              throw new GITLException("Duplicate variable:" + variable);
          }
      }
      variableList.add(new VariableDef(variable, 0));
   }

   /**
    * Returns the value of a variable.
    * @param variable variable name
    * @return value of the variable
    * @throws GITLException("Variable not found")
    */
   public int getVariableValue(String variable) throws GITLException {
       for (VariableDef v : variableList) {
           if ( v.variable.equals(variable)) {
               return v.value;
           }
       }
       throw new GITLException("Variable not found1: " + variable);

   }
   /**
    * Sets the value of a variable
    * @param variable The variable to be set
    * @param value The value
    * @throws GITLException("Variable not found")
    */
   public void setVariableValue(String variable, int value) throws GITLException {
       for (VariableDef v : variableList) {
           if ( v.variable.equals(variable)) {
               v.value = value;
               return;
           }
       }
       throw new GITLException("Variable not found2: " + variable);

   }
   /**
    * Stores the name of the function we're in.
    *
    * @param functionName
    */
   public void setActiveFunction(String functionName) {
       activeFunction = functionName;
   }

   /**
    * Retrieves the name of the active function.
    * @return the active function name
    */
   public String getActiveFunction() {
       return activeFunction;
   }
   /**
    * Sets the return flag to true and also stores
    * the return value
    * @param retVal Return value to be stored.
    */
   public void setReturn(int retVal) {
       exitStatus = true;
       returnValue = retVal;
   }

   /** Called during the 'if' statement execution if the variable is nonzero.
    * If the label to jump exists, it means that that we have processed a
    * label statement before with this name. So, we know its line number. In
    * this case, we set the current line number (the program counter).
    *
    * If the label is not found, the context variable 'jumpToLabel' is used.
    * When this variable is a nonempty string, the expression evaluators (other
    * than the label expression) do nothing. When the label expression with the
    * same name is found, we reset to normal interpretation mode again.
    *
    * @param label The label to jump to.
    */
   public void jump(String label) {
       for (LabelDef l : labelList) {
           if ( l.label.equals(label)) {
               currentLine = l.lineNum;
               jumpToLabel = "";
               return;
           }
       }
       // We haven't encountered this label until now
       // We store this label so that the parser will proceed until it finds
       // this label.

       jumpToLabel = label;
       
   }
   /**
    * returns RUN_JUMP if the jumpToLabel is a nonzero string. This means
    * that the interpreter is searching for a label, not executing.
    * @return run state (RUN_NORMAL or RUN_JUMP)
    */
   public GITLParser.RunState getRunState() {
       if (jumpToLabel.isEmpty()) {
           return GITLParser.RunState.RUN_NORMAL;
       }
       return GITLParser.RunState.RUN_JUMP;
   }

   /**
    * sets the parameters after the current key.
    * @param params the parameters.
    */
   public void setParams(LinkedList params) {
       paramList = params;
   }

   /**
    * returns the parameters after the current key.
    * @return the parameters after the current key.
    */
   public LinkedList<String> getParams() {
       return paramList;
   }

   /**
    * Adds a new function returning int with an int paramerer
    * to the current context at the current line.
    * 
    * @param functionName Name of the function
    * @param param Parameter into the function
    * @throws GITLException("Duplicate function definition")
    */
   public void addFunction(String functionName, LinkedList<String> paramList) throws GITLException {
      for (FunctionDef f : functionList) {
          if (f.name.equals(functionName)) {
              throw new GITLException("Duplicate function definition:" + functionName);
          }
      }
      functionList.add(new FunctionDef(currentLine, functionName, paramList));
      for (String param : paramList) {
          variableList.add(new VariableDef(param, 0));
      }
   }

    /**
    * Function definition structure. Contains the line number, name of
    * the function and parameter.
    */
    private static class FunctionDef {
        private int lineNum;
        private String name;
        private LinkedList<String> paramList;
        FunctionDef(int ln, String n, LinkedList<String> p) {
            lineNum = ln;
            name = n;
            paramList = p;
        }
    }

    /**
    * Label definition structure. Contains the line number and name of
    * the label.
    */
    private static class LabelDef {
        private int lineNum;
        private String label;
        LabelDef(int n, String l) {
            lineNum = n;
            label = l;
        }
    }
   /**
    * Variable definition structure. Contains the variable name and value of
    * the variable.
    */
    private static class VariableDef {
        private String variable;
        private int value;
        VariableDef(String name, int val) {
            variable = name;
            value = val;
        }
    }
}
