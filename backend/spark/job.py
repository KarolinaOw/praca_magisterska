from pyspark.sql.types import ArrayType, StructField, StructType, StringType, IntegerType
import pyspark.sql.functions
from pyspark.sql.functions import log, lit


rdd = spark.sparkContext.parallelize(seq_list, numSlices=20)

rdd = rdd.map(lambda x: list(x))
rdd = rdd.flatMap(lambda x: zip(range(1,len(x)+1), x)).filter(lambda x: x[1] != '\n')
f = rdd.map(lambda x: (x[0], (1 if x[1]=='A' else 0, 1 if x[1]=='T' else 0, 1 if x[1]=='G' else 0, 1 if x[1]=='C' else 0)))
f = f.reduceByKey(lambda x, y: (x[0] + y[0], x[1] + y[1], x[2] + y[2], x[3] + y[3]))

freq = f.toDF(['pos','quantities']).sort('pos').select('pos',
(col('quantities._1') / number_of_seq).alias("A"), (col('quantities._2') / number_of_seq).alias("T"),
(col('quantities._3') / number_of_seq).alias("G"), (col('quantities._4') / number_of_seq).alias("C"))

info = freq.select('pos', 'A', 'T', 'G', 'C', ((log(2.0, lit(4)))+(col('A')*log(2.0,col('A'))+col('T')*log(2.0,col('T'))+col('G')*log(2.0,col('G'))+col('C')*log(2.0,col('C')))).alias('info'))

contribution = info.select('pos', 'info', (col('A')*col("info")).alias("con_A"), (col('T')*col("info")).alias("con_T"), (col('G')*col("info")).alias("con_G"), (col('C')*col("info")).alias("con_C"))
