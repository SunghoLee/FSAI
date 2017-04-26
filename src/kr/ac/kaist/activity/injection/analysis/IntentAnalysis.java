package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 25/04/2017.
 */
public class IntentAnalysis {
    private final CallGraph cg;
    private final BackwardDataflowUsingDefUse backward;
    public final static MethodReference START_ACTIVTY1 = MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/ContextWrapper"), Selector.make("startActivity(Landroid/content/Intent;)V"));

    public IntentAnalysis(CallGraph cg){
        this.cg = cg;
        this.backward = new BackwardDataflowUsingDefUse(cg, new CHACache(1000));
        this.backward.addSeeds(findStartActivityNode(cg, START_ACTIVTY1), 1);
    }

    public void analyze(){
        Set<BackwardDataflowUsingDefUse.DataflowResult> res = backward.analyze();
        for(BackwardDataflowUsingDefUse.DataflowResult r : res){
            System.out.println(r);
        }
    }

    private Set<CGNode> findStartActivityNode(CallGraph cg, MethodReference mr){
        Set<CGNode> targetNodes = new HashSet<>();

        //get startActivity nodes of ContextWrapper
        Iterator<CGNode> iNodes = cg.getNodes(mr).iterator();
        while(iNodes.hasNext())
            targetNodes.add(iNodes.next());

        //recursively get startActivity nodes of subclasses of ContextWrapper
        for(IClass sub : cg.getClassHierarchy().computeSubClasses(mr.getDeclaringClass())){

            MethodReference subMr = MethodReference.findOrCreate(sub.getReference(), mr.getSelector());
            Iterator<CGNode> iSubNodes = cg.getNodes(subMr).iterator();
            while(iSubNodes.hasNext())
                targetNodes.add(iSubNodes.next());
        }

        return targetNodes;
    }
}
