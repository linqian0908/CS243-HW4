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
    	Flow.Analysis nct = new NullCheckTightOptimizer();
    	Flow.Analysis bc = new BoundCheckOptimizer();
        Flow.Analysis lv = new LiveOptimizer();
	
        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));
            // Run your optimization on each classes.
            if (nullCheckOnly=true) {
                solver.registerAnalysis(nc);
                Helper.runPass(classes, solver);
            }
            else {
                solver.registerAnalysis(nct);
                Helper.runPass(classes, solver);
                solver.registerAnalysis(bc);
                Helper.runPass(classes,solver);
		solver.registerAnalysis(lv);
		Helper.runPass(classes,solver);
            }
        }
    }
}
