package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CallGraph;

/**
 * Created by leesh on 21/04/2017.
 */
public class IntentDataAnalysis {
    private final ICFGSupergraph supergraph;

    public IntentDataAnalysis(CallGraph cg){
        this.supergraph = ICFGSupergraph.make(cg, new AnalysisCache());
    }


}
