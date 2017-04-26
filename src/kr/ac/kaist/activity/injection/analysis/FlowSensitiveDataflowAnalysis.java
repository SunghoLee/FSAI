package kr.ac.kaist.activity.injection.analysis;

import com.google.common.collect.Lists;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;

import java.util.*;

/**
 * Created by leesh on 21/04/2017.
 */
public class FlowSensitiveDataflowAnalysis<T extends DataflowResult> {

    public enum Direction{
    FORWARD_ANALYSIS,
    }

    public enum Options{
        APP_ONLY,
        TRACK_DEFUSE,
        KEEP_STACK,
    }
    private Direction d;
    private final ICFGSupergraph supergraph;
    private final Set<BasicBlockInContext<IExplodedBasicBlock>> entries;
    private final CallGraph cg;
    private int direction;
    private T res;
    private final Map<Integer, Object> heap;
    private final Set<Options> options;

    public FlowSensitiveDataflowAnalysis(CallGraph cg, Options... options){
        this.cg = cg;
        this.supergraph = ICFGSupergraph.make(cg, new AnalysisCache());
        this.entries = new HashSet<>();
        this.d = Direction.FORWARD_ANALYSIS;
        heap = new HashMap<>();
        this.options = new HashSet<>(Lists.newArrayList(options));
    }

    public void setEntries(CGNode... nodes){
        for(CGNode n : nodes){
            entries.addAll(Lists.newArrayList(supergraph.getEntriesForProcedure(n)));
        }
    }

    public void setAnalysisDirection(Direction d){
        this.d = d;
    }

    public T analyze(){
        Stack<BasicBlockInContext<IExplodedBasicBlock>> workList = new Stack<>();
        workList.addAll((entries.isEmpty())? findDefaultEntries() : entries);

        while(workList.isEmpty()){

        }

        return res;
    }

    private NodeInFlow makeNewNode(NodeInFlow bb, BasicBlockInContext<IExplodedBasicBlock> next, int v){
        if(options.contains(Options.KEEP_STACK)){
            NodeInFlowWithCallStack stackBB = (NodeInFlowWithCallStack) bb;

            if(supergraph.isCall(bb.getConcreteNode()) && supergraph.isEntry(next)){
                Stack newCallStack = (Stack)stackBB.getCallStack().clone();
                newCallStack.push(stackBB.getConcreteNode().getNode());

                return new NodeInFlowWithCallStack(next, v, newCallStack);
            }
        }else{

        }
        return null;
    }

    private int getCalleeV(SSAAbstractInvokeInstruction invokeInst, int useV){
        for(int i=0; i<invokeInst.getNumberOfUses(); i++){
            if(invokeInst.getUse(i) == useV)
                return i+1;
        }
        return -1;
    }

    private Set<NodeInFlow> findNextBlocks(NodeInFlow bb, int v){
        Set<NodeInFlow> nexts = new HashSet<>();

        if(this.d.equals(Direction.FORWARD_ANALYSIS)){
            if(options.contains(Options.TRACK_DEFUSE)){

            }else {
                Iterator<BasicBlockInContext<IExplodedBasicBlock>> iSucc = supergraph.getSuccNodes(bb.getConcreteNode());
                while (iSucc.hasNext()) {
                    BasicBlockInContext<IExplodedBasicBlock> succ = iSucc.next();
                    if(filteroutNextBlocks(bb, succ)) {
                        nexts.add(makeNewNode(bb, succ, bb.getPursuingValue()));
                    }
                }
            }
        }else{
            //TODO: implement backward data tracking
        }

        return nexts;
    }

    protected boolean filteroutNextBlocks(NodeInFlow bb, BasicBlockInContext<IExplodedBasicBlock> next){
        if(options.contains(Options.APP_ONLY)){
            if(!next.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application))
                return false;
        }

        if(options.contains(Options.KEEP_STACK)){
            if(supergraph.isExit(bb.getConcreteNode())){
                NodeInFlowWithCallStack csn = (NodeInFlowWithCallStack) bb;
                if(csn.getCallStack().isEmpty() || !csn.getCallStack().peek().equals(next.getNode()))
                    return false;
            }
        }

        return true;
    }

    protected CallGraph getCallGraph(){
        return cg;
    }

    protected ICFGSupergraph getICFG(){
        return this.supergraph;
    }

    protected Set<BasicBlockInContext<IExplodedBasicBlock>> findDefaultEntries(){
        entries.add(supergraph.getMainEntry());
        return entries;
    }

    protected boolean isTrackable(BasicBlockInContext<IExplodedBasicBlock> bb, BasicBlockInContext<IExplodedBasicBlock> succ){
        return true;
    }

    public interface NodeInFlow {
        public BasicBlockInContext<IExplodedBasicBlock> getConcreteNode();
        public int getPursuingValue();
    }

    static class NormalNodeInFlow implements NodeInFlow {
        private final BasicBlockInContext<IExplodedBasicBlock> bb;
        private final int v;

        public NormalNodeInFlow(BasicBlockInContext<IExplodedBasicBlock> bb, int v){
            this.bb = bb;
            this.v = v;
        }

        @Override
        public int getPursuingValue() {
            return v;
        }

        @Override
        public BasicBlockInContext<IExplodedBasicBlock> getConcreteNode() {
            return this.bb;
        }

        @Override
        public int hashCode(){
            return bb.hashCode() * this.v;
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof NormalNodeInFlow){
                NormalNodeInFlow nnif = (NormalNodeInFlow) o;
                if(nnif.bb.equals(this.bb) && nnif.v == this.v)
                    return true;
            }
            return false;
        }
    }

    static class NodeInFlowWithCallStack implements NodeInFlow {
        private final BasicBlockInContext<IExplodedBasicBlock> bb;
        private final int v;
        private final Stack<CGNode> callStack;

        public NodeInFlowWithCallStack(BasicBlockInContext<IExplodedBasicBlock> bb, int v, Stack<CGNode> callStack){
            this.bb = bb;
            this.callStack = (Stack)callStack.clone();
            this.v = v;
        }

        @Override
        public BasicBlockInContext<IExplodedBasicBlock> getConcreteNode() {
            return this.bb;
        }

        @Override
        public int getPursuingValue() {
            return v;
        }

        public Stack<CGNode> getCallStack(){
            return this.callStack;
        }

        @Override
        public int hashCode(){
            return bb.hashCode() * this.v + callStack.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof NodeInFlowWithCallStack){
                NodeInFlowWithCallStack nnif = (NodeInFlowWithCallStack) o;
                if(nnif.bb.equals(this.bb) && nnif.v == this.v && isSameCallStack(nnif.callStack, this.callStack))
                    return true;
            }
            return false;
        }

        private static boolean isSameCallStack(Stack a, Stack b){
            if(a.size() == b.size()){
                for(int i=0; i<a.size(); i++){
                    if(!a.get(i).equals(b.get(i)))
                        return false;
                }
                return true;
            }
            return false;
        }
    }
}
