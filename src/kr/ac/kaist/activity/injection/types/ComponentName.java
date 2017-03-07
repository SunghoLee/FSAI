package kr.ac.kaist.activity.injection.types;

import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.debug.Assertions;

/**
 * Created by leesh on 27/02/2017.
 */
public class ComponentName {
    public static TypeName TYPE_NAME = TypeName.findOrCreate("Landroid/content/ComponentName");
    public static Selector CREATE_RELATIVE_SELECTOR1 = Selector.make("createRelative(Ljava/lang/String;Ljava/lang/String;)Landroid/content/ComponentName;");
    public static Selector CREATE_RELATIVE_SELECTOR2 = Selector.make("createRelative(Landroid/content/Context;Ljava/lang/String;)Landroid/content/ComponentName;");

    public enum InitSelector{
        INIT1(Selector.make("<init>(Landroid/content/Context;Ljava/lang/Class;)V")),
        INIT2(Selector.make("<init>(Landroid/content/Context;Ljava/lang/String;)V")),
        INIT3(Selector.make("<init>(Landroid/os/Parcel;)V")),
        INIT4(Selector.make("<init>(Ljava/lang/String;Landroid/os/Parcel;)V")),
        INIT5(Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;)V")),
        ;

        private Selector s;

        InitSelector(Selector s){
            this.s = s;
        }

        public Selector getSelector(){
            return s;
        }

        static public InitSelector match(Selector s){
            for(InitSelector i : InitSelector.values()){
                if(i.getSelector().equals(s))
                    return i;
            }
            Assertions.UNREACHABLE("It is not a ComponentName init selector: " + s);
            return null;
        }
    }

}
