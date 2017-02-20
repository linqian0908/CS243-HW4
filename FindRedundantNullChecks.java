package submit;

import joeq.Class.jq_Class;
import joeq.Main.Helper;
import flow.Flow;
import hw2.MySolver;

public class FindRedundantNullChecks {
    /*
     * args is an array of class names
     * method should print out a list of quad ids of redundant null checks
     * for each function as described on the course webpage
     */
    public static void main(String[] args) {
    
        Flow.Solver solver = new MySolver();
        Flow.Analysis analysis = new RedundantNullCheck();
        solver.registerAnalysis(analysis);
        
        jq_Class[] classes = new jq_Class[args.length];
        for (int i=0; i < classes.length; i++)
        classes[i] = (jq_Class)Helper.load(args[i]);

        // visit each of the specified classes with the solver.
        for (int i=0; i < classes.length; i++) {
            Helper.runPass(classes[i], solver);
        }
        
    }
}
