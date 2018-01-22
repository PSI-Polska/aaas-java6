import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length != 1) {
            log.error("Usage: java -jar connection-checker.jar <ip-address>");
            return;
        }

        String address = args[0];
        log.info("Trying {}", address);

        RConnection conn = null;
        try {
            conn = new RConnection(address, 6311);
            conn.voidEval("print(\"TEST\")");
            log.info("OK");
        } catch (RserveException ex) {
            log.error("FAILED", ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
