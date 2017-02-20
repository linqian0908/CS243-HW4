package submit;

import java.util.List;
import joeq.Class.jq_Class;
import joeq.Main.Helper;

public class Optimize {
    /*
     * optimizeFiles is a list of names of class that should be optimized
     * if nullCheckOnly is true, disable all optimizations except "remove redundant NULL_CHECKs."
     */
    public static void optimize(List<String> optimizeFiles, boolean nullCheckOnly) {
    	Flow.Solver solver = new MySolver();
    	Flow.Analysis nullChecker = new RedundantNullCheck();
    	Flow.Analysis rd = new ReachingDefinition();]
    	
        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));
            // Run your optimization on each classes.
            solver.registerAnalysis(nullChecker);
			Helper.runPass(classes[i], solver);
			if (!nullCheckOnly) {
				solver.registerAnalysis(rd);
				Helper.runPass(classes[i],solver);
			}
        }
    }
}
