package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.debug.Assertions;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by leesh on 25/04/2017.
 */
public class BackwardDataflowUsingDefUse {

    private final CallGraph cg;
    private final CHACache cache;
    private final Set<Point> seeds = new HashSet<>();

    public BackwardDataflowUsingDefUse(CallGraph cg, CHACache cache){
        this.cg = cg;
        this.cache = cache;
    }

    protected Set<Point> trackBackward(Point p){
        Set<Point> res = new HashSet<>();
        Set<Point> visited = new HashSet<>();
        Queue<Point> queue = new LinkedBlockingQueue<>();
        queue.add(p);

        while(!queue.isEmpty()){
            Point point = queue.poll();
            if(visited.contains(point))
                continue;
            visited.add(point);
            queue.addAll(getPredPoints(point, res));
        }

        return res;
    }

    protected Set<Point> getPredPoints(Point p, Set<Point> finalResult){
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
        if(p.getIndex() < 0 || p.getTrackingVar() != p.getinstruction(cache).getDef()) {
            //1. defined as argument
            if (p.getTrackingVar() <= p.getNode().getMethod().getNumberOfParameters()) {
                res.addAll(handleArgumentPassing(p.getNode(), p.getTrackingVar()));
            }
            //2. defined by null
            else if (cache.makeSymbolTable(p.getNode()).isNullConstant(p.getTrackingVar())) {
                //no-op
            }

            // find def point
            else {
                DefUse du = cache.makeDefUse(p.getNode());
                SSAInstruction defInst = du.getDef(p.getTrackingVar());

                // 7. defined by Phi instruction
                if(defInst instanceof SSAPhiInstruction){
                    /*
                    In this case, we only change the tracking vars with them of phi instruction.
                    The index of phi instruction is smaller than zero, these points are also tracked by this method.
                     */
                    res.addAll(handlePhiInstruction(p.getNode(), (SSAPhiInstruction) defInst));
                }else
                    res.add(new Point(p.getNode(), defInst.iindex, p.getTrackingVar()));
            }
        }
        // for 3, 4, 5, 6 or 8 cases
        else{
            SSAInstruction defInst = p.getinstruction(cache);

            //3. defined by return value of method call
            if(defInst instanceof SSAAbstractInvokeInstruction){
                res.addAll(handleMethodCall(p.getNode(), (SSAAbstractInvokeInstruction) defInst));
            }
            //4. defined by getField instruction
            else if(defInst instanceof SSAGetInstruction){
                Assertions.UNREACHABLE("Impossible to define a variable through this instruction: " + defInst);
            }

            //5. defined by loadArray instruction
            else if(defInst instanceof SSAArrayLoadInstruction){
                res.addAll(handleArrayLoadInstruction(p.getNode(), (SSAArrayLoadInstruction) defInst));
//                Assertions.UNREACHABLE("Impossible to define a variable through this instruction: " + defInst);
            }

            //6. defined by cast instruction
            else if(defInst instanceof SSACheckCastInstruction){
                res.addAll(handleCastInstruction(p.getNode(), (SSACheckCastInstruction) defInst));
            }

            //8. created in this node
            else if(defInst instanceof SSANewInstruction){
                res.addAll(handleNewInstruction(p.getNode(), (SSANewInstruction) defInst, finalResult));
            }

            else{
                Assertions.UNREACHABLE("Impossible to define a variable through this instruction: " + defInst);
            }
        }

        return res;
    }

    protected Set<Point> handleArgumentPassing(CGNode n, int v){
        Set<Point> res = new HashSet<>();

        Iterator<CGNode> iPred = cg.getPredNodes(n);

        while(iPred.hasNext()){
            CGNode pred = iPred.next();
            if(isLibrary(pred))
                continue;

            Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, n);
            while(iCsr.hasNext()){
                CallSiteReference csr = iCsr.next();

                for(SSAAbstractInvokeInstruction invokeInst : cache.makeIR(pred).getCalls(csr)){
                    res.add(new Point(pred, invokeInst.iindex, invokeInst.getUse(v-1)));
                }
            }
        }

        return res;
    }

    protected Set<Point> handleMethodCall(CGNode n, SSAAbstractInvokeInstruction invokeInstruction){
        Set<Point> res = new HashSet<>();

        for(CGNode target : cg.getPossibleTargets(n, invokeInstruction.getCallSite())){
            if(isLibrary(target))
                continue;
            for(SSAReturnInstruction returnInst : findReturnInstructions(target)){
                res.add(new Point(target, returnInst.iindex, returnInst.getUse(0)));
            }
        }

        return res;
    }

    protected Set<Point> handleNewInstruction(CGNode n, SSANewInstruction newInstruction, Set<Point> finalResult){
        Set<Point> res = new HashSet<>();

        finalResult.add(new Point(n, newInstruction.iindex, newInstruction.getDef()));

        return res;
    }

    protected Set<Point> handleCastInstruction(CGNode n, SSACheckCastInstruction castInstruction){
        Set<Point> res = new HashSet<>();

        res.add(new Point(n, castInstruction.iindex, castInstruction.getUse(0)));

        return res;
    }

    protected Set<Point> handlePhiInstruction(CGNode n, SSAPhiInstruction phiInstruction){
        Set<Point> phi = new HashSet<>();

        for(int i = 0; i < phiInstruction.getNumberOfUses(); i++){
            if(phiInstruction.getUse(i) == -1){
                System.err.println("Cannot solve a variable used in the Phi instruction: " + phiInstruction);
            }else {
                phi.add(new Point(n, phiInstruction.iindex, phiInstruction.getUse(i)));
            }
        }
        return phi;
    }

    protected Set<Point> handleArrayLoadInstruction(CGNode n, SSAArrayLoadInstruction arrLoadInstruction){
        Set<Point> arrayCreationPoint = trackBackward(new Point(n, arrLoadInstruction.iindex, arrLoadInstruction.getArrayRef()));
        for(Point p : arrayCreationPoint){

        }

        Set<Point> phi = new HashSet<>();

        return phi;
    }
    private Set<SSAReturnInstruction> findReturnInstructions(CGNode n){
        Set<SSAReturnInstruction> returnInsts = new HashSet<>();
        for(SSAInstruction inst : cache.makeIR(n).getInstructions()){
            if(inst instanceof SSAReturnInstruction)
                returnInsts.add((SSAReturnInstruction) inst);
        }
        return returnInsts;
    }

    public Set<DataflowResult> analyze(){
        Set<DataflowResult> res = new HashSet<>();
        if(seeds.isEmpty()){
            System.err.println("Do not set any seeds!");
            return Collections.emptySet();
        }

        for(Point p : seeds){
            res.add(new DataflowResult(p, trackBackward(p)));
        }
        return res;
    }

    public void addSeeds(CGNode n, int argPosition){
        Iterator<CGNode> iPred = cg.getPredNodes(n);

        while(iPred.hasNext()){
            CGNode pred = iPred.next();

            //we do not consider library a target
            if(isLibrary(pred)) {
                continue;
            }

            Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, n);

            while(iCsr.hasNext()){
                CallSiteReference csr = iCsr.next();
                IR ir = cache.makeIR(pred);
                for(SSAAbstractInvokeInstruction invokeInst : ir.getCalls(csr)){
                    if(invokeInst.isStatic())
                        seeds.add(new Point(pred, invokeInst.iindex, invokeInst.getUse(argPosition-1)));
                    else
                        seeds.add(new Point(pred, invokeInst.iindex, invokeInst.getUse(argPosition)));
                }
            }
        }
    }

    public void addSeeds(Set<CGNode> nodes, int argPosition){
        for(CGNode n : nodes) {
            addSeeds(n, argPosition);
        }
    }

    private boolean isLibrary(CGNode n){
        if(n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v4"))
            return true;
        else if(n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v7"))
            return true;
        else if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return true;
        return false;
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
