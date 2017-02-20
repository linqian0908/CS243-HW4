package submit;

import joeq.Class.jq_Class;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operator.*;
import flow.Flow;

public class NullCheckOptimizer extends RedundantNullCheck {
    @override
    public void postprocess(ControlFlowGraph cfg) {
        QuadIterator qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            Quad q = qit.next();
            if (q.getOperator() instanceof Operator.NullCheck) {
                int id = q.getID();
                if (in[id].equals(out[id])) {
                    qit.remove();
                }
            }
        }
    }
}
