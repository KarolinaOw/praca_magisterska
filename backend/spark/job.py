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

session = SparkSession.builder.master('local[*]').appName('Logo Generator').getOrCreate()

parser = argparse.ArgumentParser()
parser.add_argument("--filename")
args = parser.parse_args()
if args.filename:
    filename = args.filename
else:
    raise Exception("No data")

print("Loading input file " + filename)

data = session.read.csv(filename)
data.show()
number_of_seq = data.count()

rdd = data.rdd
rdd = rdd.map(lambda x: x._c0).map(lambda x: list(x))
rdd = rdd.flatMap(lambda x: zip(range(1,len(x)+1), x)).filter(lambda x: x[1] != '\n')
f = rdd.map(lambda x: (x[0], (1 if x[1]=='A' else 0, 1 if x[1]=='T' or x[1]=='U' else 0, 1 if x[1]=='G' else 0, 1 if x[1]=='C' else 0)))
f = f.reduceByKey(lambda x, y: (x[0] + y[0], x[1] + y[1], x[2] + y[2], x[3] + y[3]))

freq = f.toDF(['pos','quantities']).sort('pos').select('pos',
                                                       (col('quantities._1') / number_of_seq).alias("A"), (col('quantities._2') / number_of_seq).alias("T|U"),
                                                       (col('quantities._3') / number_of_seq).alias("G"), (col('quantities._4') / number_of_seq).alias("C"))

info = freq.select('pos', 'A', 'T|U', 'G', 'C', ((log(2.0, lit(4)))+
                                                 (when(col('A') != 0.0, col('A')*log(2.0,col('A'))).otherwise(0.0)+
                                                  when(col("T|U") != 0.0, col('T|U')*log(2.0,col('T|U'))).otherwise(0.0)+
                                                  when(col('G') != 0.0, col('G')*log(2.0,col('G'))).otherwise(0.0)+
                                                  when(col('C') != 0.0, col('C')*log(2.0,col('C'))).otherwise(0.0))).alias('info'))
contribution = info.select('pos', 'info', (col('A')*col("info")).alias("con_A"), (col('T|U')*col("info")).alias("con_T|U"), (col('G')*col("info")).alias("con_G"), (col('C')*col("info")).alias("con_C"))

client = storage.Client()
bucket = storage.Bucket(client, 'logo-seq-creator')
blob = bucket.blob("logo-requests/output/" + os.path.basename(filename))
blob.upload_from_string(contribution.toPandas().to_csv(), 'text/csv')
