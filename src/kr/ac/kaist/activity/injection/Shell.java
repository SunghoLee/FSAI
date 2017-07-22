package kr.ac.kaist.activity.injection;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.MethodReference;
import kr.ac.kaist.activity.injection.analysis.ActivityInjectionDetector;
import kr.ac.kaist.activity.injection.analysis.IntentAnalysis;
import kr.ac.kaist.activity.injection.analysis.LaunchingActInfo;
import kr.ac.kaist.activity.injection.appinfo.ActivityInfoExtractor;
import kr.ac.kaist.activity.injection.callgraph.CHACallGraphBuiler;
import kr.ac.kaist.activity.injection.types.Activity;
import kr.ac.kaist.activity.injection.util.IRPrinter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static kr.ac.kaist.activity.injection.analysis.LibraryModel.isLibrary;

/**
 * Created by leesh on 21/02/2017.
 */
public class Shell {

    private static final boolean DEBUG = true;
    private static final String destDir = "affinities";
    private static String appName;

    public static void main(String[] args){
        String property = args[0];
        String apk = args[1];
        appName = apk.substring(0, apk.lastIndexOf("."));
        long start = System.currentTimeMillis();

        try {
            ActivityInfoExtractor extractor = new ActivityInfoExtractor(apk);
            Set<ActivityInfoExtractor.ActivityInfo> infos = extractor.extract();
//            for(ActivityInfoExtractor.ActivityInfo info : infos){
//                System.out.println(info);
//            }

            boolean isAnalyzable = isAnalyzable(infos, extractor.getAppPackageName());
            boolean hasSingleInstance = hasSingleInstance(infos, extractor.getAppPackageName());

            System.out.println("#Analyzable? " + isAnalyzable);
            System.out.println("#hasSingleInstance? " + hasSingleInstance);

            if(!DEBUG){
                if(!isAnalyzable){
                    removeFile(apk);
                }else{
                    moveToDest(apk, extractor.getDecompDir());
                }
                extractor.removeDecompDir();
            }

            CHACallGraphBuiler builder = new CHACallGraphBuiler(property, apk);

            CallGraph cg = builder.buildCallGraph();
            IRPrinter.printIR(cg, "ir", new IRPrinter.Filter() {
                @Override
                public boolean filter(CGNode n) {
                    if(isLibrary(n) && !n.getMethod().toString().contains("fakeRootMethod"))
                        return false;
                    return true;
                }
            });

            Set<ActivityInfoExtractor.ActivityInfo> injectableActivities = findInjectableActivity(infos, extractor.getAppPackageName());
            Set<String> trackActs = new HashSet<>();
            for(ActivityInfoExtractor.ActivityInfo ai : injectableActivities){
                trackActs.add(ai.getPackageName() + "/" + ai.getActivityName());
            }
            IntentAnalysis ia = new IntentAnalysis(cg, trackActs);
            Set<IntentAnalysis.IntentInfo> intentInfos = ia.analyze();
            System.out.println("##### Injectable Activities #####");
            System.out.println("# PACKNAME: " + extractor.getAppPackageName());
            for(ActivityInfoExtractor.ActivityInfo s : injectableActivities)
                System.out.println(s.getActivityName() + " ( " + s.getLaunchMode() + " ) => " + s.getTaskAffinity());
            System.out.println("#################################");
            System.out.println();

            Set<LaunchingActInfo> finalInfo = joinActivityIntentData(infos, intentInfos);
            ActivityInjectionDetector injectionDetector = new ActivityInjectionDetector(finalInfo, extractor.getAppPackageName());
            Set<LaunchingActInfo> injectionInfo = injectionDetector.detect();

            System.out.println("##### Final Injection Results #####");
            System.out.println("# PACKNAME: " + extractor.getAppPackageName());
            for(LaunchingActInfo s : injectionInfo)
                System.out.println(s.toString() + " => INJECTION_DETECTED! call? " + s.isCalled());
            System.out.println("#################################");
            System.out.println();

//            CallingComponentAnalysis cca = new CallingComponentAnalysis(cg);
//            for(CallingComponentAnalysis.ComponentCallingContext cc : cca.getCallingContexts()){
//                System.out.println(cc);
//            }

//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
        } catch (ClassHierarchyException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();

        System.out.println("#TOTAL TIME: " + ((end - start)/1000) + "s");
    }

    private static Set<CGNode> findStartActivityNode(CallGraph cg, MethodReference mr){
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

    private static boolean isAnalyzable(Set<ActivityInfoExtractor.ActivityInfo> infos, String packageName){
        for(ActivityInfoExtractor.ActivityInfo info : infos){
            if(!info.getTaskAffinity().equals(packageName)) {
                if(!info.getTaskAffinity().equals(""))
                    return true;
            }
        }
        return false;
    }

    private static boolean hasSingleInstance(Set<ActivityInfoExtractor.ActivityInfo> infos, String packageName){
        for(ActivityInfoExtractor.ActivityInfo info : infos){
            if(info.getLaunchMode().equals(Activity.LaunchMode.SINGLEINSTANCE)) {
                System.out.println("SINGLEINSTANCE: " + info);
                return true;
            }
        }
        return false;
    }

    private static Set<ActivityInfoExtractor.ActivityInfo> findInjectableActivity(Set<ActivityInfoExtractor.ActivityInfo> infos, String packageName){
        Set<ActivityInfoExtractor.ActivityInfo> res = new HashSet<>();
        for(ActivityInfoExtractor.ActivityInfo info : infos){
            if(!info.getTaskAffinity().equals(packageName)) {
                if(!info.getTaskAffinity().equals(""))
                    res.add(info);
            }
        }
        return res;
    }
    private static void removeFile(String apk){
        File f = new File(apk);
        f.delete();
    }

    private static void moveToDest(String apk, String decompDir){
        File dest = new File(destDir);
        if(!dest.exists())
            dest.mkdir();

        ProcessBuilder pb = null;
        try {
            pb = new ProcessBuilder("mv", apk, dest.getCanonicalPath());
            pb.start();
            File ttt = new File(decompDir + File.separator + "AndroidManifest.xml");
            System.out.println(decompDir + File.separator + "AndroidManifest.xml (" + ttt.exists() + ")");

            System.out.println(dest.getCanonicalPath() + File.separator + appName + "_AndroidManifest.xml");
            pb = new ProcessBuilder("mv", decompDir + File.separator + "AndroidManifest.xml", dest.getCanonicalPath() + File.separator + appName + "_AndroidManifest.xml");
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<LaunchingActInfo> joinActivityIntentData(Set<ActivityInfoExtractor.ActivityInfo> actInfos, Set<IntentAnalysis.IntentInfo> intentInfos){
        Set<LaunchingActInfo> res = new HashSet<>();

        for(ActivityInfoExtractor.ActivityInfo actInfo : actInfos){
            String activityName = actInfo.getPackageName() + "/" + actInfo.getActivityName();
            String taskAffinity = actInfo.getTaskAffinity();
            Activity.LaunchMode launchMode = actInfo.getLaunchMode();
//            System.out.println("=> " + activityName);
            for(IntentAnalysis.IntentInfo intInfo : intentInfos){
//                System.out.println("\t<= " + intInfo.getActivityName());
                if(intInfo.getActivityName().equals(activityName)){
                    res.add(new LaunchingActInfo(activityName, launchMode, taskAffinity, intInfo.getFlags(), intInfo.isInter(), intInfo.isField(), intInfo.isArray(), intInfo.isCalled()));
                }
            }
        }

        System.out.println("###### Analysis Results ######");
        for(LaunchingActInfo lai : res){
            System.out.println(lai);
        }
        System.out.println("##############################");
        System.out.println();
        return res;
    }
}
