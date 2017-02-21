package submit;

import java.util.List;
import joeq.Class.jq_Class;
import joeq.Main.Helper;
import flow.Flow;
import hw2.MySolver;

public class Optimize {
    /*
     * optimizeFiles is a list of names of class that should be optimized
     * if nullCheckOnly is true, disable all optimizations except "remove redundant NULL_CHECKs."
     */
    public static void optimize(List<String> optimizeFiles, boolean nullCheckOnly) {
    	Flow.Solver solver = new MySolver();
    	Flow.Analysis nc = new NullCheckOptimizer();
    	Flow.Analysis bc = new BoundCheckOptimizer();
    	
        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));
            // Run your optimization on each classes.
            solver.registerAnalysis(nc);
            Helper.runPass(classes, solver);
            if (!nullCheckOnly) {
                solver.registerAnalysis(bc);
                Helper.runPass(classes,solver);
            }
        }
    }
}
