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
public class BackwardDataflowUsingDefUse {

    private final CallGraph cg;
    private final CHACache cache;
    private final Set<Point> seeds = new HashSet<>();
    private final Map<Point, Set<Point>> consideringPoint = new HashMap<>();

    public BackwardDataflowUsingDefUse(CallGraph cg, CHACache cache){
        this.cg = cg;
        this.cache = cache;
    }

    Set<Point> finalResult = new HashSet<>();

    protected Set<Point> trackBackward(Point p){
        Queue<Point> queue = new LinkedBlockingQueue<>();
        queue.add(p);
        finalResult.clear();
        while(!queue.isEmpty()){
            Point point = queue.poll();
//            System.out.println("QUEUE_SIZE: " + queue.size() + " , " + point);

//            System.out.println("#BACKWARD!");
//            System.out.println("\t#P: " + point);

            Set<Point> preds = getPredPoints(point);

            for(Point pred : preds){
                if(isChanged(p, pred, preds)) {
                    queue.add(pred);
                }else{
//                    System.out.println("\t#FAILED PRED: " + pred);
                }
            }
        }

        return finalResult;
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

    protected Set<Point> getPredPoints(Point p){
        Set<Point> res = new HashSet<>();

        /*
        There are eight possible cases:
        1. defined as argument
        2. defined by null
        3. defined by return value of method call
        4. defined by getField instruction
        5. defined by loadArray instruction
        6. defined by cast instruction
        7. defined by Phi instruction
        8. created in this node
         */
        try {
            if (p.getTrackingVar() != p.getinstruction().getDef()) {
                //1. defined as argument
                if (p.getTrackingVar() <= p.getNode().getMethod().getNumberOfParameters()) {
                    res.addAll(handleArgumentPassing(p, p.getNode(), p.getTrackingVar()));
                }
                //2. defined by null
                else if (cache.makeSymbolTable(p.getNode()).isNullConstant(p.getTrackingVar())) {
                    //no-op; we do not track null values
                } else if (cache.makeSymbolTable(p.getNode()).isConstant(p.getTrackingVar())) {
                }
                //TODO: treat other constant values?
                // find def point
                else {

                    DefUse du = cache.makeDefUse(p.getNode());
                    SSAInstruction defInst = du.getDef(p.getTrackingVar());
//                    System.out.println("#DEF: " + defInst);
                    // 7. defined by Phi instruction
                    if (defInst instanceof SSAPhiInstruction) {
                /*
                In this case, we only change the tracking vars with them used in the phi instruction.
                The index of phi instruction is smaller than zero, these points are also tracked by this method.
                 */
                        res.addAll(handlePhiInstruction(p, p.getNode(), (SSAPhiInstruction) defInst));
                    } else {
                        res.add(new Point(p.getNode(), defInst.iindex, p.getTrackingVar(), defInst, p.getWork().clone()));
                    }

                }
            }
            // for 3, 4, 5, 6 or 8 cases
            else {
                SSAInstruction defInst = p.getinstruction();

                //3. defined by return value of method call
                if (defInst instanceof SSAAbstractInvokeInstruction) {
                    res.addAll(handleMethodCall(p, p.getNode(), (SSAAbstractInvokeInstruction) defInst));
                }
                //4. defined by getField instruction
                else if (defInst instanceof SSAGetInstruction) {
                    res.addAll(handleGetField(p, p.getNode(), (SSAGetInstruction) defInst));
                }

                //5. defined by loadArray instruction
                else if (defInst instanceof SSAArrayLoadInstruction) {
                    res.addAll(handleArrayLoadInstruction(p, p.getNode(), (SSAArrayLoadInstruction) defInst));
//                Assertions.UNREACHABLE("Impossible to define a variable through this instruction: " + defInst);
                }

                //6. defined by cast instruction
                else if (defInst instanceof SSACheckCastInstruction) {
                    res.addAll(handleCastInstruction(p, p.getNode(), (SSACheckCastInstruction) defInst));
                }

                //8. created in this node
                else if (defInst instanceof SSANewInstruction) {
                    res.addAll(handleNewInstruction(p, p.getNode(), (SSANewInstruction) defInst));
                } else {
                    System.err.println("Impossible for a reference: " + defInst);
//                Assertions.UNREACHABLE("Impossible to define a variable through this instruction: " + defInst);
                }
            }
        }catch(Exception e){
            System.err.println("3# Cannot build an IR for the node: " + p.getNode());
        }
        return res;
    }

    protected Set<Point> handleArgumentPassing(Point p, CGNode n, int v){
        Set<Point> res = new HashSet<>();

        Iterator<CGNode> iPred = cg.getPredNodes(n);

        while(iPred.hasNext()){
            CGNode pred = iPred.next();
            if(isLibrary(pred))
                continue;

            Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, n);
            while(iCsr.hasNext()){
                CallSiteReference csr = iCsr.next();

                try {
                    for (SSAAbstractInvokeInstruction invokeInst : cache.makeIR(pred).getCalls(csr)) {
                        res.add(new Point(pred, invokeInst.iindex, invokeInst.getUse(v - 1), invokeInst, p.getWork().clone()));
                        finalResult.add(new Point(pred, invokeInst.iindex, invokeInst.getUse(v - 1), invokeInst, p.getWork().clone()));
                    }
                }catch(NullPointerException e){
                    System.err.println("4# Cannot build an IR for the node: " + pred);
                }
            }
        }

        return res;
    }

    protected Set<Point> handleMethodCall(Point p, CGNode n, SSAAbstractInvokeInstruction invokeInstruction){
        Set<Point> res = new HashSet<>();

        for(CGNode target : cg.getPossibleTargets(n, invokeInstruction.getCallSite())){
            if(isLibrary(target))
                continue;

            for (SSAReturnInstruction returnInst : findReturnInstructions(target)) {
                res.add(new Point(target, returnInst.iindex, returnInst.getUse(0), returnInst, p.getWork().clone()));
            }
        }

        return res;
    }

    protected Set<Point> handleNewInstruction(Point p, CGNode n, SSANewInstruction newInstruction){
        Set<Point> res = new HashSet<>();
//
////        System.out.println("=> " + newInstruction + " & type: " + type);
////        System.out.println("POSSIBLE? " + isPossibleType(newInstruction.getConcreteType(), type));
////        if(isPossibleType(newInstruction.getConcreteType(), type))
//        finalResult.add(Point.make(n, newInstruction.iindex, newInstruction.getDef(), newInstruction, origin));

        return res;
    }

    protected Set<Point> handleCastInstruction(Point p, CGNode n, SSACheckCastInstruction castInstruction){
        Set<Point> res = new HashSet<>();

        res.add(new Point(n, castInstruction.iindex, castInstruction.getUse(0), castInstruction, p.getWork().clone()));
        finalResult.add(new Point(n, castInstruction.iindex, castInstruction.getUse(0), castInstruction, p.getWork().clone()));

        return res;
    }

    protected Set<Point> handlePhiInstruction(Point p, CGNode n, SSAPhiInstruction phiInstruction){
        Set<Point> phi = new HashSet<>();

        for(int i = 0; i < phiInstruction.getNumberOfUses(); i++){
            if(phiInstruction.getUse(i) == -1){
                System.err.println("Cannot solve a variable used in the Phi instruction: " + phiInstruction);
            }else {
                phi.add(new Point(n, phiInstruction.iindex, phiInstruction.getUse(i), phiInstruction, p.getWork().clone()));
            }
        }
        return phi;
    }

//    String indent = "";

    protected Set<Point> handleArrayLoadInstruction(Point p, CGNode n, SSAArrayLoadInstruction arrLoadInstruction){
        Set<Point> res = new HashSet<>();

        if(arrLoadInstruction.getDef() == p.getTrackingVar()){
            res.add(new Point(n, arrLoadInstruction.iindex, arrLoadInstruction.getArrayRef(), arrLoadInstruction, new ForwardDataflowAnalysisUsingDefUse.Work(p.getWork().getInfo(), p.getWork().clone(), arrLoadInstruction.getArrayRef(), new ForwardDataflowAnalysisUsingDefUse.WorkVisitor() {
                @Override
                public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                    if(inst instanceof SSAArrayLoadInstruction && ((SSAArrayLoadInstruction)inst).getArrayRef() == targetV){
                        return true;
                    }
                    return false;
                }

                @Override
                public String toString() {
                    return "ARRAYLOAD_WORK";
                }
            })));

            finalResult.add(new Point(n, arrLoadInstruction.iindex, arrLoadInstruction.getArrayRef(), arrLoadInstruction, new ForwardDataflowAnalysisUsingDefUse.Work(p.getWork().getInfo(), p.getWork().clone(), arrLoadInstruction.getArrayRef(), new ForwardDataflowAnalysisUsingDefUse.WorkVisitor() {
                @Override
                public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                    if(inst instanceof SSAArrayLoadInstruction && ((SSAArrayLoadInstruction)inst).getArrayRef() == targetV){
                        return true;
                    }
                    return false;
                }

                @Override
                public String toString() {
                    return "ARRAYLOAD_WORK";
                }
            })));
        }else{
            Assertions.UNREACHABLE("Impossible in " + p + " in " + arrLoadInstruction);
        }

        return res;
    }

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

    protected Set<Point> handleGetField(Point p, CGNode n, SSAGetInstruction getInstruction){
        Set<Point> res = new HashSet<>();

        if(getInstruction.isStatic()){
            FieldReference field = getInstruction.getDeclaredField();
            System.err.println("We do not treat static field: " + p + " in " + getInstruction);
//            for(Pair<CGNode,SSAGetInstruction> pair : findAllGetInstsForStaticField(field)) {
//                res.add(new Point(pair.fst, pair.snd.iindex, pair.snd.getDef(), pair.snd, p.getWork().clone()));
//                finalResult.add(new Point(pair.fst, pair.snd.iindex, pair.snd.getDef(), pair.snd, p.getWork().clone()));
//            }
//
//            for(Pair<CGNode,SSAPutInstruction> pair : findAllPutInstsForStaticField(field)) {
//                res.add(new Point(pair.fst, pair.snd.iindex, pair.snd.getVal(), pair.snd, p.getWork().clone()));
//                finalResult.add(new Point(pair.fst, pair.snd.iindex, pair.snd.getDef(), pair.snd, p.getWork().clone()));
//            }
        }else if(getInstruction.getDef() == p.getTrackingVar()){
            FieldReference field = getInstruction.getDeclaredField();

            res.add(new Point(n, getInstruction.iindex, getInstruction.getRef(), getInstruction, new ForwardDataflowAnalysisUsingDefUse.Work(p.getWork().getInfo(), p.getWork().clone(), getInstruction.getRef(), new ForwardDataflowAnalysisUsingDefUse.WorkVisitor() {
                @Override
                public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                    if(inst instanceof SSAGetInstruction && ((SSAGetInstruction)inst).getRef() == getInstruction.getRef() && ((SSAGetInstruction)inst).getDeclaredField().equals(field)){
                        return true;
                    }
                    return false;
                }

                @Override
                public String toString() {
                    return "GET_WORK";
                }
            })));

            finalResult.add(new Point(n, getInstruction.iindex, getInstruction.getRef(), getInstruction, new ForwardDataflowAnalysisUsingDefUse.Work(p.getWork().getInfo(), p.getWork().clone(), getInstruction.getRef(), new ForwardDataflowAnalysisUsingDefUse.WorkVisitor() {
                @Override
                public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                    if(inst instanceof SSAGetInstruction && ((SSAGetInstruction)inst).getRef() == getInstruction.getRef() && ((SSAGetInstruction)inst).getDeclaredField().equals(field)){
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

        return res;
    }

    private Set<SSAReturnInstruction> findReturnInstructions(CGNode n){
        Set<SSAReturnInstruction> returnInsts = new HashSet<>();
        try {
            for (SSAInstruction inst : cache.makeIR(n).getInstructions()) {
                if (inst instanceof SSAReturnInstruction)
                    returnInsts.add((SSAReturnInstruction) inst);
            }
        }catch(Exception e){
            System.err.println("Cannot build IR for the node: " + n);
        }
        return returnInsts;
    }

    public Set<Point> analyze(){
        Set<Point> res = new HashSet<>();
        if(seeds.isEmpty()){
            System.err.println("Do not set any seeds!");
            return Collections.emptySet();
        }

        for(Point p : seeds){
            res.addAll(trackBackward(p));
        }
        return res;
    }

    private boolean isPossibleType(TypeReference original, TypeReference next){
        IClassHierarchy cha = cg.getClassHierarchy();

        IClass originalClass = cha.lookupClass(original);
        IClass nextClass = cha.lookupClass(next);

        if(originalClass != null && nextClass != null && (originalClass.equals(nextClass) || cha.isSubclassOf(originalClass, nextClass) || cha.implementsInterface(originalClass, nextClass))) {
            return true;
        }

//        System.out.println("ORI: " + originalClass);
//        System.out.println("NEXT: " + nextClass);
//        System.out.println("---");
        return false;
    }

    public void addSeeds(Point p) {
        this.seeds.add(p);
    }

    public void addSeeds(Set<Point> ps) {
        this.seeds.addAll(ps);
    }
//    static class DataflowResult {
//        private final Point seed;
//        private final Set<Point> creation;
//
//        public DataflowResult(Point seed, Set<Point> creation){
//            this.seed = seed;
//            this.creation = creation;
//        }
//
//        public Point getSeed(){
//            return this.seed;
//        }
//
//        public Set<Point> getCreationPoints(){
//            return this.creation;
//        }
//
//        @Override
//        public String toString(){
//            String res = "#Seed: " + seed;
//            Iterator<Point> iP = creation.iterator();
//            while(iP.hasNext())
//                res += "\n=> " + iP.next();
//            return res;
//        }
//    }
}
