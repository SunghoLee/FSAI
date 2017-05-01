package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

/**
 * Created by leesh on 25/04/2017.
 */
public class Point {
    private final CGNode n;
    private final int iindex;
    private final int v;
    private final SSAInstruction inst;
    private final ForwardDataflowAnalysisUsingDefUse.Work w;

    public Point(CGNode n, int iindex, int v, SSAInstruction inst, ForwardDataflowAnalysisUsingDefUse.Work w){
        this.n = n;
        this.iindex = iindex;
        this.v = v;
        this.inst = inst;
        this.w = w;
    }

    public CGNode getNode(){
        return this.n;
    }

    public int getIndex(){
        return this.iindex;
    }

    public int getTrackingVar(){
        return this.v;
    }

    public SSAInstruction getinstruction(){
        return this.inst;
    }

    @Override
    public int hashCode(){
        return n.hashCode() * iindex + v;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Point){
            Point p = (Point) o;
            if(p.n.equals(this.n) && p.iindex == this.iindex && p.v == this.v){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return n.toString() + " # (" + iindex + ") " + getinstruction() + " # " + v + " [WITH] " + w;
    }

    public ForwardDataflowAnalysisUsingDefUse.Work getWork(){
        return w;
    }
}
