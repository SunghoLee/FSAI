package kr.ac.kaist.activity.injection.callgraph;

import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;
import kr.ac.kaist.wala.hybridroid.utils.LocalFileReader;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarFile;

/**
 * Created by leesh on 20/02/2017.
 */
public class CHACallGraphBuiler {

    private final Properties properties;
    private final IClassHierarchy cha;
    public CHACallGraphBuiler(String prop, String apk) throws ClassHierarchyException {
        File propFile = new File(prop);
        this.properties = new Properties();
        try {
            this.properties.load(new FileInputStream(propFile));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        AnalysisScope scope = makeAnalysisScope(apk);
        this.cha = buildClassHierarchy(scope);
//        for(IClass c : cha){
//            if(c.getName().getClassName().toString().endsWith("ComponentName")){
//                for(IMethod m : c.getDeclaredMethods()){
//                        System.out.println("#M: " + m);
//                }
//            }
//        }
//
//        System.exit(-1);
    }

    public CallGraph buildCallGraph(){
        CHACallGraph cg = new CHACallGraph(cha);
        try {
            cg.init(findEntryPoints(cha));
        } catch (CancelException e) {
            e.printStackTrace();
        }
        return cg;
    }

    protected Iterable<Entrypoint> findEntryPoints(IClassHierarchy cha){
        Set<AndroidEntryPointLocator.LocatorFlags> flags = new HashSet<>();
        flags.add(AndroidEntryPointLocator.LocatorFlags.INCLUDE_CALLBACKS);
        flags.add(AndroidEntryPointLocator.LocatorFlags.EP_HEURISTIC);
        flags.add(AndroidEntryPointLocator.LocatorFlags.CB_HEURISTIC);

        AndroidEntryPointLocator locator = new AndroidEntryPointLocator(flags);
        List<AndroidEntryPoint> androidEntries = locator.getEntryPoints(cha);

        List<Entrypoint> entries = new ArrayList<>();
        entries.addAll(androidEntries);

        return new Iterable<Entrypoint>(){
            @Override
            public void forEach(Consumer<? super Entrypoint> action) {
                entries.forEach(action);
            }

            @Override
            public Spliterator<Entrypoint> spliterator() {
                return entries.spliterator();
            }

            @Override
            public Iterator<Entrypoint> iterator() {
                return entries.iterator();
            }
        };
    }

    protected AnalysisScope makeAnalysisScope(String apk) {
        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
        //Set DexClassLoader as class loader.
        scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
        scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

        File exclusionsFile = new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS);

        try {
            //Set exclusions.
            InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class.getClassLoader()
                    .getResourceAsStream(exclusionsFile.getName());
            scope.setExclusions(new FileOfClasses(fs));
            fs.close();

            //Add Android libraries to analysis scope.
            String lib = LocalFileReader.androidJar(properties).getPath();
            if (lib.endsWith(".dex"))
                scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(lib)));
            else if (lib.endsWith(".jar"))
                scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(lib))));

            //Add a APK to analysis scope.
            if (apk.endsWith(".jar"))
                scope.addToScope(ClassLoaderReference.Application, new JarFileModule(new JarFile(new File(apk))));
            else if (apk.endsWith(".apk")) {
                scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(apk)));
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return scope;
    }

    protected IClassHierarchy buildClassHierarchy(AnalysisScope scope) throws ClassHierarchyException {
        return ClassHierarchy.make(scope);
    }
}
