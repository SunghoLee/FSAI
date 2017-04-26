//package kr.ac.kaist.activity.injection.analysis;
//
//import com.ibm.wala.ipa.cfg.BasicBlockInContext;
//import com.ibm.wala.ssa.*;
//import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
//
///**
// * Created by leesh on 21/04/2017.
// */
//public class IntegerCalculationDataflowVisitor implements IDataflowVisitor<Integer> {
//    private int res = Integer.MIN_VALUE;
//
//    @Override
//    public Integer visit(BasicBlockInContext<IExplodedBasicBlock> bb, final Integer v) {
//        if(bb.getLastInstruction() == null)
//            return v;
//
//        final SSAInstruction inst = bb.getLastInstruction();
//        SSAInstruction.IVisitor visitor = new SSAInstruction.IVisitor() {
//            @Override
//            public void visitGoto(SSAGotoInstruction instruction) {
//                res = v;
//            }
//
//            @Override
//            public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
//                //TODO:
//            }
//
//            @Override
//            public void visitArrayStore(SSAArrayStoreInstruction instruction) {
//                //TODO:
//            }
//
//            @Override
//            public void visitBinaryOp(SSABinaryOpInstruction instruction) {
//                //TODO:
//            }
//
//            @Override
//            public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitConversion(SSAConversionInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitComparison(SSAComparisonInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitSwitch(SSASwitchInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitReturn(SSAReturnInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitGet(SSAGetInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitPut(SSAPutInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitInvoke(SSAInvokeInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitNew(SSANewInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitArrayLength(SSAArrayLengthInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitThrow(SSAThrowInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitMonitor(SSAMonitorInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitCheckCast(SSACheckCastInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitInstanceof(SSAInstanceofInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitPhi(SSAPhiInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitPi(SSAPiInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
//
//            }
//
//            @Override
//            public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
//
//            }
//        };
//
//        inst.visit(visitor);
//        return res;
//    }
//}
