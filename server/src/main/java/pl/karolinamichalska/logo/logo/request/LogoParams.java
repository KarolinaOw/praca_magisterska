package pl.karolinamichalska.logo.logo.request;

public record LogoParams(
        SequenceType seqType,
        long firstPosNumber,
        String title,
        String xAxis,
        String yAxis,
        ColorScheme colors
) {
}
