package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SymbolTable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leesh on 25/04/2017.
 */
public class CHACache {
    private final Map<CGNode, IR> irCache = new HashMap<>();
    private final Map<CGNode, DefUse> duCache = new HashMap<>();
    private final DexIRFactory irFactory = new DexIRFactory();
    private final int cacheLimit;

    public CHACache(int limit){
        this.cacheLimit = limit;
    }

    public IR makeIR(CGNode n){
        if(irCache.keySet().contains(n))
            return irCache.get(n);

        if(irCache.size() == cacheLimit)
            irCache.clear();

        IR ir = irFactory.makeIR(n.getMethod(), Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
        irCache.put(n, ir);
        return ir;
    }

    public DefUse makeDefUse(CGNode n){
        if(duCache.keySet().contains(n))
            return duCache.get(n);

        if(duCache.size() == cacheLimit)
            duCache.clear();

        DefUse du = new DefUse(makeIR(n));
        duCache.put(n, du);
        return du;
    }

    public SymbolTable makeSymbolTable(CGNode n){
        return makeIR(n).getSymbolTable();
    }
}
