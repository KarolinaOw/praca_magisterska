package pl.karolinamichalska.logo.data;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;

public class SampleDataGen {

    private static final String ALPHABET = "ACGT";
    private static final Set<Character> ALPHABET_CHARACTERS = Set.of('A', 'C', 'G', 'T');

    private final String outputPrefix;
    private final int sequenceCount;
    private final int sequenceLength;
    private final double desiredVariability;

    public SampleDataGen(String outputPrefix, int sequenceCount, int sequenceLength, double desiredVariability) {
        Preconditions.checkArgument(
                desiredVariability >= 0 && desiredVariability <= 1,
                "Variability must be between 0 and 1");
        this.outputPrefix = requireNonNull(outputPrefix);
        this.sequenceCount = sequenceCount;
        this.sequenceLength = sequenceLength;
        this.desiredVariability = desiredVariability;
    }

    public void generate() {
        Path file;
        try {
            file = Files.createFile(Path.of("%s-%s-%s.csv".formatted(outputPrefix, sequenceCount, sequenceLength)));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(file.toFile())),
                StandardCharsets.UTF_8)) {
            doGenerate(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void doGenerate(OutputStreamWriter writer) throws IOException {
        final String referenceSeq = RandomStringUtils.random(sequenceLength, ALPHABET);
        double varyingCharCount = getVaryingCharCount();
        writeSequence(writer, referenceSeq);
        for (int i = 1; i < sequenceCount; i++) {
            Map<Integer, String> substitutions = new HashMap<>();
            for (int j = 0; j < varyingCharCount; j++) {
                int position = RandomUtils.nextInt(0, sequenceLength);
                substitutions.put(position, getSubstituteChar(referenceSeq, position));
            }
            String sequence = generateSeq(referenceSeq, substitutions);
            writeSequence(writer, sequence);
        }
    }

    private String getSubstituteChar(String referenceSeq, int position) {
        char refChar = referenceSeq.charAt(position);
        String reducedAlphabet = ALPHABET_CHARACTERS.stream()
                .filter(ch -> !ch.equals(refChar))
                .collect(Collector.of(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append,
                        StringBuilder::toString));
        return RandomStringUtils.random(1, reducedAlphabet);
    }

    private String generateSeq(String referenceSeq, Map<Integer, String> substitutions) {
        char[] chars = referenceSeq.toCharArray();
        substitutions.forEach(
                (position, replacement) -> chars[position] = replacement.charAt(0));
        return new String(chars);
    }

    private void writeSequence(OutputStreamWriter writer, String sequence) throws IOException {
        writer.append(sequence).append("\n");
    }

    private double getVaryingCharCount() {
        double calculated = sequenceLength * desiredVariability;
        return calculated > 0 ? calculated : 1;
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option numberOfSequences = new Option("n", "number", true, "Number of sequences");
        options.addOption(numberOfSequences);
        Option lengthOfSequence = new Option("l", "length", true, "Length of a single sequence");
        options.addOption(lengthOfSequence);
        Option outputPrefix = new Option("p", "prefix", true, "Output file prefix");
        options.addOption(outputPrefix);
        Option desiredVariability = new Option("v", "variability", true, "Desired variability between 0 and 1");
        options.addOption(desiredVariability);

        DefaultParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        SampleDataGen generator = new SampleDataGen(
                cmd.getOptionValue(outputPrefix, "sample"),
                Integer.parseInt(cmd.getOptionValue(numberOfSequences)),
                Integer.parseInt(cmd.getOptionValue(lengthOfSequence)),
                Double.parseDouble(cmd.getOptionValue(desiredVariability)));
        generator.generate();
    }
}
