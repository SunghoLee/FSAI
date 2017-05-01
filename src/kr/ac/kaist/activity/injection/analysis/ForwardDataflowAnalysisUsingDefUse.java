package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static kr.ac.kaist.activity.injection.analysis.LibraryModel.isLibrary;

/**
 * Created by leesh on 25/04/2017.
 */
public class ForwardDataflowAnalysisUsingDefUse {

    private final Map<Point, Set<Point>> consideringPoint = new HashMap<>();
    private final CallGraph cg;
    private final Set<Point> seeds = new HashSet<>();
    private final CHACache cache;
    private boolean termFlag = false;

    public ForwardDataflowAnalysisUsingDefUse(CallGraph cg, CHACache cache){
        this.cg = cg;
        this.cache = cache;
    }

    public void addSeed(Point seed){
        this.seeds.add(seed);
    }
    public void addSeeds(Set<Point> seeds){
        this.seeds.addAll(seeds);
    }

    private Set<Point> trackForward(Point p){
        Set<Point> res = new HashSet<>();
        Queue<Point> queue = new LinkedBlockingQueue<>();

        consideringPoint.put(p, new HashSet<>());
        consideringPoint.get(p).add(p);

        queue.add(p);

        while(!queue.isEmpty()){
            Point point = queue.poll();

//            System.out.println("#FORWARD!");
//            System.out.println("\t#P: " + point);
            Set<Point> succes = getSuccPoints(point);

            if(termFlag){
                consideringPoint.clear();
                termFlag = false;
                return res;
            }

            for(Point succ : succes){
                if(isChanged(p, succ, succes)) {
                    queue.add(succ);
                }else{
//                    System.out.println("\t#FAILED SUCC: " + succ);
                }
            }
        }

        consideringPoint.clear();
        return res;
    }

    private boolean isChanged(Point p, Point succ, Set<Point> newFinding){
        if(!consideringPoint.containsKey(succ)){
            consideringPoint.put(succ, new HashSet<>());
            consideringPoint.get(succ).addAll(newFinding);
            return true;
        }

        if(!consideringPoint.containsKey(p)){
            consideringPoint.put(p, new HashSet<>());
        }

        Set<Point> prePoints = consideringPoint.get(succ);
        Set<Point> curPoints = new HashSet<>(consideringPoint.get(p));
        curPoints.addAll(newFinding);
        if(prePoints.equals(curPoints))
            return false;

        consideringPoint.put(succ, curPoints);
        return true;
    }

    protected Set<Point> getSuccPoints(final Point p){
        final Set<Point> res = new HashSet<>();
        try {
            DefUse du = cache.makeDefUse(p.getNode());
            Iterator<SSAInstruction> iUse = du.getUses(p.getTrackingVar());

            Work w = p.getWork();
            CGNode n = p.getNode();
            if(w == null)
                return res;

            if (p.getinstruction().getDef() == p.getTrackingVar()) {
                while (iUse.hasNext()) {
                    SSAInstruction useInst = iUse.next();
                    Point newPoint = new Point(p.getNode(), useInst.iindex, p.getTrackingVar(), useInst, w.clone());
                    res.add(newPoint);
                }
            } else {
                p.getinstruction().visit(new SSAInstruction.IVisitor() {
                    @Override
                    public void visitGoto(SSAGotoInstruction instruction) {

                    }

                    @Override
                    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
                        if(instruction.getArrayRef() == p.getTrackingVar()) {
                            Work newWork = w.visit(n, instruction);

                            if (!newWork.equals(w)) {
                                res.add(new Point(n, instruction.iindex, instruction.getDef(), instruction, newWork.clone().setTargetV(instruction.getDef())));
                            }
                        }else{
                            Assertions.UNREACHABLE("Is possible?: " + p + " in " + instruction);
                        }
                    }

                    @Override
                    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
                        if(instruction.getValue() == p.getTrackingVar()) {
                            Iterator<SSAInstruction> iUses = cache.makeDefUse(n).getUses(instruction.getArrayRef());

                            res.addAll(findUsageOfAlias(new Point(n, instruction.iindex, instruction.getArrayRef(), instruction, new Work(w.clone(), instruction.getArrayRef(), new WorkVisitor() {
                                @Override
                                public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                                    if (inst instanceof SSAArrayLoadInstruction && ((SSAArrayLoadInstruction) inst).getArrayRef() == targetV) {
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public String toString() {
                                    return "ARRAYLOAD_WORK";
                                }
                            }))));

                            while (iUses.hasNext()) {
                                SSAInstruction useInst = iUses.next();
                                if (useInst.iindex > instruction.iindex) {
                                    res.add(new Point(n, useInst.iindex, instruction.getArrayRef(), useInst, new Work(w.clone(), instruction.getArrayRef(), new WorkVisitor() {
                                        @Override
                                        public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                                            if (inst instanceof SSAArrayLoadInstruction && ((SSAArrayLoadInstruction) inst).getArrayRef() == targetV) {
                                                return true;
                                            }
                                            return false;
                                        }

                                        @Override
                                        public String toString() {
                                            return "ARRAYLOAD_WORK";
                                        }
                                    })));
                                }
                            }
//                            res.add(p);
                        }else if(instruction.getArrayRef() == p.getTrackingVar()){

                        }else{
                            Assertions.UNREACHABLE("Is possible?: " + p + " in " + instruction);
                        }
                    }

                    @Override
                    public void visitBinaryOp(SSABinaryOpInstruction instruction) {

                    }

                    @Override
                    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {

                    }

                    @Override
                    public void visitConversion(SSAConversionInstruction instruction) {

                    }

                    @Override
                    public void visitComparison(SSAComparisonInstruction instruction) {

                    }

                    @Override
                    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {

                    }

                    @Override
                    public void visitSwitch(SSASwitchInstruction instruction) {

                    }

                    @Override
                    public void visitReturn(SSAReturnInstruction instruction) {
                        Iterator<CGNode> iPred = cg.getPredNodes(p.getNode());
                        while (iPred.hasNext()) {
                            CGNode pred = iPred.next();
                            if(isLibrary(pred))
                                continue;

                            Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, p.getNode());
                            while (iCsr.hasNext()) {
                                CallSiteReference csr = iCsr.next();
                                try {
                                    for (SSAAbstractInvokeInstruction invokeInst : cache.makeIR(pred).getCalls(csr)) {
                                        if (invokeInst.hasDef()) {
                                            res.add(new Point(pred, invokeInst.iindex, invokeInst.getDef(), invokeInst, w.clone().setTargetV(invokeInst.getDef())));
                                        }
                                    }
                                } catch (NullPointerException e) {
                                    System.err.println("6# Cannot build an IR for the node: " + pred);
                                }
                            }
                        }
//                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + instruction + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitGet(SSAGetInstruction instruction) {
                        if(instruction.isStatic()){
                            Assertions.UNREACHABLE("It is impisslbe! : " + p + " in " + instruction);
                        }
                        else if(instruction.getRef() == p.getTrackingVar()) {
                            Work newWork = w.visit(n, instruction);
                            if (!newWork.equals(w)) {
                                res.add(new Point(n, instruction.iindex, instruction.getDef(), instruction, newWork.clone().setTargetV(instruction.getDef())));
                            }

//                            res.add(p);
                        }else{
                            Assertions.UNREACHABLE("Is possible?: " + p + " in " + instruction);
                        }
                    }

                    @Override
                    public void visitPut(SSAPutInstruction instruction) {
                        final FieldReference field = instruction.getDeclaredField();

                        if(instruction.isStatic()){

                            System.err.println("We do not treat static field: " + p + " in " + instruction);
//                            for(Pair<CGNode,SSAGetInstruction> pair : findAllGetInstsForStaticField(field)) {
//                                res.add(new Point(pair.fst, pair.snd.iindex, pair.snd.getDef(), pair.snd, p.getWork().clone()));
//                            }
//
//                            for(Pair<CGNode,SSAPutInstruction> pair : findAllPutInstsForStaticField(field)) {
//                                res.add(new Point(pair.fst, pair.snd.iindex, pair.snd.getVal(), pair.snd, p.getWork().clone()));
//                            }
                        }else if(instruction.getVal() == p.getTrackingVar()){
                            Iterator<SSAInstruction> iUses = cache.makeDefUse(n).getUses(instruction.getRef());

                            res.addAll(findUsageOfAlias(new Point(n, instruction.iindex, instruction.getRef(), instruction, new Work(w.clone(), instruction.getRef(), new WorkVisitor() {
                                @Override
                                public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                                    if (inst instanceof SSAGetInstruction && ((SSAGetInstruction) inst).getRef() == targetV && ((SSAGetInstruction) inst).getDeclaredField().equals(field)) {
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public String toString() {
                                    return "GET_WORK";
                                }
                            }))));

                            while(iUses.hasNext()){
                                SSAInstruction useInst = iUses.next();
                                if(useInst.iindex > instruction.iindex) {
                                    res.add(new Point(n, useInst.iindex, instruction.getRef(), useInst, new Work(w.clone(), instruction.getRef(), new WorkVisitor() {
                                        @Override
                                        public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                                            if (inst instanceof SSAGetInstruction && ((SSAGetInstruction) inst).getRef() == targetV && ((SSAGetInstruction) inst).getDeclaredField().equals(field)) {
                                                return true;
                                            }
                                            return false;
                                        }

                                        @Override
                                        public String toString() {
                                            return "GET_WORK";
                                        }
                                    })));
                                }
                            }
//                            res.add(p);
                        }else{
//                            Assertions.UNREACHABLE("Is possible?: " + p + " in " + instruction);
                        }
                    }

                    @Override
                    public void visitInvoke(SSAInvokeInstruction instruction) {
                        CGNode curNode = p.getNode();
                        int param = -1;

                        Work newWork = w.visit(n, instruction);

                        if(newWork == null){
                            termFlag = true;
                        }
                        //calculate an argument var corresponding to the tracking var
                        for (int i = 0; i < instruction.getNumberOfUses(); i++) {
                            if (instruction.getUse(i) == p.getTrackingVar()) {
                                param = i + 1;
                                break;
                            }
                        }

                        if (param == -1)
                            Assertions.UNREACHABLE("Cannot track the variable anymore: " + instruction + " # " + p.getTrackingVar());

                        //treat all callees at the call site; do not track library methods
                        for (CGNode target : cg.getPossibleTargets(curNode, instruction.getCallSite())) {
                            if (isLibrary(target))
                                continue;

                            try {
                                DefUse du = cache.makeDefUse(target);
                                Iterator<SSAInstruction> iUse = du.getUses(param);

                                //find use instructions of the parameter
                                while (iUse.hasNext()) {
                                    SSAInstruction useInst = iUse.next();
                                    res.add(new Point(target, useInst.iindex, param, useInst, newWork.clone().setTargetV(param)));
                                }
                            }catch(NullPointerException e){
                                System.err.println("5# Cannot build an IR for the node: " + target);
                            }
                        }
                    }

                    @Override
                    public void visitNew(SSANewInstruction instruction) {
                        //TODO: is it possible? but WALA treats this possible. Why?
//                        System.out.println("? " + instruction.getNumberOfUses());
//                        System.out.println("? " + instruction.getUse(0));
//                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + instruction + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
                        //TODO: ignore this case
                    }

                    @Override
                    public void visitThrow(SSAThrowInstruction instruction) {
//                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + instruction + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitMonitor(SSAMonitorInstruction instruction) {
                        //no-op;
                    }

                    @Override
                    public void visitCheckCast(SSACheckCastInstruction instruction) {
                        res.add(new Point(p.getNode(), instruction.iindex, instruction.getDef(), instruction, w.clone()));
                    }

                    @Override
                    public void visitInstanceof(SSAInstanceofInstruction instruction) {
                        //TODO: ignore this case
                    }

                    @Override
                    public void visitPhi(SSAPhiInstruction instruction) {
                        res.add(new Point(p.getNode(), instruction.iindex, instruction.getDef(), instruction, w.clone()));
                    }

                    @Override
                    public void visitPi(SSAPiInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + instruction + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + instruction + " # " + p.getTrackingVar());
                    }

                    @Override
                    public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
                        Assertions.UNREACHABLE("This instruction cannot use a tracking variable: " + instruction + " # " + p.getTrackingVar());
                    }
                });


            }
        }catch(NullPointerException e){
            System.err.println("8# Cannot build an IR for the node: " + p.getNode());
            e.printStackTrace();
        }
        return res;
    }

//    private Set<Point> handleLastInstruction(Point p){
//        Set<Point> res = new HashSet<>();
//
//        if(p.getTrackingVar() == 1) {
//            DefUse du = cache.makeDefUse(p.getNode());
//            Iterator<SSAInstruction> iUses = du.getUses(p.getTrackingVar());
//            boolean isLast = true;
//            while(iUses.hasNext()){
//                SSAInstruction use = iUses.next();
//                if(use.iindex > p.getinstruction().iindex) {
//                    isLast = false;
//                    break;
//                }
//            }
//
//            if(isLast){
//                System.out.println("ISLAST? true");
//                Iterator<CGNode> iPred = cg.getPredNodes(p.getNode());
//                while(iPred.hasNext()){
//                    CGNode pred = iPred.next();
//                    if(isLibrary(pred))
//                        continue;
//
//                    Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, p.getNode());
//                    IR ir = cache.makeIR(pred);
//
//                    while(iCsr.hasNext()){
//                        CallSiteReference csr = iCsr.next();
//                        for(SSAAbstractInvokeInstruction invokeInst : ir.getCalls(csr)){
//                            int receiver = invokeInst.getReceiver();
//                            DefUse preDU = cache.makeDefUse(pred);
//                            Iterator<SSAInstruction> iPredUses = preDU.getUses(receiver);
//                            System.out.println("\tRECEIVER: " + receiver);
//                            while(iPredUses.hasNext()){
//                                SSAInstruction predUseInst = iPredUses.next();
//                                System.out.println("\tPREDINST: " + predUseInst);
//                                if(predUseInst.iindex > invokeInst.iindex)
//                                    res.add(new Point(pred, predUseInst.iindex, receiver, predUseInst, p.getWork().clone().setTargetV(receiver)));
//                            }
//                        }
//                    }
//                }
//                System.out.println("#RES: " + res);
//            }
//        }
//
//        return res;
//    }

    private Set<Pair<CGNode, SSAGetInstruction>> findAllGetInstsForStaticField(FieldReference fr){
        Set<Pair<CGNode,SSAGetInstruction>> res = new HashSet<>();

        for(CGNode n : cg){
            if(isLibrary(n))
                continue;

            IR ir = cache.makeIR(n);
            for(SSAInstruction inst : ir.getInstructions()){
                if(inst == null)
                    continue;
                if(inst instanceof SSAGetInstruction){
                    SSAGetInstruction getInst = (SSAGetInstruction) inst;
                    if(getInst.isStatic() && getInst.getDeclaredField().equals(fr))
                        res.add(Pair.make(n,getInst));
                }
            }
        }

        return res;
    }

    private Set<Pair<CGNode,SSAPutInstruction>> findAllPutInstsForStaticField(FieldReference fr){
        Set<Pair<CGNode,SSAPutInstruction>> res = new HashSet<>();

        for(CGNode n : cg){
            if(isLibrary(n))
                continue;

            IR ir = cache.makeIR(n);
            for(SSAInstruction inst : ir.getInstructions()){
                if(inst == null)
                    continue;
                if(inst instanceof SSAPutInstruction){
                    SSAPutInstruction putInst = (SSAPutInstruction) inst;
                    if(putInst.isStatic() && putInst.getDeclaredField().equals(fr))
                        res.add(Pair.make(n,putInst));
                }
            }
        }

        return res;
    }

    private Set<Point> findAlias(Point p){
        BackwardDataflowUsingDefUse backward = new BackwardDataflowUsingDefUse(cg, cache);
        backward.addSeeds(p);
        Set<Point> alias = backward.analyze();

//        System.out.println("#####");
//        System.out.println(alias);
//        System.out.println("#####");
        return alias;
    }

    private Set<Point> findUsageOfAlias(Point p){

        Set<Point> alias = findAlias(p);
        Set<Point> res = new HashSet<>();
        for(Point ali : alias){
            DefUse du = cache.makeDefUse(ali.getNode());
            Iterator<SSAInstruction> iUses = du.getUses(ali.getTrackingVar());
            while(iUses.hasNext()){
                SSAInstruction use = iUses.next();
                if(use.iindex > ali.getIndex()){
                    res.add(new Point(ali.getNode(), use.iindex, ali.getTrackingVar(), use, ali.getWork().clone()));
                }
            }
        }

        return res;
    }
//
    private boolean isPossibleType(TypeReference original, TypeReference next){
        IClassHierarchy cha = cg.getClassHierarchy();

        IClass originalClass = cha.lookupClass(original);
        IClass nextClass = cha.lookupClass(next);

//        System.out.println("\t\t#ORI: " + originalClass);
//        System.out.println("\t\t#NEXT: " + nextClass);
//        System.out.println("\t\t#RES: " + cha.isSubclassOf(originalClass, nextClass));
//        try {
//            System.in.read();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if(originalClass != null && nextClass != null && cha.isSubclassOf(originalClass, nextClass))
            return true;

        return false;
    }

    public void analyze(){

        if(seeds.isEmpty()){
            System.out.println("Do not set any seeds!");
            return;
        }

        for(Point p : seeds){
            trackForward(p);
        }
    }

    static class Work{
        private final Work next;
        protected int targetV;
        protected WorkVisitor visitor;

        public Work(Work next, int targetV, WorkVisitor visitor){
            this.next = next;
            this.targetV = targetV;
            this.visitor = visitor;
        }

        public Work visit(CGNode n, SSAInstruction inst){
            if(visitor.visit(n, inst, targetV))
                return this.next;
            else
                return this;
        }

        public Work clone(){
            return new Work(next, targetV, visitor);
        }

        public Work getNext(){
            return this.next;
        }

        public int getTargetV(){
            return this.targetV;
        }

        public Work setTargetV(int targetV){
            this.targetV = targetV;
            return this;
        }

        public void setVisitor(WorkVisitor visitor){
            this.visitor = visitor;
        }

        public int getLenght(){
            if(next == null)
                return 1;
            return 1 + next.getLenght();
        }

        @Override
        public String toString(){
            return visitor.toString();
        }
    }

    static abstract class WorkVisitor{
        abstract public boolean visit(CGNode n, SSAInstruction inst, int targetV);

        @Override
        abstract public String toString();
    }
}
