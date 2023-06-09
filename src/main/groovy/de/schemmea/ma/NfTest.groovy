package de.schemmea.ma

import com.pholser.junit.quickcheck.From
import de.schemmea.ma.generator.NfGenerator
import de.schemmea.ma.generator.Configuration
import de.schemmea.ma.generator.WorkflowGenerator
import de.schemmea.ma.utils.FileResourcesUtils
import edu.berkeley.cs.jqf.fuzz.Fuzz
import edu.berkeley.cs.jqf.fuzz.JQF
import nextflow.cli.CmdRun
import nextflow.cli.Launcher
import nextflow.plugin.Plugins
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

import java.nio.file.Paths

@RunWith(JQF.class)
class NfTest {


    static int iteration = 0;


    @Before
    public void setup() {
        System.out.println("@Before");
        //is this called multiple times?
        if (iteration == 0) {
            new FileResourcesUtils().copyFilesToFolder(Configuration.TEMPLATE_SOURCE_PATH, Configuration.OUTPUT_TEMPLATE_PATH);
            new FileResourcesUtils().copyFilesToFolder(Configuration.DATA_SOURCE_PATH, Configuration.OUTPUT_DATA_PATH);

        }
    }

    @Fuzz
    public void testWorkflow(@From(WorkflowGenerator.class) File inputFile) {
        print Configuration.newline + "ITERATION " + ++iteration + Configuration.newline

        String filename = inputFile.getAbsolutePath();
        String[] orig_args2 = new String[]{"run", filename};
        List<String> args2 = List.of(filename);

        Launcher launcher = new Launcher().command(orig_args2)//.run();

        CmdRun myRunner = new CmdRun();
        myRunner.setArgs(args2);
        myRunner.setLauncher(launcher);

        myRunner.run();

        //nextflow clean ? <
    }

    @After
    public void cleanUp(){
        Plugins.stop()
    }

}

