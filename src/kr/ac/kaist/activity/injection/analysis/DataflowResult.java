package kr.ac.kaist.activity.injection.analysis;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 21/04/2017.
 */
public class DataflowResult {
    private final Point seed;
    private final Set<Point> points;

    public DataflowResult(Point seed, Set<Point> points){
        this.seed = seed;
        this.points = points;
    }

    public Point getSeed(){
        return this.seed;
    }

    public Set<Point> getCatchedPoints(){
        return this.points;
    }

    @Override
    public String toString(){
        String res = "#Seed: " + seed;
        Iterator<Point> iP = points.iterator();
        while(iP.hasNext())
            res += "\n=> " + iP.next();
        return res;
    }
}
