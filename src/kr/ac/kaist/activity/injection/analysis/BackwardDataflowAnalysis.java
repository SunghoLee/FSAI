//package kr.ac.kaist.activity.injection.analysis;
//
//import com.ibm.wala.classLoader.CallSiteReference;
//import com.ibm.wala.dalvik.classLoader.DexIRFactory;
//import com.ibm.wala.ipa.callgraph.CGNode;
//import com.ibm.wala.ipa.callgraph.CallGraph;
//import com.ibm.wala.ipa.callgraph.impl.Everywhere;
//import com.ibm.wala.ssa.*;
//import com.ibm.wala.types.ClassLoaderReference;
//import com.ibm.wala.types.TypeReference;
//import com.ibm.wala.util.debug.Assertions;
//
//import java.util.*;
//import java.util.concurrent.LinkedBlockingQueue;
//
///**
// * Created by leesh on 24/04/2017.
// */
//public class BackwardDataflowAnalysis {
//
//    public enum ScopeOption{
//        APP_ONLY,
//        TOTAL,
//    };
//
//    private final CallGraph cg;
//    private final ScopeOption scope;
//    private Set<Point> seeds = new HashSet<>();
//
//    public BackwardDataflowAnalysis(CallGraph cg, ScopeOption scope){
//        this.cg = cg;
//        this.scope = scope;
//    }
//
//
//    public DataflowResult<Point> analyze(IDataflowSemanticFunction f){
//        DataflowResult<Point> res = new DataflowResult<>();
//        WorkList workList = new WorkList();
//
//        if(seeds.isEmpty())
//            System.err.println("Do not set seeds!");
//
//        workList.addAll(seeds);
//
//        Set<Point> visited = new HashSet<>();
//
//        while(!workList.isEmpty()){
//            Point p = workList.poll();
//            visited.add(p);
//
//            for(IDataflowSemanticFunction.Path path : f.visit(p.getNode(), p.getInstruction(), p.getVar())){
//                switch(path.getType()){
//                    case TRACK:
//                        workList.addAll(trackPrimitive(p.getNode(), p.getInstruction(), p.getIndex(), path.getVars()));
//                    case END:
//                        break;
//                    default:
//                        Assertions.UNREACHABLE("Do not treat all paths! " + path);
//                }
//            }
//        }
//
//        return res;
//    }
//
//    private Set<Point> track(CGNode n, SSAInstruction inst, int iindex, int v, Point.TrackingType t){
//        Set<Point> res = new HashSet<>();
//
//        if(inst instanceof SSAEntryInstruction){
//
//            if(n.getMethod().getNumberOfParameters() < v)
//                Assertions.UNREACHABLE("Impossible flow: " + v + " in " + n);
//
//            Iterator<CGNode> iPred = cg.getPredNodes(n);
//            while(iPred.hasNext()){
//                CGNode pred = iPred.next();
//
//                switch(scope){
//                    case APP_ONLY:
//                        if(!pred.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application))
//                            break;
//                    case TOTAL:
//                        Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, n);
//                        while(iCsr.hasNext()){
//                            CallSiteReference csr = iCsr.next();
//                            for(SSAAbstractInvokeInstruction invokeInst : makeIR(pred).getCalls(csr)){
//                                res.add(new Point(pred, invokeInst.iindex, invokeInst.getUse(v-1), t));
//                            }
//                        }
//                        break;
//                    default:
//                        Assertions.UNREACHABLE("Do not treat all scope options here: " + scope);
//                }
//            }
//        }else if(inst instanceof SSAGetInstruction){
//
//        }else if(inst instanceof SSAArrayLoadInstruction){
//            SSAArrayLoadInstruction arrayLoadInst = (SSAArrayLoadInstruction) inst;
//            int ref = arrayLoadInst.getArrayRef();
//            if(ref == v){
////                res.addAll(track)
//            }
//        }else{
//            switch(t){
//                case PRMITIVE:
//                    trackPrimitive(n, iindex, v, t);
//                    break;
//                case REFERENCE:
//                    break;
//                default:
//                    Assertions.UNREACHABLE("Do not treat all tracking types! " + t.getDeclaringClass());
//            }
//        }
//
//        return res;
//    }
//
//    private Set<Point> trackPrimitive(CGNode n, int iindex, int v, Point.TrackingType t){
//        Set<Point> res = new HashSet<>();
//
//        if(n.getMethod().getNumberOfParameters() >= v){
//            res.add(new Point())
//        }else{
////            SSAInstruction inst = makeDefUse(n).getDef(v);
//        }
//
//        return res;
//    }
//
//    protected void addAnalysisSeed(CGNode seed, int argPosition){
//        seeds.addAll(findCallsites(seed, argPosition));
//    }
//
//    protected Set<Point> findCallsites(CGNode n, int pos){
//        Set<Point> res = new HashSet<>();
//
//        Iterator<CGNode> iPred = cg.getPredNodes(n);
//        while(iPred.hasNext()){
//            CGNode pred = iPred.next();
//
//            switch(scope){
//                case APP_ONLY:
//                    if(!pred.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application))
//                        break;
//                case TOTAL:
//                    Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, n);
//                    while(iCsr.hasNext()){
//                        CallSiteReference csr = iCsr.next();
//                        for(SSAAbstractInvokeInstruction invokeInst : makeIR(pred).getCalls(csr)){
//                            int arg = (invokeInst.isStatic())? invokeInst.getUse(pos - 1) : invokeInst.getUse(pos);
//                            if(isPrimitive(pred.getMethod().getParameterType(pos)))
//                                res.add(new Point(pred, invokeInst.iindex, arg, Point.TrackingType.PRMITIVE));
//                            else
//                                res.add(new Point(pred, invokeInst.iindex, arg, Point.TrackingType.REFERENCE));
//                        }
//                    }
//                    break;
//                default:
//                    Assertions.UNREACHABLE("Do not treat all scope options here: " + scope);
//            }
//        }
//
//        return res;
//    }
//
//    private boolean isPrimitive(TypeReference tr){
//        return TypeReference.isPrimitiveType(tr.getName());
//    }
//
//    static private Map<CGNode, IR> irCache = new HashMap<>();
//    static private Map<CGNode, DefUse> duCache = new HashMap<>();
//    static public IR makeIR(CGNode n){
//        if(irCache.keySet().contains(n))
//            return irCache.get(n);
//
//        DexIRFactory irFactory = new DexIRFactory();
//        IR ir = irFactory.makeIR(n.getMethod(), Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
//        irCache.put(n, ir);
//        return ir;
//    }
//
//    static public DefUse makeDefUse(CGNode n){
//        if(duCache.keySet().contains(n))
//            return duCache.get(n);
//
//        DefUse du = new DefUse(makeIR(n));
//        duCache.put(n, du);
//        return du;
//    }
//
//    static public SymbolTable makeSymbolTable(CGNode n){
//        return makeIR(n).getSymbolTable();
//    }
//
//    static class Point {
//        enum TrackingType{
//            PRMITIVE,
//            REFERENCE,
//        }
//
//        private final CGNode node;
//        private final int iindex;
//        private final int v;
//        private final TrackingType t;
//        private final SSAInstruction inst;
//
//        public Point(CGNode n, int iindex, SSAInstruction inst, int v, TrackingType t){
//            this.node = n;
//            this.iindex = iindex;
//            this.v = v;
//            this.t = t;
//            this.inst = inst;
//        }
//
//        public SSAInstruction getInstruction(){
//            if(iindex == -2)
//                return new SSAEntryInstruction(v);
//            else if(iindex == -3)
//                return new SSAExitInstruction(v);
//            else if(iindex == SSAInstruction.NO_INDEX)
//                return null;
//
//            return makeIR(node).getInstructions()[iindex];
//        }
//
//        public CGNode getNode(){
//            return this.node;
//        }
//
//        public int getIndex(){
//            return this.iindex;
//        }
//
//        public int getVar(){
//            return this.v;
//        }
//
//        public TrackingType getTrackingType(){
//            return this.t;
//        }
//
//        @Override
//        public int hashCode(){
//            return node.hashCode() * iindex + v;
//        }
//
//        @Override
//        public boolean equals(Object o){
//            if(o instanceof Point){
//                Point p = (Point) o;
//                if(p.node.equals(this.node) && p.iindex == this.iindex && p.v == this.v)
//                    return true;
//            }
//            return false;
//        }
//
//        @Override
//        public String toString(){
//            return node + " # " + getInstruction() + " # " + v + " [ " + ((t.equals(TrackingType.PRMITIVE))?"PRIV" : "REF") + " ]";
//        }
//    }
//
//    class WorkList extends LinkedBlockingQueue<Point> {
//        public boolean add(CGNode n, int iindex, int v, Point.TrackingType t){
//            return this.add(new Point(n, iindex, v, t));
//        }
//    }
//
//    final static class SSAEntryInstruction extends SSAInstruction{
//
//        private final int var;
//
//        protected SSAEntryInstruction(int var) {
//            super(-2);
//            this.var = var;
//        }
//
//        @Override
//        public SSAInstruction copyForSSA(SSAInstructionFactory insts, int[] defs, int[] uses) {
//            return null;
//        }
//
//        @Override
//        public String toString(SymbolTable symbolTable) {
//            return "ENTRY: " + var;
//        }
//
//        @Override
//        public void visit(IVisitor v) {
//
//        }
//
//        @Override
//        public int hashCode() {
//            return var;
//        }
//
//        @Override
//        public boolean isFallThrough() {
//            return false;
//        }
//
//        @Override
//        public int getNumberOfUses() {
//            return 1;
//        }
//
//        @Override
//        public int getUse(int j) throws UnsupportedOperationException {
//            return (j == 0)? this.var : -1;
//        }
//    }
//
//    final static class SSAExitInstruction extends SSAInstruction{
//
//        private final int var;
//
//        protected SSAExitInstruction(int var) {
//            super(-3);
//            this.var = var;
//        }
//
//        @Override
//        public SSAInstruction copyForSSA(SSAInstructionFactory insts, int[] defs, int[] uses) {
//            return null;
//        }
//
//        @Override
//        public String toString(SymbolTable symbolTable) {
//            return "ENTRY: " + var;
//        }
//
//        @Override
//        public void visit(IVisitor v) {
//
//        }
//
//        @Override
//        public int hashCode() {
//            return var;
//        }
//
//        @Override
//        public boolean isFallThrough() {
//            return false;
//        }
//
//        @Override
//        public int getNumberOfUses() {
//            return 1;
//        }
//
//        @Override
//        public int getUse(int j) throws UnsupportedOperationException {
//            return (j == 0)? this.var : -1;
//        }
//    }
//}
