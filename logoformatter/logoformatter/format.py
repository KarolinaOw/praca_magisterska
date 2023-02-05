import sys
import uuid
from datetime import datetime
from importlib.resources import read_text
from string import Template
import os

dna_alphabet = 'ACGT'
rna_alphabet = 'ACGU'

seq_from = None
seq_to = None

contrib = []

line_index = 0
for line in sys.stdin:
    if line_index == 0:
        for config in line.split(";"):
            keyValue = config.split("=")
            if keyValue[0] == 'seq_from':
                seq_from = int(keyValue[1])
    else:
        contrib.append(list(map(lambda v: float(v), line.split(","))))
    line_index += 1

seq_len = line_index - 1

if seq_from is None:
    seq_from = 0
if seq_from >= seq_len:
    raise ValueError

seq_to = seq_len - 1

std_sizes = {"small": 5.4, "medium": 5.4 * 2, "large": 5.4 * 3}

creation_date = datetime.now().isoformat(" ")
logo_title = "Sequence Logo"
logo_start = seq_from
logo_end = seq_to
total_stacks = logo_end - logo_start + 1
stacks_per_line = min(40, total_stacks)
stack_width = std_sizes["large"]
stack_aspect_ratio = 5
stack_height = stack_width * stack_aspect_ratio
fontsize = 10
title_fontsize = 12
number_fontsize = 8
small_fontsize = 6
title_height = title_fontsize
xaxis_label_height = fontsize
line_margin_left = fontsize * 3.0
line_margin_right = fontsize * 1.5
line_margin_top = 4
line_margin_bottom = number_fontsize * 1.5
line_width = stack_width * stacks_per_line + line_margin_left + line_margin_right
line_height = stack_height + line_margin_top + line_margin_bottom
logo_margin = 2
logo_width = int(2 * logo_margin + line_width)
lines_per_logo = 1 + ((total_stacks - 1) // stacks_per_line)
logo_height = int(2 * logo_margin + title_height + xaxis_label_height + line_height * lines_per_logo)
stroke_width = 0.5
tic_length = 5
stack_margin = 0.5
yaxis_label = 'bits'
yaxis_tic_interval = 1.0
yaxis_minor_tic_ratio = 5
yaxis_minor_tic_interval = float(yaxis_tic_interval) / yaxis_minor_tic_ratio
xaxis_label = ""
xaxis_tic_interval = 1
number_interval = 5
fineprint = ""
shrink_fraction = 0.5
errorbar_fraction = 0.9
errorbar_width_fraction = 0.25
errorbar_gray = 0.75
text_font = "ArialMT"
logo_font = "Arial-BoldMT"
title_font = "ArialMT"
logo_label = ""
yaxis_scale = 2.0
end_type = "-"
debug = False
show_title = True
show_xaxis = True
show_xaxis_label = True
show_yaxis = True
show_yaxis_label = True
show_boxes = False
show_errorbars = True
show_fineprint = False
rotate_numbers = False
show_ends = False

logoformat = {
    'stacks_per_line': stacks_per_line,
    "creation_date": creation_date,
    "logo_width": logo_width,
    "logo_height": logo_height,
    "lines_per_logo": lines_per_logo,
    "line_width": line_width,
    "line_height": line_height,
    "line_margin_right": line_margin_right,
    "line_margin_left": line_margin_left,
    "line_margin_bottom": line_margin_bottom,
    "line_margin_top": line_margin_top,
    "title_height": title_height,
    "xaxis_label_height": xaxis_label_height,
    "creator_text": "Logo Generator",
    "logo_title": logo_title,
    "logo_margin": logo_margin,
    "stroke_width": stroke_width,
    "tic_length": tic_length,
    "stack_margin": stack_margin,
    "yaxis_label": yaxis_label,
    "yaxis_tic_interval": yaxis_tic_interval,
    "yaxis_minor_tic_interval": yaxis_minor_tic_interval,
    "xaxis_label": xaxis_label,
    "xaxis_tic_interval": xaxis_tic_interval,
    "number_interval": number_interval,
    "fineprint": fineprint,
    "shrink_fraction": shrink_fraction,
    "errorbar_fraction": errorbar_fraction,
    "errorbar_width_fraction": errorbar_width_fraction,
    "errorbar_gray": errorbar_gray,
    "small_fontsize": small_fontsize,
    "fontsize": fontsize,
    "title_fontsize": title_fontsize,
    "number_fontsize": number_fontsize,
    "text_font": text_font,
    "logo_font": logo_font,
    "title_font": title_font,
    "logo_label": logo_label,
    "yaxis_scale": yaxis_scale,
    "end_type": end_type,
    "debug": debug,
    "show_title": show_title,
    "show_xaxis": show_xaxis,
    "show_xaxis_label": show_xaxis_label,
    "show_yaxis": show_yaxis,
    "show_yaxis_label": show_yaxis_label,
    "show_boxes": show_boxes,
    "show_errorbars": show_errorbars,
    "show_fineprint": show_fineprint,
    "rotate_numbers": rotate_numbers,
    "show_ends": show_ends,
    "stack_height": stack_height,
    "stack_width": stack_width
}


class Color(object):

    def __init__(self, red: float, green: float, blue: float) -> None:
        if not (type(red) == type(green) == type(blue)):
            raise TypeError("Mixed floats and integers?")
        # Convert integer RBG values in [0, 255] to floats in [0, 1]
        if isinstance(red, int):
            red /= 255.0
        if isinstance(green, int):
            green /= 255.0
        if isinstance(blue, int):
            blue /= 255.0
        # Clip RBG values to [0, 1]
        self.red = max(0.0, min(red, 1.0))
        self.green = max(0.0, min(green, 1.0))
        self.blue = max(0.0, min(blue, 1.0))

    @classmethod
    def from_rgb(cls, r: float, g: float, b: float) -> "Color":
        return cls(r, g, b)


default_color_schemes = {
    dna_alphabet: {
        'A': Color.from_rgb(0, 255, 0),
        'C': Color.from_rgb(0, 0, 255),
        'G': Color.from_rgb(255, 127, 0),
        'T': Color.from_rgb(255, 0, 0)
    },
    rna_alphabet: {
        'A': Color.from_rgb(0, 255, 0),
        'C': Color.from_rgb(0, 0, 255),
        'G': Color.from_rgb(255, 127, 0),
        'U': Color.from_rgb(255, 0, 0)
    },
}


def annotate(seq_index):
    return "%d" % seq_index


def symbol_color(index, symbol, rank) -> Color:
    return default_color_schemes[dna_alphabet][symbol]


def format_color(color: Color) -> str:
    return " ".join(("[", str(color.red), str(color.green), str(color.blue), "]"))


def resource_string(modulename: str, resource: str) -> str:
    return read_text(modulename, resource)


def draw_logo():
    data = ["StartLine"]

    for seq_index in range(seq_from, seq_to + 1):

        stack_index = seq_index - seq_from

        if stack_index != 0 and (stack_index % logoformat['stacks_per_line']) == 0:
            data.append("")
            data.append("EndLine")
            data.append("StartLine")
            data.append("")

        data.append("(%s) StartStack" % annotate(seq_index))

        s = list(zip(contrib[seq_index], list(dna_alphabet)))
        s.sort(key=lambda x: x[1])
        s.reverse()
        s.sort(key=lambda x: x[0])
        s.reverse()

        C = float(sum(contrib[seq_index]))
        if C > 0:
            fraction_width = 1.0

            for rank, c in enumerate(s):
                color = symbol_color(seq_index, c[1], rank)
                if c[0] >= 0.05:
                    data.append(
                        " %f %f %s (%s) ShowSymbol"
                        % (
                            fraction_width,
                            c[0],
                            format_color(color),
                            c[1],
                        )
                )

        data.append("EndStack")
        data.append("")

    data.append("EndLine")

    substitutions = {}

    substitutions["logo_data"] = "\n".join(data)

    from_format = [
        "creation_date",
        "logo_width",
        "logo_height",
        "lines_per_logo",
        "line_width",
        "line_height",
        "line_margin_right",
        "line_margin_left",
        "line_margin_bottom",
        "line_margin_top",
        "title_height",
        "xaxis_label_height",
        "creator_text",
        "logo_title",
        "logo_margin",
        "stroke_width",
        "tic_length",
        "stacks_per_line",
        "stack_margin",
        "yaxis_label",
        "yaxis_tic_interval",
        "yaxis_minor_tic_interval",
        "xaxis_label",
        "xaxis_tic_interval",
        "number_interval",
        "fineprint",
        "shrink_fraction",
        "errorbar_fraction",
        "errorbar_width_fraction",
        "errorbar_gray",
        "small_fontsize",
        "fontsize",
        "title_fontsize",
        "number_fontsize",
        "text_font",
        "logo_font",
        "title_font",
        "logo_label",
        "yaxis_scale",
        "end_type",
        "debug",
        "show_title",
        "show_xaxis",
        "show_xaxis_label",
        "show_yaxis",
        "show_yaxis_label",
        "show_boxes",
        "show_errorbars",
        "show_fineprint",
        "rotate_numbers",
        "show_ends",
        "stack_height",
        "stack_width",
    ]

    for sf in from_format:
        substitutions[sf] = logoformat[sf]

    substitutions["shrink"] = str(logoformat['show_boxes']).lower()
    substitutions["default_color"] = format_color(Color.from_rgb(0, 0, 0))

    template = resource_string("logoformatter", "template.eps")
    logo = Template(template).substitute(substitutions)

    def convertEPStoSVG(logo_output):
        name = uuid.uuid4()

        with open(f'{name}.eps', 'w') as file:
            file.write(logo_output)

        os.system(f'inkscape -f {name}.eps --export-plain-svg {name}.svg')

        with open(f'{name}.svg', 'r') as file:
            temp = file.read().encode()

        os.remove(f'{name}.eps')
        os.remove(f'{name}.svg')
        return temp

    return convertEPStoSVG(logo)
