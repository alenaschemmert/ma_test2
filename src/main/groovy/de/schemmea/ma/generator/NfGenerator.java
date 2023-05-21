package de.schemmea.ma.generator;

import com.sourceclear.gramtest.GeneratorVisitor;
import com.sourceclear.gramtest.bnfLexer;
import com.sourceclear.gramtest.bnfParser;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.schemmea.ma.utils.Configuration;
import de.schemmea.ma.utils.FileResourcesUtils;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class NfGenerator extends Generator<String> {
    private final int generateNumber = 1;
    private final int depth = 4;
    private final int max = 4;
    private final int min = 1;

    final BlockingQueue<String> queue = new SynchronousQueue<>();
    InputStream file = null;
    Lexer lexer = null;
    List<String> scripts;

    private String magicString = "382z9dfiusdpuzn5934magicstring9834n945zp9";

    public NfGenerator() {
        super(String.class);
        System.out.println("Generator - ctor");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        scripts = loadScriptFiles();
    }

    @Override
    public String generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {

        System.out.println("Generator - generate");

        try {
            file = new FileResourcesUtils().getResourceFileAsStream("/nextflow/bnfs/nextflow.bnf");

            lexer = new bnfLexer(new ANTLRInputStream(file));

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ParserRuleContext tree = new bnfParser(tokens).rulelist();

            GeneratorVisitor extractor = new GeneratorVisitor(generateNumber, depth, min, max, true, sourceOfRandomness);
            extractor.visit(tree);
            List<String> generatedTests = extractor.getTests();

            //  int idx = generatedTests.size() > 1 ? sourceOfRandomness.nextInt(0, generatedTests.size() - 1) : 0;
            String genTest = generatedTests.get(0);


            return replaceMagicStringWithRandomScript(genTest, sourceOfRandomness);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    private String replaceMagicStringWithRandomScript(String testCase, SourceOfRandomness sourceOfRandomness) {
        String replaced = testCase;
        if (scripts.size() > 0) {
            int count = replaced.split(magicString).length - 1;
            for (int i = 0; i < count; i++) {
                int template = new Random().nextInt(scripts.size());

                try {
                    template = sourceOfRandomness.nextInt(scripts.size());
                } catch (Exception e) {
                    System.out.println("dafuq - end of random");
                }
                String filename = scripts.get(template);
                replaced = replaced.replaceFirst(magicString, filename);
            }
        }

        String processcalls = String.join(" | ", collectProcessNames(replaced)) + "";

        replaced = replaced.replace("spaceholder", processcalls);

        return replaced;
    }

    private List<String> collectProcessNames(String testcase) {
        List<String> processnames = new ArrayList<>();

        String[] split1 = testcase.split("process");

        for (int i = 1; i < split1.length; i++) {
            processnames.add(split1[i].split("\\{")[0]);
        }

        return processnames;

    }


    private List<String> loadScriptFiles() {
        return new FileResourcesUtils().getResourceFiles(Configuration.TEMPLATE_SOURCE_PATH);
    }


}