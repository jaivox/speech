#!/bin/sh
mkdir temp
cat road.sent | text2wfreq | wfreq2vocab -top 30000 > batch.vocab
cat road.sent | text2idngram -temp ./temp/ -vocab batch.vocab > batch.idngram
idngram2lm  -vocab batch.vocab -idngram batch.idngram -arpa batch.arpabo -vocab_type 1 -good_turing -disc_ranges 1 7 7 -calc_mem -context ccs.ccs -four_byte_counts -verbosity 1
sphinx_lm_convert -i batch.arpabo -o batch.arpabo.DMP
rm -rf temp
