#!/usr/bin/env python3
# SPDX-License-Identifier: MIT

import pandas as pd
import matplotlib.pyplot as plt
import sys
from matplotlib.ticker import FuncFormatter
from datasize import DataSize

def memory_units(x, pos) -> str:
    return '{:.2MiB}'.format(DataSize(x))

def analyze(result_file: str, output_folder = None) -> None:
    with open(result_file, encoding='utf-8') as inputfile:
        df = pd.read_csv(inputfile)

        cpu_percent = df['CPUPercent']
        memory = df['MemoryUsageInBytes']

        fig, (ax, ax2) = plt.subplots(ncols=2, figsize=(18,6), tight_layout=True)

        cpu_percent.plot(ax = ax, ylabel = "in % percent", title = "CPU Percent Used")

        formatter = FuncFormatter(memory_units)
        ax2.yaxis.set_major_formatter(formatter)
        memory.plot(ax = ax2, ylabel = "in bytes", title = "Memory Used")

        print("\nCPU\n")
        print(f"Min. CPU Percent: {cpu_percent.min()}%")
        print(f"Max. CPU Percent: {cpu_percent.max()}%")

        print("\nMemory\n")

        print("Min. Memory: {:.2MiB}".format(DataSize(memory.min())))
        print("Max. Memory: {:.2MiB}".format(DataSize(memory.max())))

        if output_folder != None:
            output_file_path = output_folder + "analysis.svg"
            print(f"Writing file to: {output_file_path}")
            plt.savefig(output_file_path)
        else:
            plt.show()
    
if __name__ == '__main__':
    if len(sys.argv) == 2:
        result_file = sys.argv[1]
        analyze(result_file)
    elif len(sys.argv) >= 3:
        result_file = sys.argv[1]
        output_folder = sys.argv[2]
        analyze(result_file, output_folder)
    else:
        print("Usage: {} <result_file> [<graph-output-folder>]".format(sys.argv[0]))
        sys.exit(1)