package submit;

import joeq.Class.jq_Class;
import joeq.Main.Helper;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operator.*;
import joeq.Compiler.Quad.Operand.*;
import flow.Flow;
import hw2.MySolver;
import java.util.*;

public class RedundantNullCheck implements Flow.Analysis {

        public static class VarSet implements Flow.DataflowObject {
            private Set<String> set;
            public static Set<String> universalSet;
            public VarSet() { set = new TreeSet<String>(); }

            public void setToTop() { set = new TreeSet<String>(universalSet); }
            public void setToBottom() { set = new TreeSet<String>(); }

            public void meetWith(Flow.DataflowObject o) 
            {
                VarSet a = (VarSet)o;
                set.retainAll(a.set);
            }

            public void copy(Flow.DataflowObject o) 
            {
                VarSet a = (VarSet) o;
                set = new TreeSet<String>(a.set);
            }

            @Override
            public boolean equals(Object o) 
            {
                if (o instanceof VarSet) 
                {
                    VarSet a = (VarSet) o;
                    return set.equals(a.set);
                }
                return false;
            }
            @Override
            public int hashCode() {
                return set.hashCode();
            }
            @Override
            public String toString() 
            {
                return set.toString();
            }

            public void genVar(String v) {set.add(v);}
            public void killVar(String v) {set.remove(v);}
        }

        private VarSet[] in, out;
        private VarSet entry, exit;

        public void preprocess(ControlFlowGraph cfg) {
            /* Generate initial conditions. */
            QuadIterator qit = new QuadIterator(cfg);
            int max = 0;
            while (qit.hasNext()) {
                int x = qit.next().getID();
                if (x > max) max = x;
            }
            max += 1;
            in = new VarSet[max];
            out = new VarSet[max];
            qit = new QuadIterator(cfg);

            Set<String> s = new TreeSet<String>();
            VarSet.universalSet = s;

            /* Arguments are always there. */
            int numargs = cfg.getMethod().getParamTypes().length;
            for (int i = 0; i < numargs; i++) {
                s.add("R"+i);
            }

            while (qit.hasNext()) {
                Quad q = qit.next();
                for (RegisterOperand def : q.getDefinedRegisters()) {
                    s.add(def.getRegister().toString());
                }
                for (RegisterOperand use : q.getUsedRegisters()) {
                    s.add(use.getRegister().toString());
                }
            }

            entry = new VarSet();
            exit = new VarSet();
            transferfn.val = new VarSet();
            for (int i=0; i<in.length; i++) {
                in[i] = new VarSet();
                in[i].setToTop();
                out[i] = new VarSet();
                out[i].setToTop();
            }
        }

        public void postprocess(ControlFlowGraph cfg) {
            System.out.print(cfg.getMethod().getName());
            SortedSet<Integer> result = new TreeSet<Integer>();
            QuadIterator qit = new QuadIterator(cfg);
            while (qit.hasNext()) {
                Quad q = qit.next();
                if (q.getOperator() instanceof Operator.NullCheck) {
                    int id = q.getID();
                    //System.out.println("in "+id+": "+in[id]);
                    //System.out.println("out "+id+": "+out[id]);
                    if (in[id].equals(out[id])) {
                        result.add(id);
                    }
                }
            }
            for (int id: result) {
                System.out.print(" "+id);
            }
            System.out.println();
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
            VarSet val = new VarSet();
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
            VarSet val;
            @Override
            public void visitQuad(Quad q) {
                for (RegisterOperand def : q.getDefinedRegisters()) {
                    val.killVar(def.getRegister().toString());
                }
                if (q.getOperator() instanceof Operator.NullCheck) {
                    for (RegisterOperand use : q.getUsedRegisters()) {
                        val.genVar(use.getRegister().toString());
                    }
                }
            }
        }
    }
