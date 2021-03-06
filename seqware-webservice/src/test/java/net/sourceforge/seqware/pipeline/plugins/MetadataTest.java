/*
 * Copyright (C) 2012 SeqWare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.seqware.pipeline.plugins;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Runs the tests for the Metadata plugin indicated on this
 * page:https://wiki.oicr.on.ca/x/Jga5Ag
 *
 * @author mtaschuk
 */
public class MetadataTest extends PluginTest {

    private ByteArrayOutputStream outStream = null;
    private ByteArrayOutputStream errStream = null;
    private Pattern swidPattern = Pattern.compile("SWID: ([\\d]+)");
    private Pattern errorPattern = Pattern.compile("ERROR|error|Error|FATAL|fatal|Fatal|WARN|warn|Warn");
    private PrintStream systemErr = System.err;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        instance = new Metadata();
        instance.setMetadata(metadata);

        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        PrintStream pso = new PrintStream(outStream);
        PrintStream pse = new PrintStream(errStream) {
            @Override
            public PrintStream append(CharSequence csq) {
//                systemErr.append(csq);
                return super.append(csq);
            }

            @Override
            public void print(String s) {
//                systemErr.print(s);
                super.print(s);
            }
        };
        System.setOut(pso);
        System.setErr(pse);
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }

    public MetadataTest() {
    }

    public String getOut() {
        return parsePrintStream(outStream);
    }

    public String getErr() {
        return parsePrintStream(errStream);
    }

    public String parsePrintStream(ByteArrayOutputStream stream) {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = null;
        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(stream.toByteArray());
            r = new BufferedReader(new InputStreamReader(inStream));

            String s = r.readLine();
            while (s != null) {
                s = s.trim();
                //remove any blank lines
                if (s.isEmpty()) {
                    s = r.readLine();
                    continue;
                }
                if (s.endsWith("[")) {
                    while (s != null && !s.contains("]")) {
                        sb.append(s);
                        s = r.readLine();
                    }
                }
                sb.append(s).append("\n");
                s = r.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(MetadataTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ex) {
                    systemErr.println("Couldn't close System.out reader" + ex.getMessage());
                }
            }
        }

        return sb.toString();
    }

    private void checkErrors(String s) {
        Matcher matcher = errorPattern.matcher(s);
        systemErr.println("~~~~~~~~~~" + s);
        Assert.assertFalse("Output contains errors:" + s, matcher.find());
//        systemErr.println("~~~~~~~~~~"+matcher.group());

    }

    private void checkFields(Map<String, String> expectedFields) {
        String out = getOut();
        for (String s : out.split("\n")) {
            String[] tokens = s.split("\t");
            Assert.assertTrue("Unknown field exists: " + s, expectedFields.containsKey(tokens[0]));
            Assert.assertEquals("Field has different parameter type than expected", expectedFields.get(tokens[0]), tokens[1]);
        }
    }

    @Test
    public void testListAllTables() {
        systemErr.println("Test List all Tables\n");
        launchPlugin("--list-tables");
        String output = getOut();
        String[] tables = new String[]{"TableName", "study", "experiment", "sample", "ius", "lane", "sequencer_run"};
        LinkedList<String> stuff = new LinkedList(Arrays.asList(output.split("\n")));
        for (String table : tables) {
            int index = stuff.indexOf(table);
            if (index >= 0) {
                stuff.remove(index);
            } else {
                Assert.fail("Missing a table:" + table);
            }
        }
        while (!stuff.isEmpty()) {
            String s = stuff.poll();
            Assert.fail("There are extra tables listed: " + s);
        }
    }

    @Test
    public void testListStudyFields() {
        systemErr.println("Test List study fields");

        Map<String, String> expectedFields = new HashMap<String, String>();
        expectedFields.put("Field", "Type");
        expectedFields.put("title", "String");
        expectedFields.put("description", "String");
        expectedFields.put("accession", "String");
        expectedFields.put("center_name", "String");
        expectedFields.put("center_project_name", "String");
        expectedFields.put("study_type", "Integer");

        launchPlugin("--table", "study", "--list-fields");
        checkFields(expectedFields);
    }

    @Test
    public void testListExperimentFields() {
        systemErr.println("Test List experiment fields");

        Map<String, String> expectedFields = new HashMap<String, String>();
        expectedFields.put("Field", "Type");
        expectedFields.put("title", "String");
        expectedFields.put("description", "String");
        expectedFields.put("study_accession", "Integer");
        expectedFields.put("platform_id", "Integer");


        launchPlugin("--table", "experiment", "--list-fields");
        checkFields(expectedFields);
    }

    @Test
    public void testListSampleFields() {
        systemErr.println("Test List sample fields");
        Map<String, String> expectedFields = new HashMap<String, String>();
        expectedFields.put("Field", "Type");
        expectedFields.put("title", "String");
        expectedFields.put("description", "String");
        expectedFields.put("experiment_accession", "Integer");
        expectedFields.put("organism_id", "Integer");

        launchPlugin("--table", "sample", "--list-fields");

        checkFields(expectedFields);
    }

    @Test
    public void testListSequencerRunFields() {
        systemErr.println("Test List sequencer run fields");

        Map<String, String> expectedFields = new HashMap<String, String>();
        expectedFields.put("Field", "Type");
        expectedFields.put("name", "String");
        expectedFields.put("description", "String");
        expectedFields.put("paired_end", "Boolean");
        expectedFields.put("skip", "Boolean");
        expectedFields.put("platform_accession", "Integer");



        launchPlugin("--table", "sequencer_run", "--list-fields");

        checkFields(expectedFields);
    }

    @Test
    public void testListLaneFields() {
        systemErr.println("Test List Lane fields");

        Map<String, String> expectedFields = new HashMap<String, String>();
        expectedFields.put("Field", "Type");
        expectedFields.put("name", "String");
        expectedFields.put("description", "String");
        expectedFields.put("cycle_descriptor", "String");
        expectedFields.put("skip", "Boolean");
        expectedFields.put("sequencer_run_accession", "Integer");
        expectedFields.put("study_type_accession", "Integer");
        expectedFields.put("library_strategy_accession", "Integer");
        expectedFields.put("library_selection_accession", "Integer");
        expectedFields.put("library_source_accession", "Integer");
        launchPlugin("--table", "lane", "--list-fields");

        checkFields(expectedFields);
    }

    @Test
    public void testListIUSFields() {
        systemErr.println("Test List IUS fields");

        Map<String, String> expectedFields = new HashMap<String, String>();
        expectedFields.put("Field", "Type");
        expectedFields.put("name", "String");
        expectedFields.put("description", "String");
        expectedFields.put("barcode", "String");
        expectedFields.put("skip", "Boolean");
        expectedFields.put("sample_accession", "Integer");
        expectedFields.put("lane_accession", "Integer");

        launchPlugin("--table", "ius", "--list-fields");

        checkFields(expectedFields);
    }

    @Test
    public void testMatcher() {
        String string = "[SeqWare Pipeline] ERROR [2012/11/01 15:53:51] | "
                + "MetadataWS.findObject with search string /288023 encountered error "
                + "Internal Server Error\nExperiment: null\nSWID: 6740";
        Matcher match = swidPattern.matcher(string);
        Assert.assertTrue(match.find());
        Assert.assertEquals("6740", match.group(1));
        match = errorPattern.matcher(string);
        Assert.assertTrue(match.find());
        Assert.assertEquals("ERROR", match.group(0));

    }
    private String studyAccession = null;

    @Test
    public void testCreateStudy() {
        launchPlugin("--table", "study", "--create",
                "--field", "title::alal" + System.currentTimeMillis(),
                "--field", "description::alal",
                "--field", "accession::1235",
                "--field", "center_name::oicr",
                "--field", "center_project_name::mine",
                "--field", "study_type::1");
        String s = getOut();
        studyAccession = getAndCheckSwid(s);
    }
    private String experimentAccession = null;

    @Test
    public void testCreateExperiment() {
        String sAcc = studyAccession;
        if (sAcc == null) {
            sAcc = "120";
        }

        launchPlugin("--table", "experiment", "--create",
                "--field", "study_accession::" + sAcc,
                "--field", "title::experimenttitle" + System.currentTimeMillis(),
                "--field", "description::\"Experiment Description\"",
                "--field", "platform_id::9");
        String s = getOut();
        experimentAccession = getAndCheckSwid(s);
    }
    private String sampleAccession = null;

    @Test
    public void testCreateSample() {
        String eAcc = experimentAccession;
        if (eAcc == null) {
            eAcc = "834";
        }

        launchPlugin("--table", "sample", "--create",
                "--field", "experiment_accession::" + eAcc,
                "--field", "title::sampletitle",
                "--field", "description::sampledescription",
                "--field", "organism_id::31");
        String s = getOut();
        sampleAccession = getAndCheckSwid(s);
    }
    private String runAccession = null;

    @Test
    public void testCreateSequencerRun() {
        launchPlugin("--table", "sequencer_run", "--create",
                "--field", "name::SR" + System.currentTimeMillis(),
                "--field", "description::SRD",
                "--field", "platform_accession::20",
                "--field", "paired_end::true",
                "--field", "skip::false");
        String s = getOut();
        runAccession = getAndCheckSwid(s);
    }
    private String laneAccession = null;

    @Test
    public void testCreateLane() {
        String rAcc = runAccession;
        if (rAcc == null) {
            rAcc = "4715";
        }

        launchPlugin("--table", "lane", "--create",
                "--field", "name::lane",
                "--field", "description::description",
                "--field", "cycle_descriptor::{F*120}{..}{R*120}",
                "--field", "sequencer_run_accession::" + rAcc,
                "--field", "library_strategy_accession::1",
                "--field", "study_type_accession::1",
                "--field", "library_selection_accession::1",
                "--field", "library_source_accession::1",
                "--field", "skip::false");
        String s = getOut();
        laneAccession = getAndCheckSwid(s);
    }

    @Test
    public void testCreateIUS() {
        String lAcc = laneAccession;
        if (lAcc == null) {
            lAcc = "4707";
        }
        String sAcc = sampleAccession;
        if (sAcc == null) {
            sAcc = "4760";
        }

        launchPlugin("--table", "ius", "--create",
                "--field", "name::ius",
                "--field", "description::des",
                "--field", "lane_accession::" + lAcc,
                "--field", "sample_accession::" + sAcc,
                "--field", "skip::false",
                "--field", "barcode::NoIndex");
        String s = getOut();
        getAndCheckSwid(s);

    }

    //////////////////////////////////////////////////Negative tests
    // re-enabled to test SEQWARE-1331
    @Test
    public void testCreateSampleFail() {
        String eAcc = "8350";

        instance.setParams(Arrays.asList("--table", "sample", "--create",
                "--field", "experiment_accession::" + eAcc,
                "--field", "title::sampletitle",
                "--field", "description::sampledescription",
                "--field", "organism_id::31"));
        checkExpectedFailure();
    }

    // See SEQWARE-1374
//    @Test
//    public void testCreateStudyFail() {
//        instance.setParams(Arrays.asList("--table", "study", "--create",
//                "--field", "title::alal" + System.currentTimeMillis(),
//                "--field", "description::alal",
//                "--field", "accession::1235",
//                "--field", "center_name::oicr",
//                "--field", "center_project_name::mine",
//                "--field", "study_type::42"));
//        checkExpectedFailure();
//    }

    // See SEQWARE-1374
//    @Test
//    public void testCreateExperimentFail() {
//        String sAcc = "120";
//
//        instance.setParams(Arrays.asList("--table", "experiment", "--create",
//                "--field", "study_accession::" + sAcc,
//                "--field", "title::experimenttitle" + System.currentTimeMillis(),
//                "--field", "description::\"Experiment Description\"",
//                "--field", "platform_id::42"));
//        checkExpectedFailure();
//    }

    // See SEQWARE-1374
//    @Test
//    public void testCreateSequencerRunFail() {
//        instance.setParams(Arrays.asList("--table", "sequencer_run", "--create",
//                "--field", "name::SR" + System.currentTimeMillis(),
//                "--field", "description::SRD",
//                "--field", "platform_accession::20000",
//                "--field", "paired_end::true",
//                "--field", "skip::false"));
//        checkExpectedFailure();
//    }

    @Test
    public void testCreateLaneFail() {
        String rAcc = "20000";

        instance.setParams(Arrays.asList("--table", "lane", "--create",
                "--field", "name::lane",
                "--field", "description::description",
                "--field", "cycle_descriptor::{F*120}{..}{R*120}",
                "--field", "sequencer_run_accession::" + rAcc,
                "--field", "library_strategy_accession::1",
                "--field", "study_type_accession::1",
                "--field", "library_selection_accession::1",
                "--field", "library_source_accession::1",
                "--field", "skip::false"));
        checkExpectedFailure();
    }

    @Test
    public void testCreateIUSWrongLaneFail() {
        String lAcc = laneAccession;
        if (lAcc == null) {
            lAcc = "4707";
        }
        String sAcc = sampleAccession;
        if (sAcc == null) {
            sAcc = "4760";
        }
        // set an invalid lAcc 
        lAcc = "20000";

        instance.setParams(Arrays.asList("--table", "ius", "--create",
                "--field", "name::ius",
                "--field", "description::des",
                "--field", "lane_accession::" + lAcc,
                "--field", "sample_accession::" + sAcc,
                "--field", "skip::false",
                "--field", "barcode::NoIndex"));
        checkExpectedFailure();
    }
    
    @Test
    public void testCreateIUSWrongSampleFail() {
        String lAcc = laneAccession;
        if (lAcc == null) {
            lAcc = "4707";
        }
        String sAcc = sampleAccession;
        if (sAcc == null) {
            sAcc = "4760";
        }
        // set an invalid accession
        sAcc = "20000";

        instance.setParams(Arrays.asList("--table", "ius", "--create",
                "--field", "name::ius",
                "--field", "description::des",
                "--field", "lane_accession::" + lAcc,
                "--field", "sample_accession::" + sAcc,
                "--field", "skip::false",
                "--field", "barcode::NoIndex"));
        checkExpectedFailure();
    }

    ////////////////////////////////////////////////////////////////////////////
    private String getAndCheckSwid(String s) throws NumberFormatException {
        Matcher match = swidPattern.matcher(s);
        Assert.assertTrue("SWID not found in output.", match.find());
        String swid = match.group(1);
        Assert.assertFalse("The SWID was empty", swid.trim().isEmpty());
        Integer.parseInt(swid.trim());
        return swid;
    }
    @Rule
    public TestRule watchman = new TestWatcher() {
        //This doesn't catch logs that are sent to Log4J
        @Override
        protected void succeeded(Description d) {
            // do not fail on tests that intend on failing
            if (!d.getMethodName().endsWith("Fail")) {
                checkErrors(getErr());
                checkErrors(getOut());
            }
        }
    };

    /**
     * Run an instance with an error and/or failure expected
     */
    private void checkExpectedFailure() {
        checkReturnValue(ReturnValue.SUCCESS, instance.parse_parameters());
        checkReturnValue(ReturnValue.SUCCESS, instance.init());
        checkReturnValue(ReturnValue.FAILURE, instance.do_run());
    }
}
