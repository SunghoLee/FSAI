//package kr.ac.kaist.activity.injection.analysis;
//
//import com.ibm.wala.ssa.*;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * Created by leesh on 30/04/2017.
// */
//public class IntentDataflowVisitor implements IDataflowVisitor {
//    private final Set<Point> usePoints = new HashSet<>();
//
//    @Override
//    public void visitGoto(SSAGotoInstruction instruction) {
//        //no-op;
//    }
//
//    @Override
//    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitBinaryOp(SSABinaryOpInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitConversion(SSAConversionInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitComparison(SSAComparisonInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitSwitch(SSASwitchInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitReturn(SSAReturnInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitGet(SSAGetInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitPut(SSAPutInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitInvoke(SSAInvokeInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitNew(SSANewInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitThrow(SSAThrowInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitMonitor(SSAMonitorInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitCheckCast(SSACheckCastInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitInstanceof(SSAInstanceofInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitPhi(SSAPhiInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitPi(SSAPiInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
//
//    }
//
//    @Override
//    public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
//
//    }
//
//    @Override
//    public Set<Point> getUsesPoint() {
//        return this.usePoints;
//    }
//
//    class Work{
//
//    }
//}
