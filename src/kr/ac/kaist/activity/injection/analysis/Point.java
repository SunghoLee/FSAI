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

    public Point(CGNode n, int iindex, int v){
        this.n = n;
        this.iindex = iindex;
        this.v = v;
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

    public SSAInstruction getinstruction(CHACache cache){
        return cache.makeIR(this.n).getInstructions()[this.iindex];
    }

    @Override
    public int hashCode(){
        return n.hashCode() * iindex + v;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Point){
            Point p = (Point) o;
            if(p.n.equals(this.n) && p.iindex == this.iindex && p.v == this.v) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return n.toString() + " # (" + iindex + ") " + ((iindex > -1)? getinstruction(new CHACache(1)) : "PHI")+ " # " + v;
    }
}
