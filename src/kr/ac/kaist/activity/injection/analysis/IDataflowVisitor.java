//package kr.ac.kaist.activity.injection.analysis;
//
//import com.ibm.wala.ssa.*;
//
//import java.util.Set;
//
///**
// * Created by leesh on 30/04/2017.
// */
//public interface IDataflowVisitor implements SSAInstruction.IVisitor {
//    @Override
//    void visitGoto(SSAGotoInstruction instruction);
//
//    @Override
//    void visitArrayLoad(SSAArrayLoadInstruction instruction);
//
//    @Override
//    void visitArrayStore(SSAArrayStoreInstruction instruction);
//
//    @Override
//    void visitBinaryOp(SSABinaryOpInstruction instruction);
//
//    @Override
//    void visitUnaryOp(SSAUnaryOpInstruction instruction);
//
//    @Override
//    void visitConversion(SSAConversionInstruction instruction);
//
//    @Override
//    void visitComparison(SSAComparisonInstruction instruction);
//
//    @Override
//    void visitConditionalBranch(SSAConditionalBranchInstruction instruction);
//
//    @Override
//    void visitSwitch(SSASwitchInstruction instruction);
//
//    @Override
//    void visitReturn(SSAReturnInstruction instruction);
//
//    @Override
//    void visitGet(SSAGetInstruction instruction);
//
//    @Override
//    void visitPut(SSAPutInstruction instruction);
//
//    @Override
//    void visitInvoke(SSAInvokeInstruction instruction);
//
//    @Override
//    void visitNew(SSANewInstruction instruction);
//
//    @Override
//    void visitArrayLength(SSAArrayLengthInstruction instruction);
//
//    @Override
//    void visitThrow(SSAThrowInstruction instruction);
//
//    @Override
//    void visitMonitor(SSAMonitorInstruction instruction);
//
//    @Override
//    void visitCheckCast(SSACheckCastInstruction instruction);
//
//    @Override
//    void visitInstanceof(SSAInstanceofInstruction instruction);
//
//    @Override
//    void visitPhi(SSAPhiInstruction instruction);
//
//    @Override
//    void visitPi(SSAPiInstruction instruction);
//
//    @Override
//    void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction);
//
//    @Override
//    void visitLoadMetadata(SSALoadMetadataInstruction instruction);
//
//    Set<Point> getUsesPoint();
//}
