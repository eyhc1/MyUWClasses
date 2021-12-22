package me.eyhc;
import me.eyhc.ui.LoginUI;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import java.util.List;

public class Start {
    public static void main(String[] args) {
        OptionParser opt = new OptionParser();
        opt.allowsUnrecognizedOptions();
        OptionSpec<String> netid = opt.accepts("netid").withRequiredArg().defaultsTo("");
        OptionSpec<String> password = opt.accepts("password").withRequiredArg().defaultsTo("");
        OptionSpec<Void> duoPrmpt = opt.accepts("disable2faDiag");
        OptionSpec<String> other = opt.nonOptions();
        OptionSet optset = opt.parse(args);
        List<String> unrecgArgs = optset.valuesOf(other);
        if (!unrecgArgs.isEmpty()) {
            System.out.println("<WARN> Ignored unrecognized arguments " + unrecgArgs);
        }
        new LoginUI(netid.value(optset), password.value(optset)).setPrompt2faEnabled(optset.has(duoPrmpt));
    }
}