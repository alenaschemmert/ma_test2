package de.schemmea.ma

import com.beust.jcommander.JCommander
import de.schemmea.ma.generator.Configuration
import de.schemmea.ma.guidance.FileAwareZestGuidance
import de.schemmea.ma.utils.Args
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance
import edu.berkeley.cs.jqf.fuzz.guidance.Result
import edu.berkeley.cs.jqf.fuzz.junit.*
import org.apache.commons.lang.StringUtils

import java.nio.file.Paths
import java.time.Duration

class TestExecutor {

    private static Args ARGS = new Args();
    private static Set<String> stackTraces = new HashSet<>();
    private static Map<String, Integer> nameCountMap = new HashMap<>();

    static File logfile = new File(Paths.get(Configuration.EXCEPTION_LOG_FILE).toUri());

    static void main(String... args) {

        var commander = new JCommander(ARGS)
        commander.parse(args);
        commander.setProgramName("TestingNF")

        String testname = "testWorkflow"
        Class testclass = NfTest.class

        String errorDir = Configuration.ERROR_DIR;

        boolean iswindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (iswindows && !errorDir.startsWith("C:") && !errorDir.startsWith("/")) errorDir = "/" + errorDir;

        println "Testing $testclass.name # $testname $ARGS.iteration times, duration: $ARGS.durationInSeconds s"

        File errorDirectory = Paths.get(errorDir).toFile();
        if (!errorDirectory.exists()) {
            errorDirectory.mkdir();
        }

        Guidance guidance = new FileAwareZestGuidance(testname,
                Duration.ofSeconds(ARGS.durationInSeconds),
                ARGS.iteration,
                errorDirectory,
                new Random(),
                TestExecutor::handleResult)

        GuidedFuzzing.run(testclass, testname, guidance, System.out)

        System.out.println(String.format("Covered %d edges.", guidance.getTotalCoverage().getNonZeroCount()));
        println "Tested $testclass.name#$testname $ARGS.iteration times, duration: $ARGS.durationInSeconds s"
    }


    private static void handleResult(Object[] files, Result result, Throwable throwable) {
        if (result == Result.FAILURE && files.length == 1 && files[0] instanceof File) {
            var mainFile = (File) files[0];
            String name;
            String stackTrace;
            if (throwable == null) {
                name = "NONE";
                stackTrace = StringUtils.EMPTY;
            } else {
                name = throwable.getClass().getName();
                stackTrace = throwableToStacktraceText(throwable);
            }
            boolean unique = !stackTraces.contains(stackTrace);
            if (unique) {
                stackTraces.add(stackTrace);
            }

            //count exception overall general
            var count = nameCountMap.getOrDefault(name, 0);
            nameCountMap.put(name, ++count);

            //write to csv : exception, unique, file name
            FileWriter writer = new FileWriter(logfile, true);
            writer.append(name + ";" + unique + ";" + count + ";" + mainFile.getName() + ";" + Configuration.newline);
            writer.close();

        }else if (result == Result.SUCCESS && files.length == 1 && files[0] instanceof File){
            //delete file because no error was thrown
            var mainFile = (File) files[0];
            mainFile.delete();
        }
    }

    private static String throwableToStacktraceText(Throwable throwable) {
        var writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}

