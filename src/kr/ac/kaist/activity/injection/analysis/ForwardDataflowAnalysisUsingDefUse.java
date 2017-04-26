package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.debug.Assertions;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by leesh on 25/04/2017.
 */
public class ForwardDataflowAnalysisUsingDefUse {

    private final CallGraph cg;
    private final Set<Point> seeds = new HashSet<>();
    private final FlowCatcher catcher;
    private final CHACache cache;

    public ForwardDataflowAnalysisUsingDefUse(CallGraph cg, CHACache cache, FlowCatcher catcher){
        this.cg = cg;
        this.catcher = catcher;
        this.cache = cache;
    }

    public void addSeed(Point seed){
        this.seeds.add(seed);
    }

    private Set<Point> trackForward(){
        Set<Point> res = new HashSet<>();
        Set<Point> visited = new HashSet<>();
        Queue<Point> queue = new LinkedBlockingQueue<>();
        queue.add(p);

        while(!queue.isEmpty()){
            Point point = queue.poll();
            if(visited.contains(point))
                continue;
            visited.add(point);
            queue.addAll(getSuccPoints(point, res));
        }

        return res;
    }

    private Set<Point> getSuccPoints(final Point p, Set<Point> finalResult){
        final Set<Point> res = new HashSet<>();

        DefUse du = cache.makeDefUse(p.getNode());
        Iterator<SSAInstruction> iUse = du.getUses(p.getTrackingVar());

        while(iUse.hasNext()){
            SSAInstruction useInst = iUse.next();
            Point newPoint = new Point(p.getNode(), useInst.iindex, p.getTrackingVar());
            if(catcher.needToCatch(new Point(p.getNode(), useInst.iindex, p.getTrackingVar())))
                finalResult.add(newPoint);
            else {
                useInst.visit(new SSAInstruction.IVisitor() {
                    @Override
                    public void visitGoto(SSAGotoInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitBinaryOp(SSABinaryOpInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitConversion(SSAConversionInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitComparison(SSAComparisonInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitSwitch(SSASwitchInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitReturn(SSAReturnInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitGet(SSAGetInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitPut(SSAPutInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitInvoke(SSAInvokeInstruction instruction) {
//                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                        CGNode curNode = p.getNode();
                        int param = -1;

                        //calculate an argument var corresponding to the tracking var
                        for(int i=0; i<instruction.getNumberOfUses(); i++){
                            if(instruction.getUse(i) == p.getTrackingVar()) {
                                param = i+1;
                                break;
                            }
                        }

                        if(param == -1)
                            Assertions.UNREACHABLE("Cannot track the variable anymore: " + instruction + " # " + p.getTrackingVar());

                        for(CGNode target : cg.getPossibleTargets(curNode, instruction.getCallSite())){
                            IR ir = cache.makeIR(target);

                            //find the first instruction of a callee
                            for(SSAInstruction inst : ir.getInstructions()){
                                if(inst != null) {
                                    res.add(new Point(target, inst.iindex, param));
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void visitNew(SSANewInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitThrow(SSAThrowInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitMonitor(SSAMonitorInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitCheckCast(SSACheckCastInstruction instruction) {
                        res.add(new Point(p.getNode(), instruction.iindex, instruction.getDef());
                    }

                    @Override
                    public void visitInstanceof(SSAInstanceofInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitPhi(SSAPhiInstruction instruction) {
                        res.add(new Point(p.getNode(), instruction.iindex, instruction.getDef());
                    }

                    @Override
                    public void visitPi(SSAPiInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + this + " # " + p.getTrackingVar());
                    }
                });
            }
        }
        return res;
    }

    public Set<DataflowResult> analyze(){
        Set<DataflowResult> res = new HashSet<>();
        if(seeds.isEmpty()){
            System.err.println("Do not set any seeds!");
            return Collections.emptySet();
        }

        for(Point p : seeds){
            res.add(new DataflowResult(p, trackForward(p)));
        }

        return res;
    }

    interface FlowCatcher{
        public boolean needToCatch(Point p);
    }
}
