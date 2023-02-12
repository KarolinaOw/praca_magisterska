#!/usr/bin/python

from pyspark.sql.types import ArrayType, StructField, StructType, StringType, IntegerType
import pyspark.sql.functions
from pyspark.sql.functions import log, lit, col, when
from pyspark.sql import SparkSession
import pyspark
import argparse
from google.cloud import storage
import os
import sys
import atexit
from time import perf_counter
from functools import reduce

session = SparkSession.builder.master('local[*]').appName('Logo Generator').getOrCreate()


def seconds_to_str(t):
    return "%d:%02d:%02d.%03d" % \
        reduce(lambda ll, b: divmod(ll[0], b) + ll[1:],
               [(t * 1000,), 1000, 60, 60])


line = "=" * 40


def time_log(s, elapsed=None):
    print(line)
    print(seconds_to_str(perf_counter()), '-', s)
    if elapsed:
        print("Elapsed time:", elapsed)
    print(line)
    print()


def time_end_log():
    end = perf_counter()
    elapsed = end - start
    time_log("End Program", seconds_to_str(elapsed))


def now():
    return seconds_to_str(perf_counter())


start = perf_counter()
atexit.register(time_end_log)

parser = argparse.ArgumentParser()
parser.add_argument("--filename")
args = parser.parse_args()
if args.filename:
    filename = args.filename
else:
    raise Exception("No data")

print("Loading input file " + filename)

data = session.read.csv(filename)
number_of_seq = data.count()

rdd = data.rdd
rdd = rdd.map(lambda x: x._c0).map(lambda x: list(x))
rdd = rdd.flatMap(lambda x: zip(range(1, len(x) + 1), x)).filter(lambda x: x[1] != '\n')
f = rdd.map(lambda x: (x[0], (
1 if x[1] == 'A' else 0, 1 if (x[1] == 'T') | (x[1] == 'U') else 0, 1 if x[1] == 'G' else 0, 1 if x[1] == 'C' else 0)))
f = f.reduceByKey(lambda x, y: (x[0] + y[0], x[1] + y[1], x[2] + y[2], x[3] + y[3]))

freq = f.toDF(['pos', 'quantities']).sort('pos').select('pos',
                                                        (col('quantities._1') / number_of_seq).alias("A"),
                                                        (col('quantities._2') / number_of_seq).alias("TU"),
                                                        (col('quantities._3') / number_of_seq).alias("G"),
                                                        (col('quantities._4') / number_of_seq).alias("C"))

info = freq.select('pos', 'A', 'TU', 'G', 'C', (log(2.0, lit(4)) +
                                                (when(col('A') != 0.0, col('A') * log(2.0, col('A'))).otherwise(0.0) +
                                                 when(col("TU") != 0.0, col('TU') * log(2.0, col('TU'))).otherwise(0.0) +
                                                 when(col('G') != 0.0, col('G') * log(2.0, col('G'))).otherwise(0.0) +
                                                 when(col('C') != 0.0, col('C') * log(2.0, col('C'))).otherwise(0.0))).alias('info'))
contribution = info.select((col('A') * col("info")).alias("con_A"),
                           (col('C') * col("info")).alias("con_C"),
                           (col('G') * col("info")).alias("con_G"),
                           (col('TU') * col("info")).alias("con_TU"))

client = storage.Client()
bucket = storage.Bucket(client, 'logo-seq-creator')
blob = bucket.blob("logo-requests/output/" + os.path.basename(filename))
blob.upload_from_string(contribution.toPandas().to_csv(index=False, header=False), 'text/csv')
