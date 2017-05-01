package kr.ac.kaist.activity.injection.analysis;

import kr.ac.kaist.activity.injection.types.Activity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 01/05/2017.
 */
public class ActivityInjectionDetector {
    private final Set<LaunchingActInfo> infoSet;
    private final String packageName;

    public ActivityInjectionDetector(Set<LaunchingActInfo> infoSet, String packageName){
        this.infoSet = infoSet;
        this.packageName = packageName;
    }

    public Set<LaunchingActInfo> detect(){
        Set<LaunchingActInfo> res = new HashSet<>();

        for(LaunchingActInfo info : infoSet){
            if(isInjectionCase(packageName, info))
                res.add(info);
        }

        return res;
    }

    private boolean isInjectionCase(String packageName, LaunchingActInfo i){
        if(!i.getTaskAffinity().equals(packageName)){
            // 1, 2,
            if(i.getLaunchMode().equals(Activity.LaunchMode.SINGLETASK))
                return true;

        }
        return false;
    }

}
