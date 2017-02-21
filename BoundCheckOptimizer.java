package submit;

import joeq.Class.jq_Class;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import joeq.Compiler.Quad.Operator.*;
import flow.Flow;
import java.util.*;

public class BoundCheckOptimizer implements Flow.Analysis {
;
    public static class DefSet implements Flow.DataflowObject {
        private Set<String> set;
        public static Set<String> universalSet;
        /**
         * Methods from the Flow.DataflowObject interface.
         * See Flow.java for the meaning of these methods.
         * These need to be filled in.
         */
        public DefSet()
        {
            set =  new TreeSet<String>();
        }
        public void setToTop() 
        {
            set = new TreeSet<String>(universalSet);
        }
        public void setToBottom() 
        {
            set = new TreeSet<String>();
        }
        
        /**
         * Meet is a union
         */
        public void meetWith (Flow.DataflowObject o) 
        {
            DefSet t = (DefSet)o;
            this.set.retainAll(t.set);
        }
        
        public boolean contains(String s) {
            return this.set.contains(s);
        }
        
        public void copy (Flow.DataflowObject o) 
        {
            DefSet t = (DefSet)o;
            set = new TreeSet<String>(t.set);
        }

        @Override
        public boolean equals(Object o) 
        {
            if (o instanceof DefSet) 
            {
                DefSet a = (DefSet) o;
                return set.equals(a.set);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return set.hashCode();
        }
        /**
         * toString() method for the dataflow objects which is used
         * by postprocess() below.  The format of this method must
         * be of the form "[ID0, ID1, ID2, ...]", where each ID is
         * the identifier of a quad defining some register, and the
         * list of IDs must be sorted.  See src/test/test.rd.out
         * for example output of the analysis.  The output format of
         * your reaching definitions analysis must match this exactly.
         */
        @Override
        public String toString() { return set.toString(); }
        public void genVar(String v) { set.add(v);}
        public void killVar(String v) { 
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String s = it.next();
                if (s.contains(v)) {
                    it.remove();
                }
            }
        }
    }

    private DefSet[] in, out;
    private DefSet entry, exit;

    public void preprocess(ControlFlowGraph cfg) {
        /* Generate initial conditions. */
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        while (qit.hasNext()) {
            int x = qit.next().getID();
            if (x > max) max = x;
        }
        max += 1;
        in = new DefSet[max];
        out = new DefSet[max];
        qit = new QuadIterator(cfg);

        Set<String> s = new TreeSet<String>();
        DefSet.universalSet = s;

        /* Arguments are always there. */
        while (qit.hasNext()) {
            Quad q = qit.next();
            if (q.getOperator() instanceof Operator.BoundsCheck) {
                s.add(q.toString());
            }
        }

        entry = new DefSet();
        exit = new DefSet();
        transferfn.val = new DefSet();
        for (int i=0; i<in.length; i++) {
            in[i] = new DefSet();
            in[i].setToTop();
            out[i] = new DefSet();
            out[i].setToTop();
        }
    }

    public void postprocess(ControlFlowGraph cfg) {
        QuadIterator qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            Quad q = qit.next();
            if (q.getOperator() instanceof Operator.BoundsCheck) {
                int id = q.getID();
                if (in[id].contains(q.toString())) {
                    qit.remove();
                }
            }
        }
    }

    /* Is this a forward dataflow analysis? */
    public boolean isForward() { return true;}

    /* Routines for interacting with dataflow values. */

    public Flow.DataflowObject getEntry() 
    { 
        Flow.DataflowObject result = newTempVar();
        result.copy(entry); 
        return result;
    }
    public Flow.DataflowObject getExit() 
    { 
        Flow.DataflowObject result = newTempVar();
        result.copy(exit); 
        return result;
    }
    public Flow.DataflowObject getIn(Quad q) 
    {
        Flow.DataflowObject result = newTempVar();
        result.copy(in[q.getID()]); 
        return result;
    }
    public Flow.DataflowObject getOut(Quad q) 
    {
        Flow.DataflowObject result = newTempVar();
        result.copy(out[q.getID()]); 
        return result;
    }
    public void setIn(Quad q, Flow.DataflowObject value) 
    { 
        in[q.getID()].copy(value); 
    }
    public void setOut(Quad q, Flow.DataflowObject value) 
    { 
        out[q.getID()].copy(value); 
    }
    public void setEntry(Flow.DataflowObject value) 
    { 
        entry.copy(value); 
    }
    public void setExit(Flow.DataflowObject value) 
    { 
        exit.copy(value); 
    }

    public Flow.DataflowObject newTempVar() {         
        DefSet val = new DefSet();
        val.setToTop();
        return val; 
    }

    /* Actually perform the transfer operation on the relevant
     * quad. */

    private TransferFunction transferfn = new TransferFunction ();
    public void processQuad(Quad q) {
        transferfn.val.copy(in[q.getID()]);
        transferfn.visitQuad(q);
        out[q.getID()].copy(transferfn.val);
    }

    /* The QuadVisitor that actually does the computation */
    public static class TransferFunction extends QuadVisitor.EmptyVisitor {
        DefSet val;
        @Override
        public void visitQuad(Quad q) {
            for (RegisterOperand def : q.getDefinedRegisters()) {
                val.killVar(def.getRegister().toString());
            }
            if (q.getOperator() instanceof Operator.BoundsCheck) {
                    val.genVar(q.toString());
            }
        }
    }
}
