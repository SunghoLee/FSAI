//package kr.ac.kaist.activity.injection.analysis;
//
//import com.ibm.wala.classLoader.CallSiteReference;
//import com.ibm.wala.classLoader.IClass;
//import com.ibm.wala.dalvik.classLoader.DexIRFactory;
//import com.ibm.wala.ipa.callgraph.CGNode;
//import com.ibm.wala.ipa.callgraph.CallGraph;
//import com.ibm.wala.ipa.callgraph.impl.Everywhere;
//import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
//import com.ibm.wala.ssa.*;
//import com.ibm.wala.types.ClassLoaderReference;
//import com.ibm.wala.types.MethodReference;
//import com.ibm.wala.types.Selector;
//import com.ibm.wala.types.TypeReference;
//import com.ibm.wala.util.collections.Pair;
//import com.ibm.wala.util.debug.Assertions;
//import com.ibm.wala.util.debug.UnimplementedError;
//import kr.ac.kaist.activity.injection.types.ComponentName;
//import kr.ac.kaist.activity.injection.types.Intent;
//
//import java.util.*;
//
//import static com.ibm.wala.util.debug.Assertions.UNREACHABLE;
//
///**
// * Created by leesh on 01/02/2017.
// */
//public class CallingComponentAnalysis {
//    private final CallGraph cg;
//    public final static TypeReference CONTEXT_WRAPPER = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/content/ContextWrapper");
//
//    public enum CallingMethod{
//        START_ACTIVITY1("startActivity(Landroid/content/Intent;)V"),
//        START_ACTIVITY2("startActivity(Landroid/content/Intent;Landroid/os/Bundle;)V"),
////        START_ACTIVITY3("startActivities([Landroid/content/Intent;)V"),
////        START_ACTIVITY4("startActivities([Landroid/content/Intent;Landroid/os/Bundle;)V"),
////        START_SERVICE("startService(Landroid/content/Intent;)V"),
//        ;
//
//        private final Selector selector;
//
//        CallingMethod(String s){
//            this.selector = Selector.make(s);
//        }
//
//        public Selector getSelector(){
//            return selector;
//        }
//
//        public static CallingMethod matchMethod(Selector s){
//            for(CallingMethod c : CallingMethod.values()){
//                if(c.getSelector().equals(s))
//                    return c;
//            }
//
//            return null;
//        }
//    }
//
//    private void test(){
////        for(CGNode n : cg){
////            if(n.toString().contains("Node: < Application, Lcom/smaato/soma/bannerutilities/AbstractBannerPackage$HtmlGetterJSInterface$1$1$1, process()Ljava/lang/Void; > Context: Everywhere")){
////                PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n  ,1);
////                System.out.println("PK: " + pk);
////                for(InstanceKey ik : pa.getPointsToSet(pk))
////                    System.out.println("\tIK: " + ik);
////                pk = pa.getHeapModel().getPointerKeyForLocal(n  ,40);
////                System.out.println("PK: " + pk);
////                for(InstanceKey ik : pa.getPointsToSet(pk))
////                    System.out.println("\tIK: " + ik);
////                pk = pa.getHeapModel().getPointerKeyForLocal(n  ,41);
////                System.out.println("PK: " + pk);
////                for(InstanceKey ik : pa.getPointsToSet(pk))
////                    System.out.println("\tIK: " + ik);
////                pk = pa.getHeapModel().getPointerKeyForLocal(n  ,42);
////                System.out.println("PK: " + pk);
////                for(InstanceKey ik : pa.getPointsToSet(pk))
////                    System.out.println("\tIK: " + ik);
////                pk = pa.getHeapModel().getPointerKeyForLocal(n  ,43);
////                System.out.println("PK: " + pk);
////                for(InstanceKey ik : pa.getPointsToSet(pk))
////                    System.out.println("\tIK: " + ik);
////                pk = pa.getHeapModel().getPointerKeyForLocal(n  ,45);
////                System.out.println("PK: " + pk);
////                for(InstanceKey ik : pa.getPointsToSet(pk))
////                    System.out.println("\tIK: " + ik);
////            }
////        }
//    }
//
//    public CallingComponentAnalysis(CallGraph cg){
//        this.cg = cg;
//        test();
//    }
//
//    private Set<CGNode> findCallingComponentNode(CallGraph cg, IClass klass){
//        Set<CGNode> res = new HashSet<CGNode>();
//
//        if(klass == null)
//            return res;
//
//        for(CallingMethod cm : CallingMethod.values()){
//            res.addAll(cg.getNodes(MethodReference.findOrCreate(klass.getReference(), cm.getSelector())));
//        }
//        for(IClass subK : cg.getClassHierarchy().getImmediateSubclasses(klass)){
//            res.addAll(findCallingComponentNode(cg, subK));
//        }
//
//        return res;
//    }
//
//    private Set<CGNode> getPreds(CallGraph cg, CGNode n){
//        Iterator<CGNode> iNode = cg.getPredNodes(n);
//        Set<CGNode> nodes = new HashSet<CGNode>();
//
//        while(iNode.hasNext())
//            nodes.add(iNode.next());
//
//        return nodes;
//    }
//
//    private Map<CGNode, IR> irCache = new HashMap<>();
//    private Map<CGNode, DefUse> duCache = new HashMap<>();
//
//    private IR makeIR(CGNode n){
//        if(irCache.keySet().contains(n))
//            return irCache.get(n);
//
//        DexIRFactory irFactory = new DexIRFactory();
//        IR ir = irFactory.makeIR(n.getMethod(), Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
//        irCache.put(n, ir);
//        return ir;
//    }
//
//    private DefUse makeDefUse(CGNode n){
//        if(duCache.keySet().contains(n))
//            return duCache.get(n);
//
//        DefUse du = new DefUse(makeIR(n));
//        duCache.put(n, du);
//        return du;
//    }
//
//    public static class ComponentCallingContext {
//        private CGNode originNode;
//        private SSAInstruction originInst;
//        private String action;
//        private String category;
//        private final Set<String> flags = new HashSet<>();
//        private final Set<ConstantKey> targets;
//
//        public ComponentCallingContext(CGNode originNode, SSAInstruction originInst){
//            this.action = "Unknown";
//            this.category = "Unknown";
//            this.originNode = originNode;
//            this.originInst = originInst;
//            targets = new HashSet<>();
//        }
//
//        public ComponentCallingContext(CGNode originNode, SSAInstruction originInst, String action){
//            this.action = action;
//            this.category = "Unknown";
//            this.originNode = originNode;
//            this.originInst = originInst;
//            targets = new HashSet<>();
//        }
//
//        public ComponentCallingContext(String action, String category){
//            this.action = action;
//            this.category = category;
//            targets = new HashSet<>();
//        }
//
//        public ComponentCallingContext(CGNode originNode, SSAInstruction originInst, ConstantKey target){
//            this(originNode, originInst);
//            targets.add(target);
//        }
//
//        public void addTarget(ConstantKey target){
//            this.targets.add(target);
//        }
//
//        public void addFlag(String s){
//            flags.add(s);
//        }
//
//        @Override
//        public String toString(){
//            String res = (originNode == null)? "" : originNode.toString() + "\n";
//            res += (originInst == null)? "" : "\tInstruction: " + originInst + "\n";
//            res += ((res.length() > 1)? "\t\t" : "") + "Intent[ action: " + action + ", category: " + category + ", flags: " + flags + ", target: ";
//
//            if(targets.isEmpty()){
//                res += "Unknown ]";
//            }else{
//                boolean first = true;
//                for(ConstantKey ck : targets){
//                    if(!first)
//                        res += ", ";
//                    res += ck.getValue();
//                    first = false;
//                }
//                res += " ]";
//            }
//            return res;
//        }
//    }
//
//    private Set<Pair> visited = new HashSet<>();
//
//    private boolean visited(CGNode n, int intentVar){
//        Pair<CGNode, Integer> p = Pair.make(n, intentVar);
//        if(visited.contains(p))
//            return true;
//        return !(visited.add(p));
//    }
//
//    private Map<Pair, Set<ComponentCallingContext>> cache = new HashMap<>();
//
//    private Set<ComponentCallingContext> findCache(CGNode n, int intentVar){
//        Pair<CGNode, Integer> p = Pair.make(n, intentVar);
//        if(cache.containsKey(p))
//            return cache.get(p);
//        return Collections.EMPTY_SET;
//    }
//
//    private void caching(CGNode n, int intentVar, Set<ComponentCallingContext> ccc){
//        Pair<CGNode, Integer> p = Pair.make(n, intentVar);
//
//    }
//
//    private Set<ComponentCallingContext> getCallingContexts(CGNode originNode, SSAInstruction originInst, CGNode n, int intentVar){
//        if(visited(n, intentVar))
//            return Collections.EMPTY_SET;
//
//        Set<ComponentCallingContext> res = new HashSet<>();
//
//        /*
//        Only four cases possible
//        1. def by argument
//        2. def by new
//        3. def by return of method call
//        4. def by getField
//        5. def by arrayLoad
//         */
//
//        //def by argument
//        if(intentVar < n.getMethod().getNumberOfParameters() + 1){
//            for(CGNode pred: getPreds(cg, n)){ // for each caller node for this node
//                Iterator<CallSiteReference> iCallSite = cg.getPossibleSites(pred, n);
//                while(iCallSite.hasNext()){ // for each callsites for this node in the caller node
//                    CallSiteReference csRef = iCallSite.next();
//                    IR ir = makeIR(pred);
//                    for(SSAAbstractInvokeInstruction callInst : ir.getCalls(csRef)){ // for each call instruction for this node in caller node
//                        res.addAll(getCallingContexts(originNode, originInst, pred, callInst.getUse(intentVar - 1))); // 1 is a second argument that denotes intent object
//                    }
//                }
//            }
//        }else{//def by 2, 3, 4 or 5
//            //special case for null. this case is possible in Phi instruction, so it may not be a application bug.
//            IR ir = makeIR(n);
//            if(ir.getSymbolTable().isConstant(intentVar)){
//                //no-op: don't track anymore.
//            }else {
//
//                DefUse du = makeDefUse(n);
//
//                SSAInstruction defInst = du.getDef(intentVar);
//                IntentCreationTrackingVisitor visitor = new IntentCreationTrackingVisitor(originNode, originInst, n);
//                defInst.visit(visitor);
//                res.addAll(visitor.getResult());
//            }
//        }
//
//        addFlags(n, intentVar, res);
//        return res;
//    }
//
//    private Set<String> warnings = new HashSet<>();
//
//    public Set<String> getWarnings(){
//        return warnings;
//    }
//
//    private Set<ComponentCallingContext> addFlags(CGNode n, int intentVar, Set<ComponentCallingContext> res){
//        //NOTE: we don't find flags for an intent in Primordial method, because it is not developer's code.
//        if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
//            return Collections.emptySet();
//        DefUse du = makeDefUse(n);
//        Iterator<SSAInstruction> iInst = du.getUses(intentVar);
//        while(iInst.hasNext()){
//            SSAInstruction useInst = iInst.next();
//            if(useInst instanceof SSAAbstractInvokeInstruction){
//                for(CGNode callee : cg.getPossibleTargets(n, ((SSAAbstractInvokeInstruction)useInst).getCallSite())){
//                    MethodReference calleeRef = callee.getMethod().getReference();
//                    if(calleeRef.getDeclaringClass().getName().equals(Intent.INTENT_TYPE)){
//                        if(calleeRef.getSelector().equals(Intent.AddFlagsSelector.ADD_FLAGS.getSelector())){
//                            int flagVar = useInst.getUse(Intent.AddFlagsSelector.ADD_FLAGS.getFlagIndex());
//                            IR ir = makeIR(n);
//                            SymbolTable symTab = ir.getSymbolTable();
//                            if(symTab.isIntegerConstant(flagVar)){
//                                if(Intent.Flag.matchFlag(symTab.getIntValue(flagVar)) == null) {
//                                    warnings.add("Intent flag is unknown: v" + flagVar + "[#" + symTab.getIntValue(flagVar) + "] in " + n);
//                                    addFlagToAllIntent("UNKNOWN_FLAG", res);
//                                }else
//                                    addFlagToAllIntent(Intent.Flag.matchFlag(symTab.getIntValue(flagVar)).getName(), res);
//                            }else {
////                                throw new WrongTypeException("Intent flag must be Integer constant: " + flagVar + " in " + n);
//                                warnings.add("Intent flag must be Integer constant: " + flagVar + " in " + n);
//                            }
//                        }
//                    }else if(CallingMethod.matchMethod(calleeRef.getSelector()) != null){
//                        return res;
//                    }else{
//                        int i=0;
//                        for(; i < useInst.getNumberOfUses(); i++){
//                            if(useInst.getUse(i) == intentVar)
//                                break;
//                        }
//                        res = addFlags(callee, ++i, res);
//                    }
//                }
//            }else if(useInst instanceof SSAReturnInstruction){
////                Assertions.UNREACHABLE("Intent object can be used in invoke instruction or return instruction only: " + useInst);
//                //no-op
//            }else if(useInst instanceof SSAPhiInstruction) {
//                res = addFlags(n, useInst.getDef(), res);
//            }else if(useInst instanceof SSAConditionalBranchInstruction){
//                Assertions.UNREACHABLE("Intent object can be used in invoke instruction or return instruction only: " + useInst);
//                //NOTE: this case is possible, if the conditional branch instruction is null comparison.
//                //TODO: we should check which it is really null comparison or not.
//                //no-op
//            }else if(useInst instanceof SSAGetInstruction){
//                Assertions.UNREACHABLE("Intent object can be used in invoke instruction or return instruction only: " + useInst);
//                //NOTE: this case is possible, if the instruction is for getting an extra data of the intent.
//                //no-op
//            }else if(useInst instanceof SSACheckCastInstruction){
//                res = addFlags(n, useInst.getDef(), res);
////                Assertions.UNREACHABLE("Intent object can be used in invoke instruction or return instruction only: " + useInst);
//            }else {
//                Assertions.UNREACHABLE("Intent object can be used in invoke instruction or return instruction only: " + useInst);
//            }
//        }
//        return res;
//    }
//
//    private Set<ComponentCallingContext> addFlagToAllIntent(String s, Set<ComponentCallingContext> intents){
//        for(ComponentCallingContext ccc: intents)
//            ccc.addFlag(s);
//        return intents;
//    }
//
//    public Set<ComponentCallingContext> getCallingContexts(){
//        Set<CGNode> nodes = findCallingComponentNode(cg, cg.getClassHierarchy().lookupClass(CONTEXT_WRAPPER));
//        Set<ComponentCallingContext> res = new HashSet<>();
//
//        for(CGNode n : nodes){ // for each startActivity node
//            for(CGNode pred: getPreds(cg, n)){ // for each caller node for startActivity
//                if(pred.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) && !isLibrary(pred)) {
//                    Iterator<CallSiteReference> iCallSite = cg.getPossibleSites(pred, n);
//                    while (iCallSite.hasNext()) { // for each callsites for startActivity in the caller node
//                        CallSiteReference csRef = iCallSite.next();
//                        IR ir = makeIR(pred);
//                        for (SSAAbstractInvokeInstruction callInst : ir.getCalls(csRef)) { // for each call instruction for startActivity in caller node
//                            if(!callInst.isStatic()) {
//                                res.addAll(getCallingContexts(pred, callInst, pred, callInst.getUse(1))); // 1 is a second argument that denotes intent object
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return res;
//    }
//
//    private boolean isLibrary(CGNode n){
//        if(n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v4"))
//            return true;
//        else if(n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v7"))
//            return true;
//        return false;
//    }
//
//
//    private Set<ArrayCreationSite> trackArray(CGNode originNode, SSAInstruction originInst, CGNode n, int arrayVar){
//        if(visited(n, arrayVar))
//            return Collections.EMPTY_SET;
//
//        Set<ArrayCreationSite> res = new HashSet<>();
//
//        /*
//        Only four cases possible
//        1. def by argument
//        2. def by new
//        3. def by return of method call
//        4. def by getField
//        5. def by arrayLoad
//         */
//
//        //def by argument
//        if(arrayVar < n.getMethod().getNumberOfParameters() + 1){
//            for(CGNode pred: getPreds(cg, n)){ // for each caller node for this node
//                Iterator<CallSiteReference> iCallSite = cg.getPossibleSites(pred, n);
//                while(iCallSite.hasNext()){ // for each callsites for this node in the caller node
//                    CallSiteReference csRef = iCallSite.next();
//                    IR ir = makeIR(pred);
//                    for(SSAAbstractInvokeInstruction callInst : ir.getCalls(csRef)){ // for each call instruction for this node in caller node
//                        res.addAll(trackArray(originNode, originInst, pred, callInst.getUse(arrayVar - 1))); // 1 is a second argument that denotes intent object
//                    }
//                }
//            }
//        }else{//def by 2, 3, 4 or 5
//            //special case for null. this case is possible in Phi instruction, so it may not be a application bug.
//            IR ir = makeIR(n);
//            if(ir.getSymbolTable().isConstant(arrayVar)){
//                //no-op: don't track anymore.
//            }else {
//                DefUse du = makeDefUse(n);
//
//                SSAInstruction defInst = du.getDef(arrayVar);
//                ArrayCreationTrackingVisitor visitor = new ArrayCreationTrackingVisitor(originNode, originInst, n);
//                defInst.visit(visitor);
//                res.addAll(visitor.getResult());
//            }
//        }
//        return res;
//    }
//
//    class ArrayCreationSite {
//        private final CGNode n;
//        private final int iindex;
//
//        public ArrayCreationSite(CGNode n, int iindex){
//            this.n = n;
//            this.iindex = iindex;
//        }
//
//        public CGNode getNode(){
//            return this.n;
//        }
//
//        public int getIIndex(){
//            return this.iindex;
//        }
//
//        public SSANewInstruction getCreationInstruction(){
//            return (SSANewInstruction)makeIR(n).getInstructions()[iindex];
//        }
//
//        public int getDefVar(){
//            return getCreationInstruction().getDef();
//        }
//    }
//
//
//
//    class ArrayCreationTrackingVisitor implements SSAInstruction.IVisitor{
//        private boolean DEBUG = false;
//        private final Set<ArrayCreationSite> cccSet;
//        private final CGNode n;
//        private final CGNode originNode;
//        private final SSAInstruction originInst;
//
//        public ArrayCreationTrackingVisitor(CGNode originNode, SSAInstruction originInst, CGNode n){
//            this.originNode = originNode;
//            this.originInst = originInst;
//            this.cccSet = new HashSet<>();
//            this.n = n;
//        }
//
//        @Override
//        public void visitGoto(SSAGotoInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Goto instruction.");
//        }
//
//        @Override
//        public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//
//            trackArray(originNode, originInst, n, instruction.getArrayRef());
//        }
//
//        @Override
//        public void visitArrayStore(SSAArrayStoreInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Array store instruction.");
//        }
//
//        @Override
//        public void visitBinaryOp(SSABinaryOpInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Binary operation instruction.");
//        }
//
//        @Override
//        public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Unary operation instruction.");
//        }
//
//        @Override
//        public void visitConversion(SSAConversionInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Conversion instruction.");
//        }
//
//        @Override
//        public void visitComparison(SSAComparisonInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Comparison instruction.");
//        }
//
//        @Override
//        public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Conditional branch instruction.");
//        }
//
//        @Override
//        public void visitSwitch(SSASwitchInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Switch instruction.");
//        }
//
//        @Override
//        public void visitReturn(SSAReturnInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Return instruction.");
//        }
//
//        @Override
//        public void visitGet(SSAGetInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            warnings.add("Intent can not created by Get instruction." + instruction);
////            UNREACHABLE("Intent can not created by Get instruction.");
//        }
//
//        @Override
//        public void visitPut(SSAPutInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Put instruction.");
//        }
//
//        @Override
//        public void visitInvoke(SSAInvokeInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            for(CGNode callee : cg.getPossibleTargets(n, instruction.getCallSite())){
//                IR ir = makeIR(callee);
//                SSACFG cfg = ir.getControlFlowGraph();
//                SSACFG.BasicBlock exitBlock = ir.getExitBlock();
//                Iterator<ISSABasicBlock> iReturn = cfg.getPredNodes(exitBlock);
//                while(iReturn.hasNext()){
//                    ISSABasicBlock retBB = iReturn.next();
//                    SSAInstruction inst = retBB.getLastInstruction();
//                    if(inst instanceof SSAReturnInstruction){
//                        SSAReturnInstruction retInst = (SSAReturnInstruction) inst;
//                        if(retInst.getNumberOfUses() == 0)
//                            UNREACHABLE("Return block must have a return value: " + inst);
//                        int retVar = retInst.getUse(0);
//                        cccSet.addAll(trackArray(originNode, originInst, callee, retVar));
//                    }else {
////                        UNREACHABLE("All return blocks must have return instruction: " + inst);
////                        UNREACHABLE("All return blocks must have return instruction: " + inst);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void visitNew(SSANewInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            cccSet.add(new ArrayCreationSite(n, instruction.iindex));
//        }
//
//        @Override
//        public void visitArrayLength(SSAArrayLengthInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Array length instruction.");
//        }
//
//        @Override
//        public void visitThrow(SSAThrowInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Throw instruction.");
//        }
//
//        @Override
//        public void visitMonitor(SSAMonitorInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Monitor instruction.");
//        }
//
//        @Override
//        public void visitCheckCast(SSACheckCastInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Check cast instruction.");
//        }
//
//        @Override
//        public void visitInstanceof(SSAInstanceofInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Instance of instruction.");
//        }
//
//        @Override
//        public void visitPhi(SSAPhiInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            for(int i = 0; i < instruction.getNumberOfUses(); i++){
//                int useVar = instruction.getUse(i);
//                if(useVar == -1)
//                    System.out.println("Could not track the intent object in phi instruction: " + instruction);
//                cccSet.addAll(trackArray(originNode, originInst, n, useVar));
//            }
//
////            UNREACHABLE("Intent can not created by Phi instruction.");
//        }
//
//        @Override
//        public void visitPi(SSAPiInstruction instruction) {
//
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Pi instruction.");
//        }
//
//        @Override
//        public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Get caught instruction.");
//        }
//
//        @Override
//        public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Load metadata instruction.");
//        }
//
//        public Set<ArrayCreationSite> getResult(){
//            return cccSet;
//        }
//    }
//
//
//
//
//
//
//
//    class IntentCreationTrackingVisitor implements SSAInstruction.IVisitor{
//        private boolean DEBUG = false;
//        private final Set<ComponentCallingContext> cccSet;
//        private final CGNode n;
//        private final CGNode originNode;
//        private final SSAInstruction originInst;
//        public IntentCreationTrackingVisitor(CGNode originNode, SSAInstruction originInst, CGNode n){
//            this.originNode = originNode;
//            this.originInst = originInst;
//            this.cccSet = new HashSet<>();
//            this.n = n;
//        }
//
//        @Override
//        public void visitGoto(SSAGotoInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Goto instruction.");
//        }
//
//        @Override
//        public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//
//            for(CGNode callee : cg.getPossibleTargets(n, instruction.getCallSite())){
//                IR ir = makeIR(callee);
//                SSACFG cfg = ir.getControlFlowGraph();
//                SSACFG.BasicBlock exitBlock = ir.getExitBlock();
//                Iterator<ISSABasicBlock> iReturn = cfg.getPredNodes(exitBlock);
//                while(iReturn.hasNext()){
//                    ISSABasicBlock retBB = iReturn.next();
//                    SSAInstruction inst = retBB.getLastInstruction();
//                    if(inst instanceof SSAReturnInstruction){
//                        SSAReturnInstruction retInst = (SSAReturnInstruction) inst;
//                        if(retInst.getNumberOfUses() == 0)
//                            UNREACHABLE("Return block must have a return value: " + inst);
//                        int retVar = retInst.getUse(0);
//                        cccSet.addAll(getCallingContexts(originNode, originInst, callee, retVar));
//                    }else {
////                        UNREACHABLE("All return blocks must have return instruction: " + inst);
////                        UNREACHABLE("All return blocks must have return instruction: " + inst);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void visitArrayStore(SSAArrayStoreInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Array store instruction.");
//        }
//
//        @Override
//        public void visitBinaryOp(SSABinaryOpInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Binary operation instruction.");
//        }
//
//        @Override
//        public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Unary operation instruction.");
//        }
//
//        @Override
//        public void visitConversion(SSAConversionInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Conversion instruction.");
//        }
//
//        @Override
//        public void visitComparison(SSAComparisonInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Comparison instruction.");
//        }
//
//        @Override
//        public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Conditional branch instruction.");
//        }
//
//        @Override
//        public void visitSwitch(SSASwitchInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Switch instruction.");
//        }
//
//        @Override
//        public void visitReturn(SSAReturnInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Return instruction.");
//        }
//
//        @Override
//        public void visitGet(SSAGetInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            warnings.add("Intent can not created by Get instruction." + instruction);
////            UNREACHABLE("Intent can not created by Get instruction.");
//        }
//
//        @Override
//        public void visitPut(SSAPutInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Put instruction.");
//        }
//
//        @Override
//        public void visitInvoke(SSAInvokeInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            for(CGNode callee : cg.getPossibleTargets(n, instruction.getCallSite())){
//                IR ir = makeIR(callee);
//                SSACFG cfg = ir.getControlFlowGraph();
//                SSACFG.BasicBlock exitBlock = ir.getExitBlock();
//                Iterator<ISSABasicBlock> iReturn = cfg.getPredNodes(exitBlock);
//                while(iReturn.hasNext()){
//                    ISSABasicBlock retBB = iReturn.next();
//                    SSAInstruction inst = retBB.getLastInstruction();
//                    if(inst instanceof SSAReturnInstruction){
//                        SSAReturnInstruction retInst = (SSAReturnInstruction) inst;
//                        if(retInst.getNumberOfUses() == 0)
//                            UNREACHABLE("Return block must have a return value: " + inst);
//                        int retVar = retInst.getUse(0);
//                        cccSet.addAll(getCallingContexts(originNode, originInst, callee, retVar));
//                    }else {
////                        UNREACHABLE("All return blocks must have return instruction: " + inst);
////                        UNREACHABLE("All return blocks must have return instruction: " + inst);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void visitNew(SSANewInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            cccSet.addAll(findComponentCallingContext(originNode, originInst, instruction));
//        }
//
//        @Override
//        public void visitArrayLength(SSAArrayLengthInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Array length instruction.");
//        }
//
//        @Override
//        public void visitThrow(SSAThrowInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Throw instruction.");
//        }
//
//        @Override
//        public void visitMonitor(SSAMonitorInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Monitor instruction.");
//        }
//
//        @Override
//        public void visitCheckCast(SSACheckCastInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Check cast instruction.");
//        }
//
//        @Override
//        public void visitInstanceof(SSAInstanceofInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Instance of instruction.");
//        }
//
//        @Override
//        public void visitPhi(SSAPhiInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            for(int i = 0; i < instruction.getNumberOfUses(); i++){
//                int useVar = instruction.getUse(i);
//                if(useVar == -1)
//                    System.out.println("Could not track the intent object in phi instruction: " + instruction);
//                cccSet.addAll(getCallingContexts(originNode, originInst, n, useVar));
//            }
//
////            UNREACHABLE("Intent can not created by Phi instruction.");
//        }
//
//        @Override
//        public void visitPi(SSAPiInstruction instruction) {
//
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Pi instruction.");
//        }
//
//        @Override
//        public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Get caught instruction.");
//        }
//
//        @Override
//        public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
//            if(DEBUG)
//                System.out.println("INST: " + instruction);
//            UNREACHABLE("Intent can not created by Load metadata instruction.");
//        }
//
//        public Set<ComponentCallingContext> getResult(){
//            return cccSet;
//        }
//
//        private Set<ComponentCallingContext> findComponentCallingContext(CGNode originNode, SSAInstruction originInst, SSANewInstruction newInst){
//            SSAAbstractInvokeInstruction invokeInst = findInitInstOfNew(newInst);
//            return getComponentCallingContextFromInvoke(originNode, originInst, invokeInst);
//        }
//
//        private SSAAbstractInvokeInstruction findInitInstOfNew(SSANewInstruction newInst){
//            int newIndex = newInst.iindex;
//            IR ir = makeIR(n);
//            SSAInstruction[] insts = ir.getInstructions();
//            for(int i=newIndex+1; i < insts.length; i++){
//                SSAInstruction inst = insts[i];
//                if(inst instanceof SSAAbstractInvokeInstruction && inst.getUse(0) == newInst.getDef()){
//                    return (SSAAbstractInvokeInstruction)inst;
//                }
//            }
//
//            throw new UnimplementedError("Do not consider about non-initiated object yet: " + newInst);
//        }
//
//        private ConstantKey findMetaData(CGNode n, int var){
//            DefUse du = makeDefUse(n);
//            SSAInstruction inst = du.getDef(var);
//            if(inst instanceof SSALoadMetadataInstruction){
//                SSALoadMetadataInstruction metaInst = (SSALoadMetadataInstruction) inst;
//                TypeReference type = metaInst.getType();
//                Object o = metaInst.getToken();
//                return new ConstantKey(o, cg.getClassHierarchy().lookupClass(type));
//            }
//            Assertions.UNREACHABLE("This is not a variable for meta data: v" + var + " in " + n);
//            return null;
//        }
//
//        private Set<ComponentCallingContext> getComponentCallingContextFromInvoke(CGNode originNode, SSAInstruction originInst, SSAAbstractInvokeInstruction invokeInst){
//            MethodReference ref = invokeInst.getDeclaredTarget();
//
//            /*
//        INIT_INTENT1(Selector.make("<init>()V")), // empty initialization
//        INIT_INTENT2(Selector.make("<init>(Landroid/content/Context;Ljava/lang/Class;)V")), // explicit set target
//        INIT_INTENT3(Selector.make("<init>(Landroid/content/Intent;)V")), // copy intent
//        INIT_INTENT4(Selector.make("<init>(Landroid/content/Intent;Z)V")), // copy intent
//        INIT_INTENT5(Selector.make("<init>(Landroid/os/Parcel;)V")), // Unknown
//        INIT_INTENT6(Selector.make("<init>(Ljava/lang/String;)V")), // implicit intent
//        INIT_INTENT7(Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;)V")), // implicit intent
//        INIT_INTENT8(Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;Landroid/content/Context;Ljava/lang/Class;)V")), // explicit set target
//             */
//            if(ref.getDeclaringClass().getName().equals(Intent.INTENT_TYPE)){
//                Intent.InitSelector s = Intent.InitSelector.matchInit(ref.getSelector());
//                if(s != null){
//                    switch(s){
//                        case INIT_INTENT1:
//                            return findTargetsFromIntent(originNode, originInst, n, invokeInst.getUse(0));
//                        case INIT_INTENT2:
//                            int classVar2 = invokeInst.getUse(2);
//                            ComponentCallingContext ccc2 = new ComponentCallingContext(originNode, originInst);
//                            ccc2.addTarget(findMetaData(n, classVar2));
//                            return Collections.singleton(ccc2);
//                        case INIT_INTENT3:
//                        case INIT_INTENT4:
//                            int intentVar = invokeInst.getUse(1);
//                            return getCallingContexts(originNode, originInst, n, intentVar);
//                        case INIT_INTENT5:
//                            return findTargetsFromIntent(originNode, originInst, n, invokeInst.getDef());
//                        case INIT_INTENT6:
//                            //TODO: handle implicit intent
//                        case INIT_INTENT7:
//                            //TODO: handle implicit intent
//                            return Collections.singleton(new ComponentCallingContext(originNode, originInst));
//                        case INIT_INTENT8:
//                            int classVar8 = invokeInst.getUse(2);
//                            int actionVar = invokeInst.getUse(1);
//                            IR ir = makeIR(n);
//                            SymbolTable symTab = ir.getSymbolTable();
//
//                            ComponentCallingContext ccc8 = null;
//
//                            if(!symTab.isStringConstant(actionVar))
//                                ccc8 = new ComponentCallingContext(originNode, originInst);
//                            else
//                                ccc8 = new ComponentCallingContext(originNode, originInst, symTab.getStringValue(actionVar));
//
//                            ccc8.addTarget(findMetaData(n, classVar8));
//                            return Collections.singleton(ccc8);
//                    }
//                }
//            }else {
//                warnings.add("Now, do not consider about non-action based intent initialization: " + invokeInst);
////                throw new UnimplementedError("Now, do not consider about non-action based intent initialization: " + invokeInst);
//            }
//            return Collections.singleton(new ComponentCallingContext(originNode, originInst));
//        }
//
//        private Set<Integer> findReturnValues(CGNode n){
//            Set<Integer> res = new HashSet<Integer>();
//            IR ir = makeIR(n);
//            for(SSAInstruction inst : ir.getInstructions()){
//                if(inst == null)
//                    continue;
//
//                if(!(inst instanceof SSAReturnInstruction))
//                    continue;
//
//                if(inst.getNumberOfUses() != 1)
//                    continue;
//
//                res.add(inst.getUse(0));
//            }
//            return res;
//        }
//
//        private Set<ComponentCallingContext> trackComponentName(CGNode originNode, SSAInstruction originInst, CGNode n, int cnVar){
//            Set<ComponentCallingContext> res = new HashSet<ComponentCallingContext>();
//
//            // when ComponentName object is passed as an argument
//            if(cnVar < n.getMethod().getNumberOfParameters()){
//                Iterator<CGNode> iPred = cg.getPredNodes(n);
//                // for each predecessor node
//                while(iPred.hasNext()){
//                    CGNode pred = iPred.next();
//                    Iterator<CallSiteReference> iCsr = cg.getPossibleSites(n, pred);
//                    //for each call site
//                    while(iCsr.hasNext()){
//                        CallSiteReference csr = iCsr.next();
//
//                        for(SSAAbstractInvokeInstruction invokeInst : makeIR(pred).getCalls(csr)){
//                            int predCnVar = invokeInst.getUse(cnVar - 1);
//                            res.addAll(trackComponentName(originNode, originInst, pred, predCnVar));
//                        }
//                    }
//                }
//            }else{ // others
//                DefUse du = makeDefUse(n);
//                SSAInstruction defInst = du.getDef(cnVar);
//
//                // when ComponentName object is created by method call
//                if(defInst instanceof SSAAbstractInvokeInstruction){
//                    SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) defInst;
//
//                    // when the method call is createRelative
//                    if(invokeInst.getDeclaredTarget().getDeclaringClass().getName().equals(ComponentName.TYPE_NAME) && (invokeInst.getDeclaredTarget().getSelector().equals(ComponentName.CREATE_RELATIVE_SELECTOR1) || invokeInst.getDeclaredTarget().getSelector().equals(ComponentName.CREATE_RELATIVE_SELECTOR2))){
//                        int classVar = invokeInst.getUse(1);
//                        SymbolTable symTab = makeIR(n).getSymbolTable();
//                        if(symTab.isStringConstant(classVar)){
//                            res.add(new ComponentCallingContext(originNode, originInst, new ConstantKey(symTab.getStringValue(classVar), cg.getClassHierarchy().lookupClass(TypeReference.JavaLangString))));
//                        }else
//                            res.add(new ComponentCallingContext(originNode, originInst));
//                    }else { // otherwise,
//                        CallSiteReference csr = invokeInst.getCallSite();
//
//                        for (CGNode succ : cg.getPossibleTargets(n, csr)) {
//                            for (int var : findReturnValues(succ)) {
//                                res.addAll(trackComponentName(originNode, originInst, succ, var));
//                            }
//                        }
//                    }
//                }else if(defInst instanceof SSANewInstruction){ // when ComponentName object is locally created in this method
//                    SSANewInstruction newInst = (SSANewInstruction) defInst;
//                    SSAAbstractInvokeInstruction invokeInst = findInitInstruction(n, newInst);
//                    MethodReference initMRef = invokeInst.getDeclaredTarget();
//                    if(initMRef.getDeclaringClass().getName().equals(ComponentName.TYPE_NAME)){
//                        ComponentName.InitSelector s = ComponentName.InitSelector.match(initMRef.getSelector());
//
//                        switch(s){
//                            case INIT1:
//                                int classVar = invokeInst.getUse(2);
//                                ConstantKey ck = findMetaData(n, classVar);
//                                res.add(new ComponentCallingContext(originNode, originInst, ck));
//                                break;
//                            case INIT2:
//                                int targetVar2 = invokeInst.getUse(2);
//                                SymbolTable symTab2 = makeIR(n).getSymbolTable();
//                                if(symTab2.isStringConstant(targetVar2)){
//                                    res.add(new ComponentCallingContext(originNode, originInst, new ConstantKey(symTab2.getStringValue(targetVar2), cg.getClassHierarchy().lookupClass(TypeReference.JavaLangString))));
//                                }else
//                                    res.add(new ComponentCallingContext(originNode, originInst));
//                                break;
//                            case INIT3:
//                                //TODO: handle about createRelative method
//                            case INIT4:
//                                //TODO: handle about createRelative method
//                                res.add(new ComponentCallingContext(originNode, originInst));
//                                break;
//                            case INIT5:
//                                int targetVar5 = invokeInst.getUse(2);
//                                SymbolTable symTab5 = makeIR(n).getSymbolTable();
//                                if(symTab5.isStringConstant(targetVar5)){
//                                    res.add(new ComponentCallingContext(originNode, originInst, new ConstantKey(symTab5.getStringValue(targetVar5), cg.getClassHierarchy().lookupClass(TypeReference.JavaLangString))));
//                                }else
//                                    res.add(new ComponentCallingContext(originNode, originInst));
//                                break;
//                        }
//                    }else{
//                        Assertions.UNREACHABLE("The target of this instruction must be an init method of ComponentName: " + invokeInst + ", CurTarget: " + initMRef);
//                    }
//                }
//            }
//            return res;
//        }
//
//        private SSAAbstractInvokeInstruction findInitInstruction(CGNode n, SSANewInstruction newInst){
//            SSAInstruction[] insts = makeIR(n).getInstructions();
//            int defVar = newInst.getDef();
//
//            for(int i = newInst.iindex; i<insts.length; i++){
//                SSAInstruction inst = insts[i];
//                if(inst == null)
//                    continue;
//
//                if(!(inst instanceof SSAAbstractInvokeInstruction))
//                    continue;
//
//                if(!(inst.getNumberOfUses() > 0))
//                    continue;
//
//                if(inst.getUse(0) == defVar)
//                    return (SSAAbstractInvokeInstruction)inst;
//            }
//
//            Assertions.UNREACHABLE("Cannot find the init instruction: " + newInst + " in " + n);
//            return null;
//        }
//
//        private Set<ComponentCallingContext> findTargetsFromIntent(CGNode originNode, SSAInstruction originInst, CGNode n, int intentVar){
//            Set<ComponentCallingContext> res = findTargets(originNode, originInst, n, intentVar);
//
//            if(res.isEmpty())
//                res.add(new ComponentCallingContext(originNode, originInst));
//            return res;
//        }
//
//        private Set<ComponentCallingContext> findTargets(CGNode originNode, SSAInstruction originInst, CGNode n, int intentVar){
//            Set<ComponentCallingContext> res = new HashSet<ComponentCallingContext>();
//
//            DefUse du = makeDefUse(n);
//            Iterator<SSAInstruction> iUseInst = du.getUses(intentVar);
//            while(iUseInst.hasNext()){
//                SSAInstruction useInst = iUseInst.next();
//
//                useInst.visit(new SSAInstruction.IVisitor() {
//                    @Override
//                    public void visitGoto(SSAGotoInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Goto instruction");
//                    }
//
//                    @Override
//                    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Arrayload instruction");
//                    }
//
//                    @Override
//                    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Arraystore instruction");
//                    }
//
//                    @Override
//                    public void visitBinaryOp(SSABinaryOpInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Binary operation instruction");
//                    }
//
//                    @Override
//                    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Unary operation instruction");
//                    }
//
//                    @Override
//                    public void visitConversion(SSAConversionInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Conversion instruction");
//                    }
//
//                    @Override
//                    public void visitComparison(SSAComparisonInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Comparison instruction");
//                    }
//
//                    @Override
//                    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Conditional branch instruction");
//                    }
//
//                    @Override
//                    public void visitSwitch(SSASwitchInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Switch instruction");
//                    }
//
//                    @Override
//                    public void visitReturn(SSAReturnInstruction instruction) {
//                        //TODO: should we track the data flow in return site?
//                    }
//
//                    @Override
//                    public void visitGet(SSAGetInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Get instruction");
//                    }
//
//                    @Override
//                    public void visitPut(SSAPutInstruction instruction) {
//                        //TODO: just ignore this case; it is not related with target
//                        Assertions.UNREACHABLE("Intent cannot be used in Put instruction");
//                    }
//
//                    @Override
//                    public void visitInvoke(SSAInvokeInstruction instruction) {
//                        int argNum = 0;
//
//                        //find a variable number for the intent value in the callee node
//                        for(; argNum < instruction.getNumberOfUses(); argNum++){
//                            int useVar = instruction.getUse(argNum);
//                            if(useVar == intentVar) {
//                                argNum += 1;
//                                break;
//                            }
//                        }
//
//                        MethodReference targetMRef = instruction.getDeclaredTarget();
//
//                        // when setComponentName is called
//                        if(argNum == 1 && targetMRef.getDeclaringClass().getName().equals(Intent.INTENT_TYPE) && targetMRef.getSelector().equals(Intent.SET_COMPONENT_SELECTOR)){
//                            res.addAll(trackComponentName(originNode, originInst, n, instruction.getUse(1)));
//                        }else if(argNum == 1 && targetMRef.getDeclaringClass().getName().equals(Intent.INTENT_TYPE) && targetMRef.getSelector().equals(Intent.SET_CLASS_SELECTOR)){
//                            int classVar = instruction.getUse(2);
//                            ConstantKey ck = findMetaData(n, classVar);
//
//                            res.add(new ComponentCallingContext(originNode, originInst, ck));
//                        }else if(argNum == 1 && targetMRef.getDeclaringClass().getName().equals(Intent.INTENT_TYPE) && (targetMRef.getSelector().equals(Intent.SET_CLASS_NAME_SELECTOR1) || targetMRef.getSelector().equals(Intent.SET_CLASS_NAME_SELECTOR2))){
//                            int classVar = instruction.getUse(2);
//                            SymbolTable symTab = makeIR(n).getSymbolTable();
//
//                            if(symTab.isStringConstant(classVar)){
//                                res.add(new ComponentCallingContext(originNode, originInst, new ConstantKey(symTab.getStringValue(classVar), cg.getClassHierarchy().lookupClass(TypeReference.JavaLangString))));
//                            }else
//                                res.add(new ComponentCallingContext(originNode, originInst));
//                        }else {
//                            //recursively, find the target in successor nodes
//                            for (CGNode succ : cg.getPossibleTargets(n, instruction.getCallSite())) {
//                                res.addAll(findTargets(originNode, originInst, succ, argNum));
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void visitNew(SSANewInstruction instruction) {
//                        //TODO: Ignore this case; we already treat this case at getComponentCallingContextFromInvoke method
////                        Assertions.UNREACHABLE("Intent cannot be used in New instruction");
//                    }
//
//                    @Override
//                    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Array Length instruction");
//                    }
//
//                    @Override
//                    public void visitThrow(SSAThrowInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Throw instruction");
//                    }
//
//                    @Override
//                    public void visitMonitor(SSAMonitorInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Mornitor instruction");
//                    }
//
//                    @Override
//                    public void visitCheckCast(SSACheckCastInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Checkcast instruction");
//                    }
//
//                    @Override
//                    public void visitInstanceof(SSAInstanceofInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Instanceof instruction");
//                    }
//
//                    @Override
//                    public void visitPhi(SSAPhiInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Phi instruction");
//                    }
//
//                    @Override
//                    public void visitPi(SSAPiInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in Pi instruction");
//                    }
//
//                    @Override
//                    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in GetCaughtException instruction");
//                    }
//
//                    @Override
//                    public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
//                        Assertions.UNREACHABLE("Intent cannot be used in LoadMetadata instruction");
//                    }
//                });
//            }
//
//            return res;
//        }
//    }
//}
