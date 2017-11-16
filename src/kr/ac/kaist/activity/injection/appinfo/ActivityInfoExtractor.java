package kr.ac.kaist.activity.injection.appinfo;

import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.activity.injection.decompile.AndroidDecompiler;
import kr.ac.kaist.activity.injection.decompile.parser.ManifestParser;
import kr.ac.kaist.activity.injection.types.Activity;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by leesh on 21/02/2017.
 */
public class ActivityInfoExtractor {
    private static final String TASK_AFFINITY_ATTRIBUTE = "android:taskAffinity";
    private static final String LAUNCH_MODE_ATTRIBUTE = "android:launchMode";
    private static final String ROOT_ELEMENT_NAME = "manifest";
    private static final String ACTIVITY_ELEMENT_NAME = "activity";
    private static final String PACKAGE_NAME_ATTRIBUTE = "package";
    private static final String ACTIVITY_NAME_ATTRIBUTE = "android:name";

    private final String apk;
    private String appPackageName;
    private String decompDir;

    public ActivityInfoExtractor(String apk){
        this.apk = apk;
    }

    public Set<ActivityInfo> extract() throws ParserConfigurationException, SAXException, IOException {
        this.decompDir = decomp(apk);
        ManifestParser.Element root = parse(this.decompDir);
        this.appPackageName = getPackageName(root);
        return extractActivitiesInfo(root);
    }

    private String decomp(String apk){
        return AndroidDecompiler.decompile(apk);
    }

    private ManifestParser.Element parse(String path) throws IOException, SAXException, ParserConfigurationException {
        ManifestParser p = new ManifestParser();
        return p.parse(path + File.separator + ManifestParser.MANIFEST_NAME);
    }

    private String getPackageName(ManifestParser.Element root){
        if(!root.getName().equals(ROOT_ELEMENT_NAME)){
            Assertions.UNREACHABLE("Package name is in root element only: " + root.getName());
        }

        return root.getAttributes().getValue(PACKAGE_NAME_ATTRIBUTE);
    }

    public String getAppPackageName(){
        return "L" + appPackageName.replace(".", "/");
    }

    private boolean isAbsolutePath(StringTokenizer path){
        String sourceDir = this.decompDir + File.separator + "smali";

        while(path.hasMoreTokens()){
            String target = path.nextToken();
            //if the path is directory
            if(path.hasMoreTokens()){
                sourceDir += File.separator + target;
                File targetDir = new File(sourceDir);
                if(!targetDir.exists())
                    return false;
            }else{ // if the path is a file
                File targetFile = new File(sourceDir + File.separator + target + ".smali");
                if(targetFile.exists())
                    return true;
                else
                    return false;
            }
        }
        return false;
    }

    public void removeDecompDir(){
        ProcessBuilder pb = new ProcessBuilder("rm", "-r", decompDir);
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDecompDir(){
        return decompDir;
    }

    private Set<ActivityInfo> extractActivitiesInfo(ManifestParser.Element elem){
        final Set<ActivityInfo> res = new HashSet<>();
        elem.forEach((element) -> {
                //if element is an activity
                if(element.getName().equals(ACTIVITY_ELEMENT_NAME)){
                    String packageName = null;
                    String activityName = null;
                    String taskAffinity = null;
                    Activity.LaunchMode launchMode;

                    ManifestParser.Attributes attrs = element.getAttributes();

                    //find package and own name of activity
                    if(attrs.contains(ACTIVITY_NAME_ATTRIBUTE)){
                        String fullName = attrs.getValue(ACTIVITY_NAME_ATTRIBUTE);

                        // if it is relative path
                        if(fullName.startsWith(".")) {

                            String relatedName = fullName.substring(0, fullName.lastIndexOf("."));
                            if(relatedName.length() > 0){
                                packageName = appPackageName + relatedName;
                            }else
                                packageName = appPackageName;

                        }else {
                            // if it is absolute path
                            if(!fullName.contains("."))
                                packageName = appPackageName;
                            else {
                                String path = fullName.substring(0, fullName.lastIndexOf("."));
                                // if the file does not exist, we handle the package as absolute path
                                if (isAbsolutePath(new StringTokenizer(fullName, ".")) || !isAbsolutePath(new StringTokenizer(appPackageName + "." + fullName, ".")))
                                    packageName = path;
                                else {
                                    packageName = appPackageName + "." + path;
                                }
                            }
                        }
                        if(fullName.contains("."))
                            activityName = fullName.substring(fullName.lastIndexOf(".")+1);
                        else
                            activityName = fullName;
                    }else{
                        Assertions.UNREACHABLE("Every activity element must have its own name.");
                    }

                    //find taskAffinity attribute. if it is not, the package name of the application is assigned to taskAffinity by default.
                    if(attrs.contains(TASK_AFFINITY_ATTRIBUTE)){
                        taskAffinity = attrs.getValue(TASK_AFFINITY_ATTRIBUTE);
                    }else{
                        taskAffinity = appPackageName;
                    }

                    //find launchMode attribute. if it is not, standard is assigned to launchMode by default.
                    if(attrs.contains(LAUNCH_MODE_ATTRIBUTE)){
                        launchMode = Activity.LaunchMode.match(attrs.getValue(LAUNCH_MODE_ATTRIBUTE));
                    }else{
                        launchMode = Activity.LaunchMode.STANDARD;
                    }

                    res.add(new ActivityInfo(packageName, activityName, taskAffinity, launchMode));
                }
        });

        return res;
    }

    public static class ActivityInfo {
        private final String packageName;
        private final String activityName;
        private final String taskAffinity;
        private final Activity.LaunchMode launchMode;


        public ActivityInfo(String packageName, String activityName, String taskAffinity, Activity.LaunchMode launchMode){
            this.packageName = "L" + packageName.replace(".","/");
            this.activityName = activityName;
            if(!taskAffinity.equals(""))
                this.taskAffinity = "L" + taskAffinity.replace(".","/");
            else
                this.taskAffinity = "";
            this.launchMode = launchMode;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getActivityName() {
            return activityName;
        }

        public String getTaskAffinity() {
            return taskAffinity;
        }

        public Activity.LaunchMode getLaunchMode() {
            return launchMode;
        }

        @Override
        public String toString(){
            return "< " + packageName + ", " + activityName + ", " + taskAffinity + ", " + launchMode + " >";
        }
    }
}
