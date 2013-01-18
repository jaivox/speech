#!/bin/sh
mkdir temp
cat PATlm_training_file | text2wfreq | wfreq2vocab -top 30000 > PATproject.vocab
cat PATlm_training_file | text2idngram -temp ./temp/ -vocab PATproject.vocab > PATproject.idngram
idngram2lm  -vocab PATproject.vocab -idngram PATproject.idngram -arpa PATproject.arpabo -vocab_type 1 -good_turing -disc_ranges 1 7 7 -calc_mem -context ccs.ccs -four_byte_counts -verbosity 1
sphinx_lm_convert -i PATproject.arpabo -o PATproject.arpabo.DMP
rm -rf temp
