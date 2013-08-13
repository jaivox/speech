#!/bin/sh
mkdir temp
cat road.sent | text2wfreq | wfreq2vocab -top 30000 > road.vocab
cat road.sent | text2idngram -temp ./temp/ -vocab road.vocab > road.idngram
idngram2lm  -vocab road.vocab -idngram road.idngram -arpa road.arpabo -vocab_type 1 -good_turing -disc_ranges 1 7 7 -calc_mem -context ccs.ccs -four_byte_counts -verbosity 1
sphinx_lm_convert -i road.arpabo -o road.arpabo.DMP
rm -rf temp
