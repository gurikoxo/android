/**
 * Test application for the GITL parser.
 *
 * @author Orhan Aksoy
 */

import java.io.*;
import java.util.*;

public class gitlInterp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        GITLParser parser = new GITLParser();
        GITLContext context = new GITLContext();
       
        if ( ( args.length == 0 ) ||
             ( args[0].lastIndexOf('.')  <=0 ) ||
             (! args[0].substring(1 + args[0].lastIndexOf('.')).equalsIgnoreCase("git") ) ) {

            System.out.println("Invalid filename");
            //        Test1_Keywords(parser, context);
            //        Test2_Conditional(parser, context);
            //        Test3_ReadConsole(parser, context);
            //       Test4_Arithmetic(parser, context);
            //       Test5_Error(parser, context);
            System.exit(1);
       }

        try {
            System.out.println("Interpreting " + args[0]);
            BufferedReader inp = new BufferedReader ( new FileReader(args[0]));
            String line = null;

            LinkedList<String> program = new LinkedList<String>();

            while ((line = inp.readLine()) != null ) {
                program.addLast(line);
            }
            parser.parse(context, program);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        

    }

    static void Test5_Error(GITLParser parser, GITLContext context) {
        LinkedList<String> lines = new LinkedList<String>();
        lines.addLast("int main int p");
        lines.addLast("int c");
        lines.addLast("int c");
        lines.addLast("d = 3");
        lines.addLast("d = 3 + 2 *");
        lines.addLast("call");
        lines.addLast("end");
        parser.parse(context, lines);
   }
    
    static void Test4_Arithmetic(GITLParser parser, GITLContext context) {
        LinkedList<String> lines = new LinkedList<String>();
        lines.addLast("int main int p");
        lines.addLast("int c");
        lines.addLast("int b");
        lines.addLast("b = 3");
        lines.addLast("c = b + 5 * 8 + 2 * 2");
        lines.addLast("call print c res");
        lines.addLast("c = b * 5 + 8 * 2 + 1");
        lines.addLast("call print c res");
        lines.addLast("c = b * ( 5 + 8 ) + ( 2 + 1 ) ");
        lines.addLast("call print c res");
        lines.addLast("end");
        parser.parse(context, lines);
    }

    static void Test1_Keywords(GITLParser parser, GITLContext context) {
        LinkedList<String> lines = new LinkedList<String>();
        lines.addLast("int main int p");
        lines.addLast("int c");
        lines.addLast("int a");
        lines.addLast("a = 3 + 2 * 5");
        lines.addLast("// This is a comment");
        lines.addLast(" c = a + 2");
        lines.addLast("call print c result ");
        lines.addLast("if c thelabel");
        lines.addLast("label thelabel");
        lines.addLast("return orhan");
        lines.addLast("end");
        parser.parse(context, lines);
    }

    static void Test2_Conditional(GITLParser parser, GITLContext context) {
        LinkedList<String> lines = new LinkedList<String>();
         lines.addLast("int main int p");
       lines.addLast("int aa");
        lines.addLast("aa = 1");
        lines.addLast("if aa lab");
        lines.addLast("call print aa 0");
        lines.addLast("label lab");
        lines.addLast("aa = 222");
        lines.addLast("call print aa 0");
        lines.addLast("end");
        parser.parse(context, lines);


    }
   static void Test3_ReadConsole(GITLParser parser, GITLContext context) {
        LinkedList<String> lines = new LinkedList<String>();
        lines.addLast("int main int p4");
        lines.addLast("int aa");
        lines.addLast("call scan 0 aa");
        lines.addLast("call print aa 0");
        lines.addLast("return aa");
        lines.addLast("end");
        parser.parse(context, lines);


    }

}
