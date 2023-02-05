!#/usr/bin/bash

out = print $1 | grep -v '^>' | sed -E "s/\n(\w)/\1/g"
print $out

sed -E "s/\n(\w)/\1/g" << cat sample_seqs.fa | grep -v '^>'

^>[^\n]*\n

