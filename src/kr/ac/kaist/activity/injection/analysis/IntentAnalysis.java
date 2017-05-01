package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.activity.injection.types.Intent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static kr.ac.kaist.activity.injection.analysis.LibraryModel.isLibrary;

/**
 * Created by leesh on 25/04/2017.
 */
public class IntentAnalysis {
    private final CallGraph cg;
//    private final BackwardDataflowUsingDefUse backward;
    private final ForwardDataflowAnalysisUsingDefUse forward;
    public final static MethodReference START_ACTIVTY1 = MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/ContextWrapper"), Selector.make("startActivity(Landroid/content/Intent;)V"));
    private final Set<String> trackableActivities;

    public IntentAnalysis(CallGraph cg){
        this(cg, new HashSet<>());
    }

    public IntentAnalysis(CallGraph cg, Set<String> actNames){
        this.cg = cg;
//        this.backward = new BackwardDataflowUsingDefUse(cg, new CHACache(1000));
        this.forward = new ForwardDataflowAnalysisUsingDefUse(cg, new CHACache(1000));
        this.forward.addSeeds(findIntentCreationSites());
//        this.backward.addSeeds(findStartActivityNode(cg, START_ACTIVTY1), 1);
        this.trackableActivities = actNames;
    }

    public Set<IntentInfo> analyze(){
        forward.analyze();

        removeUnknown();
        printResults();
        return intentRes;
    }

    Set<IntentInfo> intentRes = new HashSet<>();

    private void removeUnknown(){
        Set<IntentInfo> irs = new HashSet<>(intentRes);
        for(IntentInfo i : irs){
            if(i.getActivityName().equals("UNKNOWN"))
                intentRes.remove(i);
        }
    }

    public void printResults(){
        System.out.println("###### Intent Information ######");
        for(IntentInfo i : intentRes){
            System.out.println(i);
        }
        System.out.println("################################");
        System.out.println();
    }

    public Set<Point> findIntentCreationSites(){
        Set<Point> res = new HashSet<>();

        CHACache cache = new CHACache(1000);
        for(CGNode n : cg){
            if(isLibrary(n))
                continue;

            IR ir = null;
            try {
                ir = cache.makeIR(n);
            }catch(NullPointerException e){
                continue;
            }
            Iterator<NewSiteReference> iNsr = ir.iterateNewSites();
            while(iNsr.hasNext()){
                NewSiteReference nsr = iNsr.next();
                SSANewInstruction newInst = ir.getNew(nsr);

                if(newInst.getConcreteType().getName().equals(Intent.INTENT_TYPE)) {
                    final IntentInfo info = new IntentInfo();
                    intentRes.add(info);

                    res.add(new Point(n, newInst.iindex, newInst.getDef(), newInst, new ForwardDataflowAnalysisUsingDefUse.Work(null, newInst.getDef(), new ForwardDataflowAnalysisUsingDefUse.WorkVisitor() {

                        @Override
                        public boolean visit(CGNode n, SSAInstruction inst, int targetV) {
                            if(inst instanceof SSAAbstractInvokeInstruction && inst.getUse(0) == targetV){
                                SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) inst;
                                if(invokeInst.toString().contains("<init>")){
                                    for(Intent.InitSelector init : Intent.InitSelector.values()){
                                        switch(Intent.InitSelector.matchInit(invokeInst.getDeclaredTarget().getSelector())){
                                            /*
                                            INIT_INTENT1(Selector.make("<init>()V")), // empty initialization
                                            INIT_INTENT2(Selector.make("<init>(Landroid/content/Context;Ljava/lang/Class;)V")), // explicit set target
                                            INIT_INTENT3(Selector.make("<init>(Landroid/content/Intent;)V")), // copy intent
                                            INIT_INTENT4(Selector.make("<init>(Landroid/content/Intent;Z)V")), // copy intent
                                            INIT_INTENT5(Selector.make("<init>(Landroid/os/Parcel;)V")), // Unknown
                                            INIT_INTENT6(Selector.make("<init>(Ljava/lang/String;)V")), // implicit intent
                                            INIT_INTENT7(Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;)V")), // implicit intent
                                            INIT_INTENT8(Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;Landroid/content/Context;Ljava/lang/Class;)V")), // explicit set target
                                             */
                                            case INIT_INTENT1:
//                                                break;
                                                return true;
                                            case INIT_INTENT2:
                                                IR ir2 = cache.makeIR(n);
                                                SSAInstruction[] insts2 = ir2.getInstructions();
                                                for(int i = invokeInst.iindex -1; i>-1; i--){
                                                    if(insts2[i] == null)
                                                        continue;
                                                    if(insts2[i] instanceof SSALoadMetadataInstruction && insts2[i].getDef() == invokeInst.getUse(2)){
                                                        SSALoadMetadataInstruction metaInst2 = (SSALoadMetadataInstruction) insts2[i];
                                                        if(!trackableActivities.isEmpty()) {
                                                            if(trackableActivities.contains(adjustActivityName(metaInst2.getToken().toString())))
                                                                info.setActivityName(metaInst2.getToken().toString());
                                                            else
                                                                return true;
                                                        }else
                                                            info.setActivityName(metaInst2.getToken().toString());
                                                    }
                                                }
                                                break;
                                            case INIT_INTENT3:
                                                return true;
//                                                break;
                                            case INIT_INTENT4:
                                                return true;
//                                                break;
                                            case INIT_INTENT5:
                                                return true;
//                                                break;
                                            case INIT_INTENT6:
                                                if(!trackableActivities.isEmpty())
                                                    return true;
                                                if(cache.makeSymbolTable(n).isStringConstant(invokeInst.getUse(1)))
                                                    info.setActivityName(cache.makeSymbolTable(n).getStringValue(invokeInst.getUse(1)));
                                                else
                                                    return true;
                                                break;
                                            case INIT_INTENT7:
                                                if(!trackableActivities.isEmpty())
                                                    return true;
                                                if(cache.makeSymbolTable(n).isStringConstant(invokeInst.getUse(1)))
                                                    info.setActivityName(cache.makeSymbolTable(n).getStringValue(invokeInst.getUse(1)));
                                                else
                                                    return true;
                                                break;
                                            case INIT_INTENT8:
                                                IR ir8 = cache.makeIR(n);
                                                SSAInstruction[] insts8 = ir8.getInstructions();
                                                for(int i = invokeInst.iindex -1; i>-1; i--){
                                                    if(insts8[i] == null)
                                                        continue;
                                                    if(insts8[i] instanceof SSALoadMetadataInstruction && insts8[i].getDef() == invokeInst.getUse(4)){
                                                        SSALoadMetadataInstruction metaInst = (SSALoadMetadataInstruction) insts8[i];
                                                        if(!trackableActivities.isEmpty()) {
                                                            if(trackableActivities.contains(adjustActivityName(metaInst.getToken().toString())))
                                                                info.setActivityName(metaInst.getToken().toString());
                                                            else
                                                                return true;
                                                        }else
                                                            info.setActivityName(metaInst.getToken().toString());
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }else{
                                    if(Intent.AddFlagsSelector.ADD_FLAGS.getSelector().equals(invokeInst.getDeclaredTarget().getSelector()) ||
                                            Intent.SetFlagsSelector.SET_FLAGS.getSelector().equals(invokeInst.getDeclaredTarget().getSelector())){
                                        int flagVar = invokeInst.getUse(1);
                                        if(cache.makeSymbolTable(n).isConstant(flagVar)){
                                            Set<Intent.Flag> flags = Intent.Flag.calculateFlags(cache.makeSymbolTable(n).getIntValue(flagVar));
                                            if(!flags.isEmpty())
                                                info.addFlags(flags);
                                            else{
                                                System.err.println("Is it possible? " + cache.makeSymbolTable(n).getIntValue(flagVar));
                                            }
                                        }else{
                                            System.err.println("UNKNOW FLAG!: " + invokeInst + " in " + n);
                                        }
                                    }
                                }
                            }
                            return false;
                        }

                        @Override
                        public String toString() {
                            return "INTENT_WORK";
                        }
                    })));
                }
            }
        }

        return res;
    }

    private String adjustActivityName(String name){
        if(name.contains(","))
            name = name.substring(name.indexOf(",")+1, name.lastIndexOf(">"));
        return name;
    }

    public static class IntentInfo {
        private String activityName = "UNKNOWN";
        private final Set<Intent.Flag> flags = new HashSet<>();

        public IntentInfo(){}

        public void setActivityName(String name){
            if(name.contains(","))
                name = name.substring(name.indexOf(",")+1, name.lastIndexOf(">"));
            this.activityName = name;
        }

        public void addFlag(Intent.Flag flag){
            this.flags.add(flag);
        }

        public void addFlags(Set<Intent.Flag> flags){
            this.flags.addAll(flags);
        }

        public String getActivityName(){
            return this.activityName;
        }

        public Set<Intent.Flag> getFlags(){
            return this.flags;
        }

        @Override
        public String toString(){
            return "[Intent] name: " + activityName + ", Flags: " + flags;
        }
    }
}
