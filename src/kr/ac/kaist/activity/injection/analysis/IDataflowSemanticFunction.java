//package kr.ac.kaist.activity.injection.analysis;
//
//import com.ibm.wala.ipa.callgraph.CGNode;
//import com.ibm.wala.ssa.*;
//
//import java.util.Set;
//
///**
// * Created by leesh on 21/04/2017.
// */
//public interface IDataflowSemanticFunction {
//    public enum PathType{
//        TRACK,
//        END,
//    }
//
//    default Set<Path> visit(CGNode n, SSAInstruction inst, int v){
//        return visit(n, inst, v);
//    }
//    public Set<Path> visit(CGNode n, BackwardDataflowAnalysis.SSAEntryInstruction inst, int v);
//    public Set<Path> visit(CGNode n, BackwardDataflowAnalysis.SSAExitInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAAbstractBinaryInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAAbstractInvokeInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAAbstractThrowInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAAbstractUnaryInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAAddressOfInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAArrayLengthInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAArrayLoadInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAArrayStoreInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSABinaryOpInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAArrayReferenceInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSACheckCastInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAComparisonInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAConditionalBranchInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAConversionInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAFieldAccessInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAGetCaughtExceptionInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAGotoInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAInstanceofInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSALoadMetadataInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAMonitorInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSANewInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAPhiInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAPiInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAPutInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAGetInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAUnaryOpInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAReturnInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSAStoreIndirectInstruction inst, int v);
//    public Set<Path> visit(CGNode n, SSASwitchInstruction inst, int v);
//
//    class Path{
//        private final PathType type;
//        private final int var;
//
//        public Path(PathType type, int var){
//            this.type = type;
//            this.var = var;
//        }
//
//        public PathType getType(){
//            return this.type;
//        }
//
//        public int getVars(){
//            return this.var;
//        }
//    }
//}
