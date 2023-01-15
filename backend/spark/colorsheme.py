class ColorRule:

    def symbol_color(self, seq_index: init, symbol: str, rank: int) -> Optional[Color]:
        raise NotImplementedError  # pragma: no cover


class ColorScheme(ColorRule):
    def __init__(
            self,
            rules: List[ColorRule] = [],
            title: str = "",
            description: str = "",
            default_color: str = "black",
            alphabet: Alphabet = seq.generic_alphabet,
    ) -> None:

        self.rules = rules
        self.title = title
        self.description = description
        self.default_color = Color.from_string(default_color)
        self.alphabet = alphabet

        def symbol_color(self, seq_index: int, symbol: str, rank: int) -> Color:
            if symbol not in self.alphabet:
                raise KeyError("Colored symbol '%s' does not exist in alphabet." % symbol)

        for rule in self.rules:
            color = rule.symbol_color(seq_index, symbol, rank)
            if color is not None:
                return color

        return self.default_color



nucleotide = ColorScheme(
    [
        SymbolColor("G", "orange"),
        SymbolColor("TU", "red"),
        SymbolColor("C", "blue"),
        SymbolColor("A", "green"),
    ],
)

aminoAcids = ColorScheme(
    [
        SymbolColor("GSTYCQN", "green"),
        SymbolColor("KRH", "blue"),
        SymbolColor("DE", "red"),
        SymbolColor("AVLIPWFM", "black"),
    ],
)

monochrome = ColorScheme([])