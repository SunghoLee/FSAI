package kr.ac.kaist.activity.injection.analysis;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.ClassLoaderReference;

/**
 * Created by leesh on 27/04/2017.
 */
public class LibraryModel {
    public static boolean isLibrary(CGNode n){
        try {
            if (n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v4"))
                return true;
            else if (n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v7"))
                return true;
            else if (n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("com/google"))
                return true;
            if (n.getMethod().toString().contains("fakeRootMethod"))
                return false;
            else if (n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
                return true;
            return false;
        }catch(Exception e){
            return true;
        }
    }

    public static boolean isLibrary(IClass c){
        try {
            if(c.getName().getPackage().toString().startsWith("java/lang/reflect"))
                return true;
            if(c.getName().getPackage().toString().startsWith("android/support/v4"))
                return true;
            else if(c.getName().getPackage().toString().startsWith("android/support/v7"))
                return true;
            else if(c.getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
                return true;
            return false;
        }catch(Exception e){
            return true;
        }
    }

    public static boolean isApplication(CGNode n){
        if(n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v4"))
            return false;
        else if(n.getMethod().getDeclaringClass().getName().getPackage().toString().startsWith("android/support/v7"))
            return false;
        else if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return false;
        return true;
    }
}
