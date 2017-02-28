package kr.ac.kaist.activity.injection.types;

/**
 * Created by leesh on 21/02/2017.
 */
public class Activity {
    private final LaunchMode launchMode;
    private final String taskAffinity;

    public enum LaunchMode {
        STANDARD("standard"),
        SINGLETOP("singleTop"),
        SINGLETASK("singleTask"),
        SINGLEINSTANCE("singleInstance"),
        ;

        private final String s;

        LaunchMode(String s){
            this.s = s;
        }

        public String getValue(){
            return s;
        }

        public static LaunchMode match(String mode){
            for(LaunchMode l : LaunchMode.values())
                if(l.getValue().equals(mode))
                    return l;

            try {
                throw new Exception("the mode is not a LaunchMode attribute: " + mode);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String toString(){
            return this.getValue();
        }
    }

    public Activity(LaunchMode launchMode, String taskAffinity){
        this.launchMode = launchMode;
        this.taskAffinity = taskAffinity;
    }

    public LaunchMode getLaunchMode(){
        return this.launchMode;
    }

    public String getTaskAffinity(){
        return this.taskAffinity;
    }
}
