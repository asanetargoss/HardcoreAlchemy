package targoss.hardcorealchemy.util;

public class StringUtil {
    public static String trimBefore(String in) {
        String out = (in + ".").trim();
        out = out.substring(0, out.length() - 1);
        return out;
    }
    
    public static String trimAfter(String in) {
        String out = ("." + in).trim();
        out = out.substring(1, out.length());
        return out;
    }
}
