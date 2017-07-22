package kr.ac.kaist.activity.injection.analysis;

import kr.ac.kaist.activity.injection.types.Activity;
import kr.ac.kaist.activity.injection.types.Intent;

import java.util.Set;

/**
 * Created by leesh on 01/05/2017.
 */
public class LaunchingActInfo {
    private final String actName;
    private final Activity.LaunchMode launchMode;
    private final String taskAffinity;
    private final Set<Intent.Flag> flags;
    private final boolean isInter;
    private final boolean isField;
    private final boolean isArray;
    private final boolean called;

    public LaunchingActInfo(String actName, Activity.LaunchMode launchMode, String taskAffinity, Set<Intent.Flag> flags, boolean a, boolean b, boolean c, boolean d){
        this.actName = actName;
        this.launchMode = launchMode;
        this.taskAffinity = taskAffinity;
        this.flags = flags;
        this.isInter = a;
        this.isField = b;
        this.isArray = c;
        this.called = d;
    }

    @Override
    public String toString(){
        return actName + " < " + launchMode.getValue() + ", " + taskAffinity + ", " + flags + " > (" + isInter + " & " + isField + " & " + isArray + ")";
    }

    public String getActivityName(){
        return this.actName;
    }

    public Activity.LaunchMode getLaunchMode(){
        return this.launchMode;
    }

    public String getTaskAffinity(){
        return this.taskAffinity;
    }

    public Set<Intent.Flag> getFlags(){
        return this.flags;
    }

    public boolean isCalled(){return this.called;}


}
