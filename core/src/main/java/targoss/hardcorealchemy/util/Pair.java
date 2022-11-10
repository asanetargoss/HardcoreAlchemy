package targoss.hardcorealchemy.util;

public final class Pair<FIRST, SECOND> {
    public final FIRST first;
    public final SECOND second;
    
    public Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public int hashCode() {
        // Note that this produces bad results for sequential values.
        // But... if you know your values are sequential, then why are you using a hash map?
        // If more "random" values are desired, feed the output of this through an LCG.
        return first.hashCode() ^ second.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> op = (Pair<?, ?>)o;
        if (first == null) {
            if (second == null) {
                return op.first == null && op.second == null;
            }
            else {
                return op.first == null && second.equals(op.second);
            }
        }
        else {
            if (second == null) {
                return op.second == null && first.equals(op.first);
            }
            else {
                return second.equals(op.second) && first.equals(op.first);
            }
        }
    }
}
